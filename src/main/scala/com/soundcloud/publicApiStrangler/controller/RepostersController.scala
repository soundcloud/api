package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.mapping.timeline.User
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.twitter.util.Future

class RepostersController(userAuthentication: UserAuthentication,
                          repostsClient: RepostsClient)
    extends BffInjectionBasedController {

  get("/e1/tracks/:id/reposters")(trackReposters)
  get("/e1/tracks/:id/reposters.json")(trackReposters)

  get("/e1/playlists/:id/reposters")(playlistReposters)
  get("/e1/playlists/:id/reposters.json")(playlistReposters)

  private def trackReposters(request: Request): Future[ResponseBuilder] =
    userAuthentication.withUserSession(request) { session =>
      repostsClient.trackReposters(
        session,
        new Urn(s"""soundcloud:tracks:${request.routeParams("id")}"""),
        baseUrl(request)).map(respond)
    }

  private def playlistReposters(request: Request): Future[ResponseBuilder] =
    userAuthentication.withUserSession(request) { session =>
      repostsClient.playlistReposters(
        session,
        new Urn(s"""soundcloud:playlists:${request.routeParams("id")}"""),
        baseUrl(request)).map(respond)
    }

  private def respond(users: List[User]): ResponseBuilder =
    render.json(users)

  private def baseUrl(request: Request): String = {
    // default means that request is coming from a dev environment
    val protocol = request.headerMap.getOrElse("X-Forwarded-Proto", "http")
    s"$protocol://${request.host.get}"
  }
}
