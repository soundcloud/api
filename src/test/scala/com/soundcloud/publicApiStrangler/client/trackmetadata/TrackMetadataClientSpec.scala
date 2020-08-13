package com.soundcloud.publicApiStrangler.client.trackmetadata

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import play.api.libs.json.{JsNull, Json}

class TrackMetadataClientSpec extends UnitSpecification {
  trait Context extends Scope {
    val service = mock[JsonClient]
    val trackmetadataClient = new TrackmetadataClient(service)
  }

  "#urnsByUser" >> {
    trait UrnsByUserContext extends Context {
      val userUrn = Urn("soundcloud", "users", "1")
      val urn1 = Urn("soundcloud", "tracks", "1")
      val urn2 = Urn("soundcloud", "tracks", "2")

      val path = Path("/users") / userUrn / "tracks" / "urns"

      val jsonBody = Json.parse(s"""
           |{
           |  "data": ["$urn1", "$urn2"]
           |}
         """.stripMargin)
    }

    "200 status" in new UrnsByUserContext {
      when(service.getWithSession(anonymousSession, path, Params.empty, Headers.empty))
        .thenReturn(Future(jsonResponse(Status.Ok, jsonBody)))

      Await.result(trackmetadataClient.urnsByUser(anonymousSession, userUrn)) ==== List(urn1, urn2)
    }

    "500 status" in new UrnsByUserContext {
      when(service.getWithSession(anonymousSession, path, Params.empty, Headers.empty))
        .thenReturn(Future(jsonResponse(Status.InternalServerError, JsNull)))

      Await.result(trackmetadataClient.urnsByUser(anonymousSession, userUrn)) ==== List.empty
    }
  }
}
