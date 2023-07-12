package com.soundcloud.apipublic.client.search

import com.soundcloud.jvmkit.module.http.client._
import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.test.fixtures.Fixtures.withContentsOf
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.libs.json.Json
import com.soundcloud.jvmkit.module.outcome._
import QueryMappers._
import com.soundcloud.apipublic.handler.support.requestParser.{AccessParams, AccessParamsExtractor}
import com.twitter.finagle.http.ParamMap

class SearchDispatcherClientSpec extends Specification with Mockito {
  import com.soundcloud.apipublic.handler.search.ParamsExtractor._

  trait Context extends Scope {
    val mockClient = smartMock[JsonClient]
    val client = new SearchDispatcherClient(mockClient)
    val userSession = mock[UserSession]
  }

  "should search tracks" in new Context {
    val json = withContentsOf("search", "tracks").toString()
    val rawParams = ParamMap("q" -> "foo")
    val params = rawParams.asTracksParams
    val expected = Json.parse(json).as[SearchResponse]
    val access: AccessParams = AccessParamsExtractor.unapply(rawParams)

    when(mockClient.getWithSession(userSession, Path() / "search" / "tracks", params.toParams, Headers.empty))
      .thenReturn(Future.value(JsonResponseBuilder.ok(json)))
    when(
      mockClient.getWithSession(
        userSession,
        Path() / "search" / "tracks",
        params.toParams,
        Headers.empty
      )
    ).thenReturn(Future.value(JsonResponseBuilder.ok(json)))
    val result = Await.result(client.searchTracks(session = userSession, params = params, access = access).value)
    result ==== expected.good
  }

  "should search playlists" in new Context {
    val json = withContentsOf("search", "tracks").toString()
    val rawParams = ParamMap("q" -> "foo")
    val params = rawParams.asPlaylistParams
    val expected = Json.parse(json).as[SearchResponse]
    val access: AccessParams = AccessParamsExtractor.unapply(rawParams)
    when(
      mockClient.getWithSession(
        userSession,
        Path() / "search" / "playlists",
        params.toParams,
        Headers.empty
      )
    ).thenReturn(Future.value(JsonResponseBuilder.ok(json)))
    val result = Await.result(client.searchPlaylists(session = userSession, params = params, access = access).value)
    result ==== expected.good
  }
}
