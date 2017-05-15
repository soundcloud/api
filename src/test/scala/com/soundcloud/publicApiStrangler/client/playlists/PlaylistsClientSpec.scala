package com.soundcloud.publicApiStrangler.client.playlists

import com.soundcloud.jvmkit.module.http.client.Params
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.BigJvmKitConversions._
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.scalakit.finagle.http.{NotFoundStatus, OkStatus}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse}
import com.twitter.util.{Await, Future}
import play.api.libs.json.Json

class PlaylistsClientSpec extends UnitSpecification {

  trait Context extends Scope {
    val session = PlaylistsClient.session

    val moshimoshiClient = mock[JsonClient]
    val client = new PlaylistsClient(moshimoshiClient)

    val track = Urn("soundcloud:tracks:123")
    val owner = Urn("soundcloud:users:123")

    val path = Path() / "tracks" / track / "playlists"
    val params = Params("user_urn" -> owner)
  }

  trait WithServerResponse extends Context {
    val response = mock[JsonResponse]

    moshimoshiClient.get(session, path, params, Params.empty) returns (Future.value(response))
  }

  "When all is fine" in new WithServerResponse {
    val validPlaylistJson = Json.obj("secret_token" -> "s-whatever", "user" -> Json.obj("urn" -> "soundcloud:users:123"))

    response.status returns OkStatus
    response.body returns Json.arr(validPlaylistJson)

    Await.result(client.getPlaylistContainingTrackOwnedByUser(track, owner)) ==== List(Playlist(Urn("soundcloud:users:123"), "s-whatever"))
  }

  "OK Bad JSON response" in new WithServerResponse {
    val invalidPlaylistJson = Json.obj("user" -> Json.obj("urn" -> "soundcloud:users:123"))

    response.status returns OkStatus
    response.body returns Json.arr(invalidPlaylistJson)

    Await.result(client.getPlaylistContainingTrackOwnedByUser(track, owner)) ==== List.empty
  }

  "Error response" in new WithServerResponse {
    response.status returns NotFoundStatus

    Await.result(client.getPlaylistContainingTrackOwnedByUser(track, owner)) ==== List.empty
  }

  "transport error (or somesuch)" in new Context {
    moshimoshiClient.get(session, path, params, Params.empty) returns Future.exception(new RuntimeException)

    Await.result(client.getPlaylistContainingTrackOwnedByUser(track, owner)) ==== List.empty
  }
}
