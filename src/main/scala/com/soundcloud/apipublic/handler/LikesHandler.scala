package com.soundcloud.apipublic.handler

import com.soundcloud.apipublic.handler.representation.collection.CollectionResponse
import com.soundcloud.apipublic.handler.support.requestParser.{AccessParams, AccessParamsExtractor}
import com.soundcloud.apipublic.service._
import com.soundcloud.apipublic.service.pagination.CursorBasedPagination
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.users.UserRepresentationsService
import com.soundcloud.apipublic.support.ErrorResponse
import com.soundcloud.apipublic.support.PlaylistUrnUtil.getPlaylistUrn
import com.soundcloud.apipublic.support.TrackUrnUtil.getTrackUrn
import com.soundcloud.apipublic.support.UserUrnUtil.getUserUrn
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.outcome.{Bad, _}
import com.soundcloud.jvmkit.module.util.session.UserSession
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
            case Bad(NotAllowed(_)) => ErrorResponse(Status.Forbidden)
            case Bad(NotFound(_)) => ErrorResponse.notFound()
            case Bad(NotValid(reason)) => ErrorResponse.badRequest(reason.mkString("; "))
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
      val access = AccessParamsExtractor.unapply(req.params, predefinedAccess = AccessParams.explicitAccess)
      performGetTracksLikes(req, session, userUrn.identifier, access)
    }
  }

  def getTrackLikers(request: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      val hasLinkedPartitioning = request.params.contains("linked_partitioning")
      val pagination = CursorBasedPagination.build(request, Seq("linked_partitioning"))

      Try(getTrackUrn(request)) match {
        case Return(urn) =>
          likesService.trackLikers(session, urn, pagination).flatMap {
            case Good(response) =>
              userRepresentationsService.users(session, response.urns).map { users =>
                JsonResponseBuilder
                  .ok(Collection.getRepresentation(Collection(users, response.nextHRef), hasLinkedPartitioning))
              }
            case Bad(NotFound(_)) => Future.value(ErrorResponse.notFound())
            case _ => Future.value(ErrorResponse(Status.InternalServerError))
          }
        case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
      }
    }
  }

  def getUserPlaylistsLikes(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      val userId = req.routeParams("userId")
      performGetPlaylistsLikes(req, session, userId)
    }
  }

  def getMePlaylistsLikes(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, userUrn) =>
      performGetPlaylistsLikes(req, session, userUrn.identifier)
    }
  }

  private def performGetPlaylistsLikes(
      request: HandlerRequest,
      session: UserSession,
      userId: String
  ): Future[Response] = {
    val hasLinkedPartitioning = request.params.contains("linked_partitioning")
    val pagination = CursorBasedPagination.build(request, Seq("linked_partitioning"))

    Try(getUserUrn(userId)) match {
      case Return(urn) =>
        val playlistsCollection = likesService
          .userPlaylistsLikes(session, urn, pagination)
          .map(Good(_))
        CollectionResponse.handleCollectionResponse(playlistsCollection, hasLinkedPartitioning)
      case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
    }
  }

  private def performGetTracksLikes(
      request: HandlerRequest,
      session: UserSession,
      userId: String,
      access: AccessParams
  ): Future[Response] = {
    val hasLinkedPartitioning = request.params.contains("linked_partitioning")
    val pagination = CursorBasedPagination.build(request, Seq("linked_partitioning", "access"))

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
