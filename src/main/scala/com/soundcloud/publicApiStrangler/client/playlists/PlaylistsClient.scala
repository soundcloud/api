package com.soundcloud.publicApiStrangler.client.playlists

import com.soundcloud.jvmkit.{AnonymousUserSession, Urn, UserSessionBuilder}
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.twitter.util.{Future, NonFatal}

class PlaylistsClient(moshimoshiClient: JsonClient) {
  def getPlaylistContainingTrackOwnedByUser(track: Urn, owner: Urn): Future[List[Playlist]] = {
    val path = Path() / "tracks" / track / "playlists"
    val params = Params("user_urn" -> owner)

    // Passing explicit values for all parameters in order to allow stubbing using Mockito.
    moshimoshiClient.get(PlaylistsClient.session, path, params, Params.empty).map {
      case JsonResponse(OkStatus, body, _, _) => body.as[List[Playlist]]
      case _ => List.empty[Playlist]
    } handle { case NonFatal(_) => List.empty[Playlist] }
  }
}

object PlaylistsClient {
  val agent = Urn("soundcloud:system:public-api-strangler-playlists")

  val session: AnonymousUserSession = (new UserSessionBuilder).setAgent(agent).build.asInstanceOf[AnonymousUserSession]
}
