package com.soundcloud.publicApiStrangler.client

import com.soundcloud.scalakit.finagle.http.{InternalServerErrorStatus, NotFoundStatus, OkStatus, StatusCode}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse}
import com.soundcloud.scalakit.test.UnitSpecification
import com.soundcloud.scalakit.{Path, Urn}
import com.twitter.util.{Await, Future}
import play.api.libs.json.Json
import org.mockito.Mockito._

class RichOkidokiClientSpec extends UnitSpecification {
  trait GenericContext[T] extends Scope {
    def resultF: Future[T]
    def result = Await.result(resultF)
    def resultT = Await.result(resultF.liftToTry)
  }

  trait TrackAudioMetadataContext extends GenericContext[TrackAudioMetadata] {
    val jsonClient = mock[JsonClient]
    lazy val client = new RichOkidokiClient(jsonClient)

    def resultF = client.fetchTrackAudioMetadata(session, urn)

    lazy val path = Path() / "tracks" / urn / "audio"
    val session = anonymousSession
    val urn = Urn("soundcloud:tracks:123")

    def mockTrackAudioMetadata: TrackAudioMetadata = TrackAudioMetadata("finished", "vqf", 9001)
    def mockResponseContents = Json.obj(
      "state" -> mockTrackAudioMetadata.state,
      "original_content_size" -> mockTrackAudioMetadata.original_content_size,
      "original_format" -> mockTrackAudioMetadata.original_format)
    def mockResponseStatus: StatusCode = OkStatus
    def mockResponse = Future.value(JsonResponse(mockResponseStatus, mockResponseContents))

    when(jsonClient.get(beTypedEqualTo(session), beTypedEqualTo(path), any, any))
      .thenReturn(mockResponse)
  }

  trait TrackDomainLockingsContext extends GenericContext[Seq[DomainLocking]] {
    val jsonClient = mock[JsonClient]
    lazy val client = new RichOkidokiClient(jsonClient)

    def resultF = client.fetchTrackDomainLockings(session, urn)

    val session = anonymousSession
    val urn = Urn("soundcloud:tracks:123")
  }

  "track audio" >> {
    "200 response" in new TrackAudioMetadataContext {
      result ==== TrackAudioMetadata("finished", "vqf", 9001)
    }

    "404 response" in new TrackAudioMetadataContext {
      override def mockResponseStatus = InternalServerErrorStatus

      resultT.isThrow === true
    }

    "500 response" in new TrackAudioMetadataContext {
      override def mockResponseStatus = InternalServerErrorStatus

      resultT.isThrow === true
    }

    "exception response" in new TrackAudioMetadataContext {
      override def mockResponse = Future.exception(new RuntimeException("kaboom"))

      resultT.isThrow === true
    }
  }

  // TODO: add domain locking tests
}
