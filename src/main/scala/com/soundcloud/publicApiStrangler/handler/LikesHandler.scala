package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.outcome.{Bad, _}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.handler.representation.collection.CollectionResponse
import com.soundcloud.publicApiStrangler.handler.support.requestParser.{AccessParams, AccessParamsExtractor}
import com.soundcloud.publicApiStrangler.service._
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.users.UserRepresentationsService
import com.soundcloud.publicApiStrangler.support.ErrorResponse
import com.soundcloud.publicApiStrangler.support.PlaylistUrnUtil.getPlaylistUrn
import com.soundcloud.publicApiStrangler.support.TrackUrnUtil.getTrackUrn
import com.soundcloud.publicApiStrangler.support.UserUrnUtil.getUserUrn
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, Return, Throw, Try}
import play.api.libs.json.Json

class LikesHandler(
    userAuthentication: UserAuthentication,
    likesService: LikesService,
    userRepresentationsService: UserRepresentationsService
) {

  def createMeLikedTrackId(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, _) =>
      Try(getTrackUrn(req)) match {
        case Return(urn) =>
          likesService.createTrackLike(session, urn).map {
            case Good(_) => JsonResponseBuilder.ok(requestBodyForStatus(Status.Ok))
            case Bad(NotAuthorized(_)) => ErrorResponse(Status.Unauthorized)
            case Bad(NotFound(_)) => ErrorResponse.notFound()
            case Bad(UnexpectedError(TwinagleException(ErrorCode.ResourceExhausted, _, _, _))) =>
              ErrorResponse(Status.TooManyRequests)
            case _ => ErrorResponse(Status.InternalServerError)
          }
        case _ => Future.value(ErrorResponse.badRequest())
      }
    }
  }

  def deleteMeLikedTrackId(req: HandlerRequest): Future[Response] = userAuthentication.withLoggedInUser(req) {
    (session, _) =>
      Try(getTrackUrn(req)) match {
        case Return(urn) =>
          likesService.deleteTrackLike(session, urn).map {
            case Good(_) => JsonResponseBuilder.ok(requestBodyForStatus(Status.Ok))
            case Bad(NotFound(_)) => ErrorResponse.notFound()
            case Bad(NotValid(_)) => ErrorResponse.badRequest()
            case _ => ErrorResponse(Status.InternalServerError)
          }
        case _ => Future.value(ErrorResponse.badRequest())
      }
  }

  def createMeLikedPlaylistId(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, _) =>
      Try(getPlaylistUrn(req)) match {
        case Return(urn) =>
          likesService.createPlaylistLike(session, urn).map {
            case Good(_) => JsonResponseBuilder.ok(requestBodyForStatus(Status.Ok))
            case Bad(NotAuthorized(_)) => ErrorResponse(Status.Unauthorized)
            case Bad(NotFound(_)) => ErrorResponse.notFound()
            case Bad(HttpServiceError(HttpResponseFields(Status.TooManyRequests.code, _, _, _))) =>
              ErrorResponse(Status.TooManyRequests)
            case _ => ErrorResponse(Status.InternalServerError)
          }
        case _ => Future.value(ErrorResponse.badRequest())
      }
    }
  }

  def deleteMeLikedPlaylistId(req: HandlerRequest): Future[Response] = userAuthentication.withLoggedInUser(req) {
    (session, _) =>
      Try(getPlaylistUrn(req)) match {
        case Return(urn) =>
          likesService.deletePlaylistLike(session, urn).map {
            case Good(_) => JsonResponseBuilder.ok(requestBodyForStatus(Status.Ok))
            case Bad(NotFound(_)) => ErrorResponse.notFound()
            case Bad(NotValid(_)) => ErrorResponse.badRequest()
            case _ => ErrorResponse(Status.InternalServerError)
          }
        case _ => Future.value(ErrorResponse.badRequest())
      }
  }

  def getUserTracksLikes(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      val userId = req.routeParams("userId")
      val access = AccessParamsExtractor.unapply(req.params)
      performGetTracksLikes(req, session, userId, access)
    }
  }

  def getMeTracksLikes(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, userUrn) =>
      performGetTracksLikes(req, session, userUrn.identifier, AccessParams.explicitAccess)
    }
  }

  def getTrackLikers(request: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      val hasLinkedPartitioning = request.params.contains("linked_partitioning")
      val pagination = CursorBasedPagination.build(request, Seq("linked_partitioning"))

      Try(getTrackUrn(request)) match {
        case Return(urn) => {
          likesService.trackLikers(session, urn, pagination).flatMap {
            case Good(response) => {
              userRepresentationsService.users(session, response.urns).map { users =>
                JsonResponseBuilder
                  .ok(Collection.getRepresentation(Collection(users, response.nextHRef), hasLinkedPartitioning))
              }
            }
            case Bad(NotFound(_)) => Future.value(ErrorResponse.notFound())
            case _ => Future.value(ErrorResponse(Status.InternalServerError))
          }
        }
        case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
      }
    }
  }

  private def performGetTracksLikes(
      request: HandlerRequest,
      session: UserSession,
      userId: String,
      access: AccessParams
  ): Future[Response] = {
    val hasLinkedPartitioning = request.params.contains("linked_partitioning")
    val pagination = CursorBasedPagination.build(request, Seq("linked_partitioning"))

    Try(getUserUrn(userId)) match {
      case Return(urn) =>
        val tracksCollection = likesService
          .userTracksLikes(session, urn, access, pagination)
          .map(Good(_))
        CollectionResponse.handleCollectionResponse(tracksCollection, hasLinkedPartitioning)
      case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
    }
  }

  private def requestBodyForStatus(status: Status): String = {
    Json.stringify(Json.obj("status" -> s"${status.code} - ${status.reason}"))
  }
}
