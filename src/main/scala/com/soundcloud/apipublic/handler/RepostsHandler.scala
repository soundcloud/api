package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.outcome.{Bad, Good}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.client.reposts.RepostsClient._
import com.soundcloud.apipublic.service.RepostsService
import com.soundcloud.apipublic.service.pagination.CursorBasedPagination
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.support.ErrorResponse
import com.soundcloud.apipublic.support.PlaylistUrnUtil.getPlaylistUrn
import com.soundcloud.apipublic.support.TrackUrnUtil.getTrackUrn
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, Return, Try}

class RepostsHandler(userAuthentication: UserAuthentication, repostsService: RepostsService) {

  def createTracksRepost(request: HandlerRequest): Future[Response] =
    execute(request, getTrackUrn, repostsService.createTracksRepost)

  def deleteTracksRepost(request: HandlerRequest): Future[Response] =
    execute(request, getTrackUrn, repostsService.deleteTracksRepost)

  def getTracksReposters(request: HandlerRequest): Future[Response] =
    userAuthentication.withUserSession(request) { session =>
      val pagination = CursorBasedPagination.build(request)

      Try(getTrackUrn(request)) match {
        case Return(urn) =>
          repostsService
            .getTrackReposters(session, urn, pagination)
            .map {
              case Good(userCollection) => JsonResponseBuilder.ok(Collection.getRepresentation(userCollection, true))
              case Bad(com.soundcloud.jvmkit.module.outcome.NotFound(_)) => ErrorResponse.notFound()
              case Bad(_) => ErrorResponse(Status.InternalServerError)
            }
        case _ => Future.value(ErrorResponse.badRequest())
      }
    }

  def createPlaylistsRepost(request: HandlerRequest): Future[Response] =
    execute(request, getPlaylistUrn, repostsService.createPlaylistsRepost)

  def deletePlaylistsRepost(request: HandlerRequest): Future[Response] =
    execute(request, getPlaylistUrn, repostsService.deletePlaylistsRepost)

  def getPlaylistsReposters(request: HandlerRequest): Future[Response] =
    userAuthentication.withUserSession(request) { session =>
      val pagination = CursorBasedPagination.build(request)

      Try(getPlaylistUrn(request)) match {
        case Return(urn) =>
          repostsService
            .getPlaylistReposters(session, urn, pagination)
            .map(userCollection => {
              JsonResponseBuilder.ok(Collection.getRepresentation(userCollection, true))
            })
        case _ => Future.value(ErrorResponse.badRequest())
      }
    }

  private def execute(
      request: HandlerRequest,
      extractUrn: HandlerRequest => Urn,
      serviceCall: (UserSession, Urn) => Future[Result]
  ): Future[Response] =
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      Try(extractUrn(request)) match {
        case Return(urn) => serviceCall(session, urn).map(renderResult)
        case _ => Future.value(ErrorResponse.badRequest())
      }
    }

  private def renderResult(result: Result): Response = result match {
    case Created => ResponseBuilder.created()
    case Deleted => ResponseBuilder.ok()
    case AlreadyExists => ResponseBuilder.ok()
    case NotFound => ErrorResponse.notFound()
    case Forbidden => ErrorResponse.forbidden()
    case SpamBlocked => ErrorResponse(Status.TooManyRequests)
    case Failed => ErrorResponse(Status.InternalServerError)
  }
}
