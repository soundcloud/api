package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.Urn
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.{InternalServerErrorStatus, OkStatus, StatusCode}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse}
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import play.api.libs.json.{JsArray, JsNull, JsValue, Json}

class RichOkidokiClientSpec extends UnitSpecification {
  trait GenericContext[T] extends Scope {
    def resultF: Future[T]
    def result = Await.result(resultF)
    def resultT = Await.result(resultF.liftToTry)
  }

  trait TrackAudioMetadataContext extends GenericContext[Option[TrackAudioMetadata]] {
    val jsonClient = mock[JsonClient]
    lazy val client = new RichOkidokiClient(jsonClient)

    def resultF = client.fetchTrackAudioMetadata(session, urn)

    lazy val path = Path() / "tracks" / urn / "audio"
    val session = anonymousSession
    val urn = Urn("soundcloud:tracks:123")

    def mockTrackAudioMetadata: TrackAudioMetadata = TrackAudioMetadata("finished", Some("vqf"), Some(9001))
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

    lazy val path = Path() / "tracks" / urn.getIdentifier / "domain_lockings"
    val session = anonymousSession
    val urn = Urn("soundcloud:tracks:123")

    def mockTrackDomainLockings: Seq[DomainLocking] = Seq(
      DomainLocking(
        domain = "example.com",
        urn = Urn("soundcloud:domain-lockings:1"),
        trackUrn = Urn("soundcloud:tracks:2")))
    def mockResponseContents: JsValue = JsArray(
      Seq(
        Json.obj(
          "domain" -> "example.com",
          "self" -> Json.obj(
            "urn" -> "soundcloud:domain-lockings:112358"
          ),
          "track_urn" -> "soundcloud:tracks:12")))
    def mockResponseStatus: StatusCode = OkStatus
    def mockResponse = Future.value(JsonResponse(mockResponseStatus, mockResponseContents))

    when(jsonClient.get(beTypedEqualTo(session), beTypedEqualTo(path), any, any))
      .thenReturn(mockResponse)
  }

  "track audio" >> {
    "200 response" in new TrackAudioMetadataContext {
      result ==== Some(TrackAudioMetadata("finished", Some("vqf"), Some(9001)))
    }

    "200 response with null values for size and format" in new TrackAudioMetadataContext {
      override def mockResponse = Future.value(JsonResponse(mockResponseStatus, Json.obj("state" -> "storing", "original_content_size" -> JsNull, "original_format" -> JsNull)))

      result.get.state ==== "storing"
      result.get.original_format ==== None
      result.get.original_content_size ==== None
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

  "track domain lockings" >> {
    "200 response" in new TrackDomainLockingsContext {
      result ==== Seq(DomainLocking(domain = "example.com", urn = Urn("soundcloud:domain-lockings:112358"), trackUrn = Urn("soundcloud:tracks:12")))
    }

    "404 response" in new TrackDomainLockingsContext {
      override def mockResponseStatus = InternalServerErrorStatus

      resultT.isThrow === true
    }

    "500 response" in new TrackDomainLockingsContext {
      override def mockResponseStatus = InternalServerErrorStatus

      resultT.isThrow === true
    }

    "exception response" in new TrackAudioMetadataContext {
      override def mockResponse = Future.exception(new RuntimeException("kaboom"))

      resultT.isThrow === true
    }
  }
}
