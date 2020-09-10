package com.soundcloud.publicApiStrangler.client.search

import com.soundcloud.jvmkit.module.http.client._
import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures.withContentsOf
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.libs.json.Json
import com.soundcloud.outcome._

class SearchClientSpec extends Specification with Mockito {

  trait Context extends Scope {
    val mockClient = smartMock[JsonClient]
    val client = new SearchClient(mockClient)
    val userSession = mock[UserSession]
  }

  "should search tracks" in new Context {
    val json = withContentsOf("search", "tracks").toString()
    val params = Params("q" -> "foo")
    val expected = Json.parse(json).as[SearchResponse]
    when(
      mockClient.getWithSession(
        userSession,
        Path() / "search" / "tracks",
        params,
        Headers.empty
      )
    ).thenReturn(Future.value(JsonResponseBuilder.ok(json)))
    val result = Await.result(client.searchTracks(userSession, params).value)
    result ==== expected.good
  }

  "should search playlists" in new Context {
    val json = withContentsOf("search", "tracks").toString()
    val params = Params("q" -> "foo")
    val expected = Json.parse(json).as[SearchResponse]
    when(
      mockClient.getWithSession(
        userSession,
        Path() / "search" / "playlists",
        params,
        Headers.empty
      )
    ).thenReturn(Future.value(JsonResponseBuilder.ok(json)))
    val result = Await.result(client.searchPlaylists(userSession, params).value)
    result ==== expected.good
  }
}
