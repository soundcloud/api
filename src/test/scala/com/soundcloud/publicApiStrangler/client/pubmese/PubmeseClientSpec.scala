package com.soundcloud.publicApiStrangler.client.pubmese

import com.soundcloud.jvmkit.Urn
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.{InternalServerErrorStatus, NotFoundStatus, OkStatus}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.{Await, Future}
import play.api.libs.json.Json

class PubmeseClientSpec extends UnitSpecification {
  trait Context extends Scope {
    val jsonClient = mock[JsonClient]

    lazy val client = new PubmeseClient(jsonClient)

    lazy val path = Path() / "tracks" / urn
    val session = anonymousSession
    val urn = Urn("soundcloud:tracks:123")
  }

  "track exists and has an ISRC" in new Context {
    jsonClient.get(session, path, Params.empty, Params.empty) returns
      Future.value(JsonResponse(OkStatus, Json.obj("isrc" -> "15RC")))

    Await.result(client.isrcForTrack(session, urn)) ==== Some(Isrc("15RC"))
  }

  "track exists but has no ISRC" in new Context {
    jsonClient.get(session, path, Params.empty, Params.empty) returns
      Future.value(JsonResponse(OkStatus, Json.obj()))

    Await.result(client.isrcForTrack(session, urn)) ==== None
  }

  "track does not exist" in new Context {
    jsonClient.get(session, path, Params.empty, Params.empty) returns
      Future.value(JsonResponse(NotFoundStatus, Json.obj()))

    Await.result(client.isrcForTrack(session, urn)) ==== None
  }

  "500 response" in new Context {
    jsonClient.get(session, path, Params.empty, Params.empty) returns
      Future.value(JsonResponse(InternalServerErrorStatus, Json.obj()))

    Await.result(client.isrcForTrack(session, urn)) ==== None
  }

  "exception response" in new Context {
    jsonClient.get(session, path, Params.empty, Params.empty) returns
      Future.exception(new RuntimeException("kaboom"))

    Await.result(client.isrcForTrack(session, urn)) ==== None
  }
}
