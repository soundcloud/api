package com.soundcloud.apipublic.client.playlists

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.apipublic.client.support.UnhandledResponseException
import com.twitter.finagle.http.Status
import com.twitter.util.Future

import scala.util.control.NonFatal

class PlaylistDeletionClient(jsonClient: JsonClient) {
  def deletePlaylist(session: UserSession, urn: Urn): Future[Outcome[Unit]] = {
    jsonClient
      .deleteWithSession(
        session,
        Path() / "playlists" / urn,
        Params.empty,
        Headers.empty,
        None
      )
      .map { response =>
        response.status match {
          case Status.Accepted | Status.Ok => Good(())
          case Status.NotFound | Status.Unauthorized => NotFound().bad
          case _ => throw UnhandledResponseException(response)
        }
      } handle {
      case NonFatal(_) => NotValid("Unhandled exception when deleting playlist.").bad
    }
  }
}
