package com.soundcloud.publicApiStrangler.client.pubmese

import com.soundcloud.jvmkit.Urn
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.{InternalServerErrorStatus, NotFoundStatus, OkStatus, StatusCode}
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

    def mockIsrc: Option[Isrc] = Some(Isrc("15RC"))
    def mockResponseStatus: StatusCode = OkStatus
    def mockResponse = Future.value(JsonResponse(mockResponseStatus, Json.obj("isrc" -> mockIsrc.map(_.toString))))

    jsonClient.get(session, path, Params.empty, Params.empty) returns mockResponse
  }

  "track exists and has an ISRC" in new Context {
    Await.result(client.isrcForTrack(session, urn)) ==== Some(Isrc("15RC"))
  }

  "track exists but has no ISRC" in new Context {
    override def mockIsrc = None
    Await.result(client.isrcForTrack(session, urn)) ==== None
  }

  "track does not exist" in new Context {
    override def mockResponseStatus = NotFoundStatus
    Await.result(client.isrcForTrack(session, urn)) ==== None
  }

  "500 response" in new Context {
    override def mockResponseStatus = InternalServerErrorStatus
    Await.result(client.isrcForTrack(session, urn)) ==== None
  }

  "exception response" in new Context {
    override def mockResponse = Future.exception(new RuntimeException("kaboom"))
    Await.result(client.isrcForTrack(session, urn)) ==== None
  }
}
