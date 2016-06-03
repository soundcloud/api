package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.Future
import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.Urn
import com.soundcloud.service.client.OkidokiClient
import com.soundcloud.service.response.representation.{ForbiddenDeletePlaylistResponse, InvalidUrnDeletePlaylistResponse, NotAuthorizedDeletePlaylistResponse, OkDeletePlaylistResponse}
import play.api.libs.json.Json

/**
  * Overrides the public api endpoints for playlists and switches to
  * moshimoshi playlists controller.
  * Reason for overriding is to make use of a single mothership
  * controller to manage playlists (moshimoshi) to ease carving out a separate
  * playlists service in near future.
  */
class PlaylistsController(userAuthentication: UserAuthentication,
                          okidokiClient: OkidokiClient,
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
        okidokiClient.deletePlaylist(session, playlistUrn(request)).map {
          case OkDeletePlaylistResponse => render.ok.typedJson(Json.obj("status" -> "200 - OK"))
          case NotAuthorizedDeletePlaylistResponse => render.unauthorized
          case ForbiddenDeletePlaylistResponse => render.forbidden
          case InvalidUrnDeletePlaylistResponse => render.notFound
          case _ => render.internalServerError
        }
      }
  }

  private def playlistUrn(request: Request): Urn = {
    val IdParamPattern = "(\\d+)".r
    Urn(request.routeParams("id") match {
      case IdParamPattern(id) => s"soundcloud:playlists:$id"
    })
  }
}
