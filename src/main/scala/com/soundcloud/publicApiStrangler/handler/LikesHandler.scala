package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.service.pagination.Pagination
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TracksCollection
import com.soundcloud.publicApiStrangler.handler.representation.tracks.TrackRepresentationResponse.handleResponseFromService
import com.soundcloud.publicApiStrangler.service._
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.client.liebling.{LikeDeleted, LikeNotFound}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.Json

import scala.util.{Success, Try}

class LikesHandler(userAuthentication: UserAuthentication, likesService: LikesService, baseUrl: String) {
  private val numericRegexp = """\d+""".r

  def getUserLikedTrackId(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      val userId = req.routeParams("userId")
      performGetUserLikedTrackId(req, session, userId)
    }
  }

  def getMeLikedTrackId(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, userUrn) =>
      performGetUserLikedTrackId(req, session, userUrn.identifier)
    }
  }

  def createMeLikedTrackId(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, _) =>
      val trackId = req.routeParams("trackId")
      Try(Urn("soundcloud", "tracks", trackId)) match {
        case Success(trackUrn @ Urn(_, _, numericRegexp())) =>
          likesService.createTrackLike(session, trackUrn).map { createResponse =>
            val body = responseBodyForCreateResponse(createResponse)
            createResponse match {
              case OkCreatedCreateResponse => JsonResponseBuilder.created(body)
              case OkCreateResponse => JsonResponseBuilder.ok(body)
              case NotAuthorizedCreateResponse => JsonResponseBuilder.unauthorized(body)
              case NotFoundCreateResponse => JsonResponseBuilder.notFound(body)
              case SpamBlockedCreateResponse => JsonResponseBuilder(Status.TooManyRequests, body).build
            }
          }
        case _ => Future.value(JsonResponseBuilder.badRequest(requestBodyForStatus(Status.BadRequest)))
      }
    }
  }

  def deleteMeLikedTrackId(req: HandlerRequest): Future[Response] = userAuthentication.withLoggedInUser(req) {
    (session, _) =>
      val trackId = req.routeParams("trackId")
      Try(Urn("soundcloud", "tracks", trackId)) match {
        case Success(trackUrn @ Urn(_, _, numericRegexp())) =>
          likesService.deleteTrackLike(session, trackUrn).map {
            case LikeDeleted => JsonResponseBuilder.ok(requestBodyForStatus(Status.Ok))
            case LikeNotFound => JsonResponseBuilder.notFound(notFoundErrorString)
          }
        case _ => Future.value(JsonResponseBuilder.badRequest(requestBodyForStatus(Status.BadRequest)))
      }
  }

  private def performGetUserLikedTrackId(
      request: HandlerRequest,
      session: UserSession,
      userId: String
  ): Future[Response] = {
    val trackId = request.routeParams("trackId")
    Try(Urn("soundcloud", "users", userId)) match {
      case Success(userUrn @ Urn(_, _, numericRegexp())) =>
        Try(Urn("soundcloud", "tracks", trackId)) match {
          case Success(trackUrn @ Urn(_, _, numericRegexp())) =>
            likesService
              .userTrackLikeForUrn(session, userUrn, trackUrn)
              .map {
                case None => JsonResponseBuilder.notFound(notFoundErrorString)
                case Some(track) => JsonResponseBuilder.ok(Json.stringify(Json.toJson(track)))

              }
          case _ => Future.value(JsonResponseBuilder.notFound(notFoundErrorString))
        }
      case _ => Future.value(JsonResponseBuilder.notFound(notFoundErrorString))
    }
  }

  def getUserTracksLikes(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      val userId = req.routeParams("userId")
      performGetTracksLikes(req, session, userId)
    }
  }

  def getMeTracksLikes(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, userUrn) =>
      performGetTracksLikes(req, session, userUrn.identifier)
    }
  }

  private def performGetTracksLikes(request: HandlerRequest, session: UserSession, userId: String): Future[Response] = {
    val hasLinkedPartitioning = request.params.get("linked_partitioning").isDefined
    val pagination = Pagination.buildCursorBasedPagination(request, Seq("linked_partitioning"))

    def fetchTrackRepresentation(urn: Urn): Future[Outcome[TracksCollection]] = {
      likesService
        .userTracksLikes(session, urn, pagination)
        .map(Good(_))
    }

    Try(Urn("soundcloud", "users", userId)) match {
      case Success(urn @ Urn(_, _, numericRegexp())) =>
        val trackRepresentation = fetchTrackRepresentation(urn)
        handleResponseFromService(trackRepresentation, hasLinkedPartitioning)
      case _ => Future.value(JsonResponseBuilder.notFound(notFoundErrorString))
    }
  }

  private val notFoundErrorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""

  private def responseBodyForCreateResponse(createResponse: CreateResponse): String = {
    val status = createResponse match {
      case OkCreatedCreateResponse => Status.Created
      case OkCreateResponse => Status.Ok
      case NotAuthorizedCreateResponse => Status.Forbidden
      case NotFoundCreateResponse => Status.NotFound
      case SpamBlockedCreateResponse => Status.TooManyRequests
    }
    requestBodyForStatus(status)
  }

  private def requestBodyForStatus(status: Status): String = {
    Json.stringify(Json.obj("status" -> s"${status.code} - ${status.reason}"))
  }
}
