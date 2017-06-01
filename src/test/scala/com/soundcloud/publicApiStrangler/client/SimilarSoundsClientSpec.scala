package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.mapper.similarsounds.SimilarSoundsMapper
import com.soundcloud.publicApiStrangler.test.Helpers._
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.Await
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._

class SimilarSoundsClientSpec extends UnitSpecification {

  trait Context extends Scope {
    implicit val service = mock[JsonClient]
    implicit val session = mock[UserSession]

    val client = new SimilarSoundsClient(service)

    def path = Path() / "similar-to" / Urn("soundcloud:sounds:1")
  }

  "#similarSounds" >> {

    "responds with similar tracks" in new Context {
      expectOkResponse(path, similarSoundsNonEmpty, Params("page" -> "2", "page_size" -> "20", "variant" -> "foo-variant", "query_urn" -> "soundcloud:similar-sounds:123"))
      Await.result(client.fetchSimilar(session, Urn("soundcloud:sounds:1"), 2, 20, "foo-variant", Some(Urn("soundcloud:similar-sounds:123")))) ==== Some(SimilarSoundsMapper(similarSoundsNonEmpty))
    }

    "uses default values for optional parameters" in new Context {
      expectOkResponse(path, similarSoundsNonEmpty, Params("page" -> "1", "page_size" -> "10", "variant" -> "", "query_urn" -> ""))
      Await.result(client.fetchSimilar(session, Urn("soundcloud:sounds:1"))) ==== Some(SimilarSoundsMapper(similarSoundsNonEmpty))
    }

    "responds with none on backend 404" in new Context {
      expectNotFoundResponse(path, Params("page" -> "1", "page_size" -> "10", "variant" -> "", "query_urn" -> ""))
      Await.result(client.fetchSimilar(session, Urn("soundcloud:sounds:1"))) ==== None
    }

    "throws exception on bad request" in new Context {
      expectBadRequestResponse(path, Params("page" -> "1", "page_size" -> "10", "variant" -> "", "query_urn" -> ""))
      Await.result(client.fetchSimilar(session, Urn("soundcloud:sounds:1"))) must throwA[IllegalStateException]
    }

    "throws exception on internal error" in new Context {
      expectInternalErrorResponse(path, Params("page" -> "1", "page_size" -> "10", "variant" -> "", "query_urn" -> ""))
      Await.result(client.fetchSimilar(session, Urn("soundcloud:sounds:1"))) must throwA[IllegalStateException]
    }

  }
}
