package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.experimental.result.{Bad, Good}
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.playlists.PlaylistDeletionClient
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.Json

/**
  * Overrides the public api endpoints for playlists and switches to
  * moshimoshi playlists controller.
  * Reason for overriding is to make use of a single mothership
  * controller to manage playlists (moshimoshi) to ease carving out a separate
  * playlists service in near future.
  */
class PlaylistsHandler(userAuthentication: UserAuthentication, playlistDeletionClient: PlaylistDeletionClient) {

  def handleDelete(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      playlistDeletionClient.deletePlaylist(session, playlistUrn(request)).map {
        case Good(status) => ResponseBuilder(status = status, body = Json.stringify(Json.obj("status" -> statusDescription(status)))).build
        case Bad(_) => ResponseBuilder.internalServerError()
      }
    }
  }

  private def playlistUrn(request: HandlerRequest): Urn = {
    val IdParamPattern = "(\\d+)".r
    new Urn(request.routeParams("id") match {
      case IdParamPattern(id) => s"soundcloud:playlists:$id"
    })
  }

  private def statusDescription(status: Status): String = {
    s"${status.code} - ${com.twitter.finagle.http.Status(status.code).reason}"
  }
}
