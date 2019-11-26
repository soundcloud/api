package com.soundcloud.publicApiStrangler.client.playlists

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.support.{Bad, StringError, Good, Result}
import com.twitter.finagle.http.Status
import com.twitter.util.Future

import scala.util.control.NonFatal

class PlaylistDeletionClient(jsonClient: JsonClient) {
  def deletePlaylist(session: UserSession, urn: Urn): Future[Result[Status]] = {
    jsonClient
      .deleteWithSession(
        session,
        Path() / "playlists" / urn,
        Params.empty,
        Headers.empty,
        None
      )
      .map { response =>
        Good(response.status)
      } handle { case NonFatal(_) => Bad(StringError("Unhandled exception when deleting playlist.")) }
  }
}
