package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient._
import com.soundcloud.publicApiStrangler.service.RepostsService
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.support.ErrorResponse
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future

class RepostsHandler(userAuthentication: UserAuthentication, repostsService: RepostsService) {

  def createTracksRepost(request: HandlerRequest) = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      val target = Urn("soundcloud", "tracks", request.routeParams("id"))
      repostsService.createTracksRepost(session, target).map(renderResult)
    }
  }

  def deleteTracksRepost(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      val target = Urn("soundcloud", "tracks", request.routeParams("id"))
      repostsService.deleteTracksRepost(session, target).map(renderResult)
    }
  }

  def getTracksReposters: HandlerRequest => Future[Response] = getReposters(_: HandlerRequest, "tracks")

  def createPlaylistsRepost(request: HandlerRequest) = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      val target = Urn("soundcloud", "playlists", request.routeParams("id"))
      repostsService.createPlaylistsRepost(session, target).map(renderResult)
    }
  }

  def deletePlaylistsRepost(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      val target = Urn("soundcloud", "playlists", request.routeParams("id"))
      repostsService.deletePlaylistsRepost(session, target).map(renderResult)
    }
  }

  def getPlaylistsReposters: HandlerRequest => Future[Response] = getReposters(_: HandlerRequest, "playlists")

  private def getReposters(request: HandlerRequest, targetType: String): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      val pagination = CursorBasedPagination.build(request)

      val target = Urn("soundcloud", targetType, request.routeParams("id"))

      repostsService
        .getReposters(session, target, pagination)
        .map(userCollection => {
          JsonResponseBuilder.ok(Collection.getRepresentation(userCollection, true))
        })
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
