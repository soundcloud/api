package com.soundcloud.publicApiStrangler.client.pubmese

import com.soundcloud.jvmkit.Urn
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.{InternalServerErrorStatus, NotFoundStatus, OkStatus, StatusCode}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import play.api.libs.json.Json

class PubmeseClientSpec extends UnitSpecification {
  trait GenericContext[T] extends Scope {
    def resultF: Future[T]
    def result = Await.result(resultF)
  }

  trait Context extends GenericContext[Option[Isrc]] {
    val jsonClient = mock[JsonClient]

    lazy val client = new PubmeseClient(jsonClient)

    def resultF = client.isrcForTrack(session, urn)

    lazy val path = Path() / "tracks" / urn
    val session = anonymousSession
    val urn = Urn("soundcloud:tracks:123")

    def mockIsrc: Option[Isrc] = Some(Isrc("15RC"))
    def mockResponseContents = Json.obj("isrc" -> mockIsrc.map(_.toString))
    def mockResponseStatus: StatusCode = OkStatus
    def mockResponse = Future.value(JsonResponse(mockResponseStatus, mockResponseContents))

    when(jsonClient.get(session, path, Params.empty, Params.empty))
      .thenReturn(mockResponse)
  }

  "track exists and has an ISRC" in new Context {
    result ==== Some(Isrc("15RC"))
  }

  "track exists but has no ISRC" in new Context {
    override def mockIsrc = None
    result ==== None
  }

  "track does not exist" in new Context {
    override def mockResponseStatus = NotFoundStatus
    result ==== None
  }

  "500 response" in new Context {
    override def mockResponseStatus = InternalServerErrorStatus
    result ==== None
  }

  "exception response" in new Context {
    override def mockResponse = Future.exception(new RuntimeException("kaboom"))
    result ==== None
  }
}
