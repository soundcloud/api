package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient._
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future

class RepostsHandler(userAuthentication: UserAuthentication, repostsClient: RepostsClient) {
  def createTracksRepost = createRepost(_: HandlerRequest, "tracks")

  def deleteTracksRepost = deleteRepost(_: HandlerRequest, "tracks")

  def createPlaylistsRepost = createRepost(_: HandlerRequest, "playlists")

  def deletePlaylistsRepost = deleteRepost(_: HandlerRequest, "playlists")

  private def createRepost(request: HandlerRequest, targetType: String): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, userUrn) =>
      val target = Urn("soundcloud", targetType, request.routeParams("id"))
      repostsClient.createRepost(session, target, baseUrl(request)).map(renderResult)
    }
  }

  private def deleteRepost(request: HandlerRequest, targetType: String): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, userUrn) =>
      val target = Urn("soundcloud", targetType, request.routeParams("id"))
      repostsClient.deleteRepost(session, target, baseUrl(request)).map(renderResult)
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
    case NotFound => ResponseBuilder.notFound()
    case SpamBlocked => JsonResponseBuilder(status = Status.TooManyRequests).build
    case Failed => ResponseBuilder.internalServerError()
  }
}
