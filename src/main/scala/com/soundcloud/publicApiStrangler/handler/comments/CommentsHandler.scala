package com.soundcloud.publicApiStrangler.handler.comments

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.outcome.{Bad, Good, _}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.comments.Comment
import com.soundcloud.publicApiStrangler.service.comments.CommentService
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.twitter.conversions.DurationOps._
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Duration, Future, Try}
import play.api.libs.json.{JsValue, Json}
import com.soundcloud.publicApiStrangler.client.mothership._

class CommentsHandler(
    userAuthentication: UserAuthentication,
    commentService: CommentService
) {

  def getCommentsForTrack(request: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      val track = Urn("soundcloud", "tracks", request.routeParams("trackId"))
      commentService.fetchTracksComments(session, track, pagination(request)).map {
        case Good(comments) => {
          val body = Collection.getNonNullRepresentation(comments, request.params.get("linked_partitioning").isDefined)
          JsonResponseBuilder(Status.Ok, body, buildCacheHeaders(Some(10.minutes))).build
        }
        case Bad(NotFound(_)) => JsonResponseBuilder.notFound(notFoundErrorString)
        case Bad(_) => JsonResponseBuilder.badRequest()
      }
    }
  }

  def createCommentsForTrack(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      val commentJsValue = parseCommentJson(request)
      commentJsValue match {
        case None => Future.value(JsonResponseBuilder(Status.UnprocessableEntity, noCommentErrorString).build)
        case Some(commentJson) => {
          val body = (commentJson \ "body").asOpt[String]
          if (!body.isDefined) {
            Future.value(JsonResponseBuilder(Status.UnprocessableEntity, noCommentBodyErrorString).build)
          } else {
            val commentParams = extractCommentParams(request, commentJson)
            commentService.createComment(session, commentParams).map {
              case Good(comment) => createResponse(comment)
              case Bad(applicationError) => createErrorResponse(applicationError)
            }
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
      case _: NotAllowed => JsonResponseBuilder(Status.Forbidden, forbiddenErrorString).build
      case CustomError(TooManyRequests, Some(CustomError(context: RateLimitedError, _))) =>
        JsonResponseBuilder(Status.TooManyRequests, spamWarningError(context.spamWarningUrn)).build
      case _ => JsonResponseBuilder.badRequest()
    }
  }

  private def extractCommentParams(request: HandlerRequest, commentJson: JsValue): CreateCommentParams = {
    val trackUrn = Urn("soundcloud", "tracks", request.routeParams("trackId"))
    val timestamp = timestampInt(commentJson)
    val secretToken = request.params.get("secret_token")
    val body = (commentJson \ "body").as[String]
    CreateCommentParams(trackUrn, body, timestamp, secretToken)
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

  private val notFoundErrorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""
  private val noCommentErrorString = """{"errors":[{"error_message":"Parameter comment is missing"}]}"""
  private val noCommentBodyErrorString = """{"errors":[{"error_message":"Body can't be blank"}]}"""
  private val forbiddenErrorString =
    """{"errors":[{"error_message":"You are not authorized to perform that action."}]}"""
  private def spamWarningError(urn: Urn): String = s"""{"spam_warning_urn":"${urn.toString}"}"""
}
