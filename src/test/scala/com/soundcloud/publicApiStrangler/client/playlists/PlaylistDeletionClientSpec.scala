package com.soundcloud.publicApiStrangler.client.playlists

import com.soundcloud.jvmkit.module.experimental.result.{Bad, Error, Good}
import com.soundcloud.jvmkit.module.http.client.{Headers, HttpClient, HttpResponse, OkHttpStatus, Params}
import com.soundcloud.jvmkit.module.util.{Path, Urn, UserSessionBuilder}
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.{Await, Future}

class PlaylistDeletionClientSpec extends UnitSpecification {
  trait Context extends Scope {
    val session = new UserSessionBuilder()
      .setUser(Urn("soundcloud:users:2"))
      .setAgent(Urn("soundcloud:applications:v2"))
      .build()

    val urn = Urn("soundcloud:playlists:123")

    val path = Path() / "playlists" / urn

    val httpClient = mock[HttpClient]
    val client = new PlaylistDeletionClient(httpClient)
  }

  "with response" in new Context {
    val response = mock[HttpResponse].status returns OkHttpStatus
    httpClient.deleteWithSession(session, path, Params.empty, Headers.empty, None) returns Future.value(response)

    Await.result(client.deletePlaylist(session, urn)) ==== Good(OkHttpStatus)
  }

  "with transport error" in new Context {
    val exception = new RuntimeException
    httpClient.deleteWithSession(session, path, Params.empty, Headers.empty, None) returns Future.exception(exception)

    Await.result(client.deletePlaylist(session, urn)) ==== Bad(Error("Unhandled exception when deleting playlist.", exception))
  }
}
