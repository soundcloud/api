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
  def createTracksRepost = createRepost(_: HandlerRequest, "tracks")

  def deleteTracksRepost = deleteRepost(_: HandlerRequest, "tracks")

  def getTracksReposters: HandlerRequest => Future[Response] = getReposters(_: HandlerRequest, "tracks")

  def createPlaylistsRepost = createRepost(_: HandlerRequest, "playlists")

  def deletePlaylistsRepost = deleteRepost(_: HandlerRequest, "playlists")

  def getPlaylistsReposters: HandlerRequest => Future[Response] = getReposters(_: HandlerRequest, "playlists")

  private def createRepost(request: HandlerRequest, targetType: String): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      val target = Urn("soundcloud", targetType, request.routeParams("id"))
      repostsService.createRepost(session, target, baseUrl(request)).map(renderResult)
    }
  }

  private def deleteRepost(request: HandlerRequest, targetType: String): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      val target = Urn("soundcloud", targetType, request.routeParams("id"))
      repostsService.deleteRepost(session, target, baseUrl(request)).map(renderResult)
    }
  }

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

  private def baseUrl(request: HandlerRequest): String = {
    // default means that request is coming from a dev environment
    val protocol = request.headerMap.getOrElse("X-Forwarded-Proto", "http")
    s"$protocol://${request.host.get}"
  }

  private def renderResult(result: Result): Response = result match {
    case Created => ResponseBuilder.created()
    case Deleted => ResponseBuilder.ok()
    case AlreadyExists => ResponseBuilder.ok()
    case NotFound => ErrorResponse.notFound()
    case SpamBlocked => ErrorResponse(Status.TooManyRequests)
    case Failed => ErrorResponse(Status.InternalServerError)
  }
}
