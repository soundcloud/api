package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.jvmkit.ModuleConversions._
import com.soundcloud.jvmkit.Urn
import com.soundcloud.jvmkit.module.experimental.result.{Bad, Good}
import com.soundcloud.publicApiStrangler.client.playlists.PlaylistDeletionClient
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json.Json

/**
  * Overrides the public api endpoints for playlists and switches to
  * moshimoshi playlists controller.
  * Reason for overriding is to make use of a single mothership
  * controller to manage playlists (moshimoshi) to ease carving out a separate
  * playlists service in near future.
  */
class PlaylistsController(userAuthentication: UserAuthentication,
                          playlistDeletionClient: PlaylistDeletionClient,
                          mothershipDispatcher: DispatchToMothershipHandler)
  extends BffInjectionBasedController {

  post("/playlists")(mothershipDispatcher.dispatch(_))

  put("/playlists/:id")(mothershipDispatcher.dispatch(_))
  put("/playlists/:id.json")(mothershipDispatcher.dispatch(_))

  delete("/playlists/:id")(handleDelete)
  delete("/playlists/:id.json")(handleDelete)

  def handleDelete: (Request) => Future[ResponseBuilder] = {
    request =>
      userAuthentication.withLoggedInUser(request) { (session, _) =>
        playlistDeletionClient.deletePlaylist(session, playlistUrn(request)).map {
          case Good(status) => render.status(status.code).json(Json.obj("status" -> statusDescription(status)))
          case Bad(_) => render.internalServerError
        }
      }
  }

  private def playlistUrn(request: Request): Urn = {
    val IdParamPattern = "(\\d+)".r
    new Urn(request.routeParams("id") match {
      case IdParamPattern(id) => s"soundcloud:playlists:$id"
    })
  }

  private def statusDescription(status: Status): String = {
    s"${status.code} - ${com.twitter.finagle.http.Status(status.code).reason}"
  }
}
