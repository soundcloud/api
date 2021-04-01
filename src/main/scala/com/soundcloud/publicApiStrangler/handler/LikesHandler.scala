package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.client.liebling.{LikeDeleted, LikeNotFound}
import com.soundcloud.publicApiStrangler.handler.representation.collection.CollectionResponse
import com.soundcloud.publicApiStrangler.service._
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.support.ErrorResponse
import com.soundcloud.publicApiStrangler.support.TrackUrnUtil.getTrackUrn
import com.soundcloud.publicApiStrangler.support.UserUrnUtil.getUserUrn
import com.soundcloud.publicApiStrangler.support.PlaylistUrnUtil.getPlaylistUrn
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, Return, Throw, Try}
import play.api.libs.json.Json

class LikesHandler(userAuthentication: UserAuthentication, likesService: LikesService) {

  def getMeLikedTrackId(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, userUrn) =>
      performGetUserLikedTrackId(req, session, userUrn.identifier)
    }
  }

  def createMeLikedTrackId(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, _) =>
      Try(getTrackUrn(req)) match {
        case Return(urn) =>
          likesService.createTrackLike(session, urn).map {
            case OkCreateResponse | OkCreatedCreateResponse => JsonResponseBuilder.ok(requestBodyForStatus(Status.Ok))
            case NotAuthorizedCreateResponse => ErrorResponse(Status.Unauthorized)
            case NotFoundCreateResponse => ErrorResponse.notFound()
            case SpamBlockedCreateResponse => ErrorResponse(Status.TooManyRequests)
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
            case LikeDeleted => JsonResponseBuilder.ok(requestBodyForStatus(Status.Ok))
            case LikeNotFound => ErrorResponse.notFound()
          }
        case _ => Future.value(ErrorResponse.badRequest())
      }
  }

  def createMeLikedPlaylistId(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, _) =>
      Try(getPlaylistUrn(req)) match {
        case Return(urn) =>
          likesService.createPlaylistLike(session, urn).map {
            case OkCreatedCreateResponse => JsonResponseBuilder.created(requestBodyForStatus(Status.Created))
            case OkCreateResponse => JsonResponseBuilder.ok(requestBodyForStatus(Status.Ok))
            case NotAuthorizedCreateResponse => ErrorResponse(Status.Unauthorized)
            case NotFoundCreateResponse => ErrorResponse.notFound()
            case SpamBlockedCreateResponse => ErrorResponse(Status.TooManyRequests)
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
            case LikeDeleted => JsonResponseBuilder.ok(requestBodyForStatus(Status.Ok))
            case LikeNotFound => ErrorResponse.notFound()
          }
        case _ => Future.value(ErrorResponse.badRequest())
      }
  }

  private def performGetUserLikedTrackId(
      request: HandlerRequest,
      session: UserSession,
      userId: String
  ): Future[Response] = {
    Try(getUserUrn(userId)) match {
      case Return(userUrn) =>
        Try(getTrackUrn(request)) match {
          case Return(trackUrn) =>
            likesService
              .userTrackLikeForUrn(session, userUrn, trackUrn)
              .map {
                case None => ErrorResponse.notFound()
                case Some(track) => JsonResponseBuilder.ok(Json.stringify(Json.toJson(track)))

              }
          case _ => Future.value(ErrorResponse.notFound())
        }
      case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
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
    val hasLinkedPartitioning = request.params.contains("linked_partitioning")
    val pagination = CursorBasedPagination.build(request, Seq("linked_partitioning"))

    Try(getUserUrn(userId)) match {
      case Return(urn) =>
        val tracksCollection = likesService
          .userTracksLikes(session, urn, pagination)
          .map(Good(_))
        CollectionResponse.handleCollectionResponse(tracksCollection, hasLinkedPartitioning)
      case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
    }
  }

  private def requestBodyForStatus(status: Status): String = {
    Json.stringify(Json.obj("status" -> s"${status.code} - ${status.reason}"))
  }
}
