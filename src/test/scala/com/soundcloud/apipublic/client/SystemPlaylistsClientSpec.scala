package com.soundcloud.apipublic.client

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.apipublic.mapper.similarcreators.SimilarCreatorsMapper
import com.soundcloud.apipublic.mapper.similarsounds.SimilarSoundsMapper
import com.soundcloud.apipublic.test.Helpers._
import com.soundcloud.apipublic.test.UnitSpecification
import com.twitter.util.Await
import com.soundcloud.apipublic.test.fixtures.Fixtures._

class SystemPlaylistsClientSpec extends UnitSpecification {
  trait Context extends Scope {
    implicit val service = mock[JsonClient]
    implicit val session = mock[UserSession]

    val client = new SystemPlaylistsClient(service)

    val path = Path() / "similar-sounds" / "get"
    val seed = Urn("soundcloud", "tracks", "1")
    val params = Params("track_urn" -> seed.toString)
  }

  trait SimilarCreatorsClientContext extends Scope {
    implicit val service = mock[JsonClient]
    implicit val session = mock[UserSession]

    val client = new SystemPlaylistsClient(service)

    val similarCreatorsPath = Path() / "similar-creators" / "get"
    val userSeed = Urn("soundcloud", "users", "1")
    val similarCreatorsParams = Params("user_urn" -> userSeed.toString, "page_size" -> "50")
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

  "#fetchSimilarCreators" >> {
    "responds with similar creators" in new SimilarCreatorsClientContext {
      expectOkResponse(similarCreatorsPath, similarCreatorsNonEmpty, similarCreatorsParams)
      Await.result(client.fetchSimilarCreators(session, userSeed, 50)) ==== Some(
        SimilarCreatorsMapper(similarCreatorsNonEmpty)
      )
    }

    "responds with none on backend 404" in new SimilarCreatorsClientContext {
      expectNotFoundResponse(similarCreatorsPath, similarCreatorsParams)
      Await.result(client.fetchSimilarCreators(session, userSeed, 50)) ==== None
    }

    "throws exception on bad request" in new SimilarCreatorsClientContext {
      expectBadRequestResponse(similarCreatorsPath, similarCreatorsParams)
      Await.result(client.fetchSimilarCreators(session, userSeed, 50)) must throwA[IllegalStateException]
    }

    "throws exception on internal error" in new SimilarCreatorsClientContext {
      expectInternalErrorResponse(similarCreatorsPath, similarCreatorsParams)
      Await.result(client.fetchSimilarCreators(session, userSeed, 50)) must throwA[IllegalStateException]
    }
  }
}
