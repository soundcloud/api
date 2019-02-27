package com.soundcloud.publicApiStrangler.client.playlists

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.support.{Bad, StringError, Good}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Await, Future}

class PlaylistDeletionClientSpec extends UnitSpecification {

  trait Context extends Scope {
    val session = new UserSessionBuilder()
      .setUser(Urn("soundcloud", "users", "2"))
      .setAgent(Urn("soundcloud", "applications", "v2"))
      .build()

    val urn = Urn("soundcloud", "playlists", "123")

    val path = Path() / "playlists" / urn

    val jsonClient = mock[JsonClient]
    val client = new PlaylistDeletionClient(jsonClient)
  }

  "with response" in new Context {
    val response = mock[Response].status returns Status.Ok
    jsonClient.deleteWithSession(session, path, Params.empty, Headers.empty, None) returns Future.value(response)

    Await.result(client.deletePlaylist(session, urn)) ==== Good(Status.Ok)
  }

  "with transport error" in new Context {
    jsonClient.deleteWithSession(session, path, Params.empty, Headers.empty, None) returns Future.exception(new RuntimeException)

    Await.result(client.deletePlaylist(session, urn)) ==== Bad(StringError("Unhandled exception when deleting playlist."))
  }
}
