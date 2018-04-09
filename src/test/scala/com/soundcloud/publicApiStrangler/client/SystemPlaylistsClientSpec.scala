package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.mapper.similarsounds.SimilarSoundsMapper
import com.soundcloud.publicApiStrangler.test.Helpers._
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.Await
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._

class SystemPlaylistsClientSpec extends UnitSpecification {

  trait Context extends Scope {
    implicit val service = mock[JsonClient]
    implicit val session = mock[UserSession]

    val client = new SystemPlaylistsClient(service)

    val path = Path() / "similar-sounds" / "get"
    val seed = Urn("soundcloud:sounds:1")
    val params = Params("track_urn" -> seed.toString)
  }

  "#fetchSimilar" >> {

    "responds with similar tracks" in new Context {
      expectOkResponse(path, similarSoundsNonEmpty, params)
      Await.result(client.fetchSimilar(session, seed)) ==== Some(SimilarSoundsMapper(similarSoundsNonEmpty))
    }

    "uses default values for optional parameters" in new Context {
      expectOkResponse(path, similarSoundsNonEmpty, params)
      Await.result(client.fetchSimilar(session, seed)) ==== Some(SimilarSoundsMapper(similarSoundsNonEmpty))
    }

    "responds with none on backend 404" in new Context {
      expectNotFoundResponse(path, params)
      Await.result(client.fetchSimilar(session, seed)) ==== None
    }

    "throws exception on bad request" in new Context {
      expectBadRequestResponse(path, params)
      Await.result(client.fetchSimilar(session, seed)) must throwA[IllegalStateException]
    }

    "throws exception on internal error" in new Context {
      expectInternalErrorResponse(path, params)
      Await.result(client.fetchSimilar(session, seed)) must throwA[IllegalStateException]
    }

  }
}
