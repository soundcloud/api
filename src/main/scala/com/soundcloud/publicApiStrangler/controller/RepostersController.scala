package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.web.BffInjectionBasedController
import com.soundcloud.publicApiStrangler.mapping.timeline.User
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.twitter.util.Future

class RepostersController(repostsClient: RepostsClient) extends BffInjectionBasedController {

  get("/e1/tracks/:id/reposters")(trackReposters)
  get("/e1/tracks/:id/reposters.json")(trackReposters)

  get("/e1/playlists/:id/reposters")(playlistReposters)
  get("/e1/playlists/:id/reposters.json")(playlistReposters)

  private def trackReposters(request: Request): Future[ResponseBuilder] = ???
  private def playlistReposters(request: Request): Future[ResponseBuilder] = ???

  private def respond(users: List[User]): ResponseBuilder =
    render.json(users)
}
