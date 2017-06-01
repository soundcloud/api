package com.soundcloud.publicApiStrangler.client.playlists

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params, UrnParam}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.{AnonymousUserSession, UserSessionBuilder}
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.Json

import scala.util.control.NonFatal

class PlaylistsClient(moshimoshiClient: JsonClient) {
  def getPlaylistContainingTrackOwnedByUser(track: Urn, owner: Urn): Future[List[Playlist]] = {
    val path = Path() / "tracks" / track / "playlists"
    val params = Params("user_urn" -> UrnParam(owner))

    // Passing explicit values for all parameters in order to allow stubbing using Mockito.
    moshimoshiClient.getWithSession(PlaylistsClient.session, path, params, Headers.empty()).map { response: Response =>
      response.status match {
        case Status.Ok => Json.parse(response.contentString).as[List[Playlist]]
        case _ => List.empty[Playlist]
      }
    } handle { case NonFatal(_) => List.empty[Playlist] }
  }
}

object PlaylistsClient {
  val agent = Urn("soundcloud:system:public-api-strangler-playlists")

  val session: AnonymousUserSession = (new UserSessionBuilder).setAgent(agent).build.asInstanceOf[AnonymousUserSession]
}
