package com.soundcloud.publicApiStrangler.client.playlists

import com.soundcloud.jvmkit.module.experimental.result._
import com.soundcloud.jvmkit.module.http.client.{HttpClient, HttpResponse, HttpStatus, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.util.{Future, NonFatal}

class PlaylistDeletionClient(jsonClient: HttpClient) {
  def deletePlaylist(session: UserSession, urn: Urn): Future[Result[HttpStatus]] = {
    jsonClient.deleteWithSession(
      session,
      Path() / "playlists" / urn,
      Params.empty,
      Headers.empty,
      None
    ).map {
      case HttpResponse(status, _, _) => Good(status)
    } handle { case NonFatal(e) => Bad(Error("Unhandled exception when deleting playlist.", e)) }
  }
}
