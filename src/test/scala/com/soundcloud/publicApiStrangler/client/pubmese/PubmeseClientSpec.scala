package com.soundcloud.publicApiStrangler.client.pubmese

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}
import play.api.libs.json.{JsNull, Json}
import com.soundcloud.publicApiStrangler.client.CommonJsonFormats.{urnFormat, urnSetReads}

class PubmeseClientSpec extends UnitSpecification {

  trait Context extends Scope {
    val jsonClient = mock[JsonClient]

    lazy val client = new PubmeseClient(jsonClient)

    val path = Path() / "tracks"

    val session = anonymousSession
    val urn1 = Urn("soundcloud:tracks:123")
    val urn2 = Urn("soundcloud:tracks:456")

    val urns = Set(urn1, urn2)

    def stubbedRequestFor(urns: Set[Urn]) =
      jsonClient.postWithSession(session, path, Params.empty, Headers.empty, requestBodyFor(urns))

    private def requestBodyFor(urns: Set[Urn]) = Some(Json.obj("track_urns" -> urns).toString())
  }

  "single track" >> {
    "track exists and has an ISRC" in new Context {
      stubbedRequestFor(Set(urn1)) returns
        Future.value(jsonResponse(Status.Ok,
          Json.arr(Json.obj("track_urn" -> urn1, "isrc" -> "15RC"))
        ))

      Await.result(client.isrcForTrack(session, urn1)) ==== Some(Isrc("15RC"))
    }

    "track exists but has no ISRC" in new Context {
      stubbedRequestFor(Set(urn1)) returns
        Future.value(jsonResponse(Status.Ok,
          Json.arr(Json.obj("track_urn" -> urn1, "isrc" -> JsNull))
        ))

      Await.result(client.isrcForTrack(session, urn1)) ==== None
    }

    "track does not exist" in new Context {
      stubbedRequestFor(Set(urn1)) returns
        Future.value(jsonResponse(Status.NotFound,
          Json.obj()
        ))

      Await.result(client.isrcForTrack(session, urn1)) ==== None
    }
  }

  "multiple tracks" >> {
    "track exists and has an ISRC" in new Context {
      stubbedRequestFor(urns) returns
        Future.value(jsonResponse(Status.Ok, Json.arr(
          Json.obj("track_urn" -> (urn1), "isrc" -> "15RC"),
          Json.obj("track_urn" -> (urn2), "isrc" -> "15RC2"))
        ))

      val result = Await.result(client.isrcsForTracks(session, urns))
      result.get(urn1) ==== Some(Isrc("15RC"))
      result.get(urn2) ==== Some(Isrc("15RC2"))
    }

    "track exists but has no ISRC" in new Context {
      stubbedRequestFor(urns) returns
        Future.value(jsonResponse(Status.Ok, Json.arr(
          Json.obj("track_urn" -> (urn1), "isrc" -> JsNull),
          Json.obj("track_urn" -> (urn2), "isrc" -> "15RC2"))
        ))

      Await.result(client.isrcsForTracks(session, urns)).get(urn1) ==== None
    }

    "track does not exist" in new Context {
      stubbedRequestFor(urns) returns
        Future.value(jsonResponse(Status.Ok, Json.arr(
          Json.obj("track_urn" -> (urn2), "isrc" -> "15RC2"))
        ))

      Await.result(client.isrcsForTracks(session, urns)).get(urn1) ==== None
    }

    "500 response" in new Context {
      stubbedRequestFor(urns) returns
        Future.value(jsonResponse(Status.InternalServerError,
          Json.obj()
        ))

      Await.result(client.isrcsForTracks(session, urns)).get(urn1) ==== None
    }

    "exception response" in new Context {
      stubbedRequestFor(urns) returns
        Future.exception(new RuntimeException("kaboom"))

      Await.result(client.isrcsForTracks(session, urns)).get(urn1) ==== None
    }
  }
}
