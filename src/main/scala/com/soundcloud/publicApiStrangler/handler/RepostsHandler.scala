package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient._
import com.soundcloud.publicApiStrangler.service.RepostsService
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.support.ErrorResponse
import com.soundcloud.publicApiStrangler.support.PlaylistUrnUtil.getPlaylistUrn
import com.soundcloud.publicApiStrangler.support.TrackUrnUtil.getTrackUrn
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, Return, Try}

class RepostsHandler(userAuthentication: UserAuthentication, repostsService: RepostsService) {

  def createTracksRepost(request: HandlerRequest): Future[Response] =
    execute(request, getTrackUrn, repostsService.createTracksRepost)

  def deleteTracksRepost(request: HandlerRequest): Future[Response] =
    execute(request, getTrackUrn, repostsService.deleteTracksRepost)

  def getTracksReposters(request: HandlerRequest): Future[Response] = executeGet(request, getTrackUrn)

  def createPlaylistsRepost(request: HandlerRequest): Future[Response] =
    execute(request, getPlaylistUrn, repostsService.createPlaylistsRepost)

  def deletePlaylistsRepost(request: HandlerRequest): Future[Response] =
    execute(request, getPlaylistUrn, repostsService.deletePlaylistsRepost)

  def getPlaylistsReposters(request: HandlerRequest): Future[Response] = executeGet(request, getPlaylistUrn)

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

  private def executeGet(request: HandlerRequest, extractUrn: HandlerRequest => Urn): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      val pagination = CursorBasedPagination.build(request)

      Try(extractUrn(request)) match {
        case Return(urn) =>
          repostsService
            .getReposters(session, urn, pagination)
            .map(userCollection => {
              JsonResponseBuilder.ok(Collection.getRepresentation(userCollection, true))
            })
        case _ => Future.value(ErrorResponse.badRequest())
      }
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
