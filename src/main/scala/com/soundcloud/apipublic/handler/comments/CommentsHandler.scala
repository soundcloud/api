package com.soundcloud.apipublic.handler.comments

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.outcome.{Bad, Good, _}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.client.moshimoshicomments.Comment
import com.soundcloud.apipublic.client.mothership._
import com.soundcloud.apipublic.service.comments.CommentService
import com.soundcloud.apipublic.service.pagination.OffsetBasedPagination
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.support.ErrorResponse
import com.soundcloud.apipublic.support.TrackUrnUtil.getTrackUrn
import com.twitter.conversions.DurationOps._
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util._
import play.api.libs.json.{JsString, JsValue, Json}

class CommentsHandler(
    userAuthentication: UserAuthentication,
    commentService: CommentService
) {

  def getCommentsForTrack(request: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      Try(getTrackUrn(request)) match {
        case Return(urn) =>
          commentService
            .fetchTracksComments(session, urn, pagination(request), secretToken(request))
            .value
            .map {
              case Good(comments) =>
                val body = Collection.getNonNullRepresentation(comments, request.params.contains("linked_partitioning"))
                JsonResponseBuilder(Status.Ok, body, buildCacheHeaders(Some(10.minutes))).build
              case Bad(NotFound(_)) => ErrorResponse.notFound()
              case Bad(_) => ErrorResponse.badRequest()
            }
        case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
      }
    }
  }

  private def secretToken(request: HandlerRequest) = request.params.get("secret_token")

  def createCommentsForTrack(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      val commentJsValue = parseCommentJson(request)
      commentJsValue match {
        case None => Future.value(ErrorResponse(Status.UnprocessableEntity, noCommentErrorString))
        case Some(commentJson) =>
          val body = (commentJson \ "body").asOpt[String]
          if (body.isEmpty) {
            Future.value(ErrorResponse(Status.UnprocessableEntity, noCommentBodyErrorString))
          } else {
            extractCommentParams(request, commentJson) match {
              case Right(commentParams) =>
                commentService.createComment(session, commentParams).map {
                  case Good(comment) => createResponse(comment)
                  case Bad(applicationError) => createErrorResponse(applicationError)
                }
              case Left(err) => Future.value(createErrorResponse(err))
            }
          }
      }
    }
  }

  private def createResponse(comment: Comment): Response = {
    val headers = Map("Location" -> comment.location)
    JsonResponseBuilder(Status.Created, Json.stringify(Json.toJson(comment)), headers).build
  }

  private def createErrorResponse(applicationError: ApplicationError): Response = {
    applicationError match {
      case _: NotAllowed => ErrorResponse.badRequest()
      case CustomError(TooManyRequests, Some(CustomError(context: RateLimitedError, _))) =>
        ErrorResponse(Status.TooManyRequests, "Spam warning", Some(spamWarningError(context.spamWarningUrn)))
      case _ => ErrorResponse.badRequest()
    }
  }

  private def extractCommentParams(
      request: HandlerRequest,
      commentJson: JsValue
  ): Outcome[CreateCommentParams] =
    Try(getTrackUrn(request)) match {
      case Return(urn) =>
        val timestamp = timestampInt(commentJson)
        val secretToken = request.params.get("secret_token")
        val body = (commentJson \ "body").as[String]
        CreateCommentParams(urn, body, timestamp, secretToken).good
      case Throw(e) => NotValid(e.getMessage).bad
    }

  private def timestampInt(commentJson: JsValue): Option[Int] = {
    (commentJson \ "timestamp").asOpt[String] match {
      case Some(timestampString) => Try(timestampString.toFloat.toInt).toOption
      case None =>
        (commentJson \ "timestamp").asOpt[Float] match {
          case Some(timestampFloat) => Some(timestampFloat.toInt)
          case None => None
        }
    }
  }

  private def parseCommentJson(request: HandlerRequest): Option[JsValue] = {
    Try(Json.parse(request.contentString)).toOption match {
      case Some(jsonBody: JsValue) => (jsonBody \ "comment").toOption
      case None => None
    }
  }

  private def pagination(request: HandlerRequest): OffsetBasedPagination = {
    val params = request.params
    val limit = params.get("limit").map(_.toInt).getOrElse(200) // 200 limit to match the mothership behaviour
    val offset = params.get("offset").map(_.toInt)

    val basePagination =
      OffsetBasedPagination.build(request, request.params.keySet.toSeq)
    basePagination.copy(limit = limit, offset = offset)
  }

  private def buildCacheHeaders(duration: Option[Duration]): Map[String, String] =
    duration
      .map { maxAge =>
        Map("Cache-Control" -> s"max-age=${maxAge.inSeconds.toString}, must-revalidate")
      }
      .getOrElse(Map.empty)

  private val noCommentErrorString = "Parameter comment is missing."
  private val noCommentBodyErrorString = "Body can't be blank."
  private def spamWarningError(urn: Urn): Map[String, JsString] = Map("spam_warning_urn" -> JsString(urn.toString))
}
