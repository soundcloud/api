package com.soundcloud.publicApiStrangler.client.trackmetadata

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.chrono.ChronoResponse
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.finagle.http.{ParamMap, Status}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import play.api.libs.json.JsNull

class TrackMetadataClientSpec extends UnitSpecification {
  trait Context extends Scope {
    val service = mock[JsonClient]
    val trackmetadataClient = new TrackmetadataClient(service)
  }

  "#userTracks" >> {
    trait TracksByUser extends Context {
      val userUrn = Urn("soundcloud", "users", "1")
      val urn1 = Urn("soundcloud", "tracks", "1")
      val urn2 = Urn("soundcloud", "tracks", "2")

      val path = Path("/users") / userUrn / "tracks" / "chrono"
      val pagination = CursorBasedPagination(
        "https://api.soundcloud.com",
        "/users/1/tracks/",
        ParamMap(),
        Some("2"),
        2
      )

    }

    "200 status" in new TracksByUser {
      when(
        service.getWithSession(
          anonymousSession,
          path,
          Params("cursor" -> "2", "limit" -> "2", "direction" -> "desc"),
          Headers.empty
        )
      ).thenReturn(Future(jsonResponse(Status.Ok, trackmetadataClientTracks_chrono)))

      var result = Await.result(trackmetadataClient.userTracks(anonymousSession, userUrn, pagination))
      result.items must haveSize(2)
    }

    "500 status" in new TracksByUser {
      when(
        service.getWithSession(
          anonymousSession,
          path,
          Params("cursor" -> "2", "limit" -> "2", "direction" -> "desc"),
          Headers.empty
        )
      ).thenReturn(Future(jsonResponse(Status.InternalServerError, JsNull)))

      Await.result(trackmetadataClient.userTracks(anonymousSession, userUrn, pagination)) ==== ChronoResponse.emptyResponse
    }
  }
}
