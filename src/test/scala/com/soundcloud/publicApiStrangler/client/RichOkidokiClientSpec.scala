package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.Urn
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.{InternalServerErrorStatus, OkStatus, StatusCode}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import play.api.libs.json.{JsArray, JsNull, JsValue, Json}

class RichOkidokiClientSpec extends UnitSpecification {
  trait GenericContext extends Scope {
    val session = anonymousSession

    val jsonClient = mock[JsonClient]
    lazy val client = new RichOkidokiClient(jsonClient)
  }

  "track audio" >> {
    trait TrackAudioMetadataContext extends GenericContext {
      def resultF = client.fetchTrackAudioMetadata(session, urn)
      def resultT = Await.result(resultF.liftToTry)
      def result = Await.result(resultF)


      lazy val path = Path() / "tracks" / urn / "audio"
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
    trait TrackDomainLockingsContext extends GenericContext {
      def resultF = client.fetchTrackDomainLockings(session, urn)
      def resultT = Await.result(resultF.liftToTry)
      def result = Await.result(resultF)

      lazy val path = Path() / "tracks" / urn.getIdentifier / "domain_lockings"
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

    "exception response" in new TrackDomainLockingsContext {
      override def mockResponse = Future.exception(new RuntimeException("kaboom"))

      resultT.isThrow === true
    }
  }

  "#fetchTrackGeoblockings" >> {
    trait GeoblockingsContext extends GenericContext {
      val path = Path() / "tracks" / "geo_blockings"

      val urns = Set(
        Urn("soundcloud:tracks:1"),
        Urn("soundcloud:tracks:2"),
        Urn("soundcloud:tracks:3"),
        Urn("soundcloud:tracks:4"))

      val (firstBatch, secondBatch) = urns.splitAt(2)

      val firstBatchJson = Json.parse(
        s"""
          |{
          |  "collection": [{
          |    "track_urn": "${firstBatch.head}",
          |    "geo_blockings": ["DE", "BR"]
          |  }, {
          |    "track_urn": "${firstBatch.last}",
          |    "geo_blockings": []
          |  }]
          |}
        """.stripMargin)

      val secondBatchJson = Json.parse(
        s"""
          |{
          |  "collection": [{
          |    "track_urn": "${secondBatch.head}",
          |    "geo_blockings": ["US", "UK"]
          |  }]
          |}
        """.stripMargin)
    }

    "200 response" in new GeoblockingsContext {
      when(jsonClient.get(session, path, Map("urns" -> firstBatch.toList), Params.empty))
          .thenReturn(Future.value(JsonResponse(OkStatus, firstBatchJson)))
      when(jsonClient.get(session, path, Map("urns" -> secondBatch.toList), Params.empty))
          .thenReturn(Future.value(JsonResponse(OkStatus, secondBatchJson)))

      val batchSize = 2
      Await.result(client.fetchTrackGeoblockings(session, urns, batchSize)) ==== Map(
        Urn("soundcloud:tracks:1") -> List("DE", "BR"),
        Urn("soundcloud:tracks:2") -> List.empty,
        Urn("soundcloud:tracks:3") -> List("US", "UK")
      )
    }

    "500 response"  in new GeoblockingsContext {
      when(jsonClient.get(session, path, Params("urns" -> urns.toList), Params.empty))
        .thenReturn(Future.value(JsonResponse(InternalServerErrorStatus, JsNull)))

      Await.result(client.fetchTrackGeoblockings(session, urns)) ==== Map.empty
    }
  }

  "#fetchTracksAudioMetadata" >> {
    trait GeoblockingsContext extends GenericContext {
      val path = Path() / "tracks" / "audio"

      val urns = Set(
        Urn("soundcloud:tracks:1"),
        Urn("soundcloud:tracks:2"),
        Urn("soundcloud:tracks:3"),
        Urn("soundcloud:tracks:4"))

      val (firstBatch, secondBatch) = urns.splitAt(2)

      val firstBatchJson = Json.parse(
        s"""
          |{
          |  "collection": [{
          |    "track_urn": "${firstBatch.head}",
          |    "state": "finished",
          |    "original_content_size": 4,
          |    "original_format": "mp3"
          |  }, {
          |    "track_urn": "${firstBatch.last}",
          |    "state": "failed",
          |    "original_content_size": null,
          |    "original_format": null
          |  }]
          |}
        """.stripMargin)

      val secondBatchJson = Json.parse(
        s"""
          |{
          |  "collection": [{
          |    "track_urn": "${secondBatch.head}",
          |    "state": "finished",
          |    "original_content_size": 5,
          |    "original_format": "ogg"
          |  }]
          |}
        """.stripMargin)
    }

    "200 response" in new GeoblockingsContext {
      when(jsonClient.get(session, path, Map("urns" -> firstBatch.toList), Params.empty))
          .thenReturn(Future.value(JsonResponse(OkStatus, firstBatchJson)))
      when(jsonClient.get(session, path, Map("urns" -> secondBatch.toList), Params.empty))
          .thenReturn(Future.value(JsonResponse(OkStatus, secondBatchJson)))

      val batchSize = 2
      Await.result(client.fetchTracksAudioMetadata(session, urns, batchSize)) ==== Map(
        Urn("soundcloud:tracks:1") -> TrackAudioMetadata("finished", Some("mp3"), Some(4)),
        Urn("soundcloud:tracks:2") -> TrackAudioMetadata("failed", None, None),
        Urn("soundcloud:tracks:3") -> TrackAudioMetadata("finished", Some("ogg"), Some(5))
      )
    }

    "500 response"  in new GeoblockingsContext {
      when(jsonClient.get(session, path, Params("urns" -> urns.toList), Params.empty))
        .thenReturn(Future.value(JsonResponse(InternalServerErrorStatus, JsNull)))

      Await.result(client.fetchTracksAudioMetadata(session, urns)) ==== Map.empty
    }
  }

  "#fetchTracksDomainLockings" >> {
    trait GeoblockingsContext extends GenericContext {
      val path = Path() / "domain_lockings"

      val urns = Set(
        Urn("soundcloud:tracks:1"),
        Urn("soundcloud:tracks:2"),
        Urn("soundcloud:tracks:3"),
        Urn("soundcloud:tracks:4"))

      val (firstBatch, secondBatch) = urns.splitAt(2)

      val firstBatchJson = Json.parse(
        s"""
          |[
          |  {
          |    "domain": "domain1",
          |    "track_urn": "${firstBatch.head}",
          |    "self": {
          |      "urn": "soundcloud:domain-lockings:1"
          |    }
          |  }, {
          |    "domain": "domain2",
          |    "track_urn": "${firstBatch.head}",
          |    "self": {
          |      "urn": "soundcloud:domain-lockings:2"
          |    }
          |  }, {
          |    "domain": "domain3",
          |    "track_urn": "${firstBatch.last}",
          |    "self": {
          |      "urn": "soundcloud:domain-lockings:3"
          |    }
          |  }
          |]
        """.stripMargin)

      val secondBatchJson = Json.parse(
        s"""
           |[
           |  {
           |    "domain": "domain4",
           |    "track_urn": "${secondBatch.head}",
           |    "self": {
           |      "urn": "soundcloud:domain-lockings:4"
           |    }
           |  }
           |]
        """.stripMargin)
    }

    "200 response" in new GeoblockingsContext {
      when(jsonClient.get(session, path, Map("track_ids" -> firstBatch.map(_.getIdentifier).toList), Params.empty))
          .thenReturn(Future.value(JsonResponse(OkStatus, firstBatchJson)))
      when(jsonClient.get(session, path, Map("track_ids" -> secondBatch.map(_.getIdentifier).toList), Params.empty))
          .thenReturn(Future.value(JsonResponse(OkStatus, secondBatchJson)))

      val batchSize = 2
      Await.result(client.fetchTracksDomainLockings(session, urns, batchSize)) ==== Map(
        Urn("soundcloud:tracks:1") -> List(DomainLocking("domain1", Urn("soundcloud:domain-lockings:1"), Urn("soundcloud:tracks:1")),
                                           DomainLocking("domain2", Urn("soundcloud:domain-lockings:2"), Urn("soundcloud:tracks:1"))),
        Urn("soundcloud:tracks:2") -> List(DomainLocking("domain3", Urn("soundcloud:domain-lockings:3"), Urn("soundcloud:tracks:2"))),
        Urn("soundcloud:tracks:3") -> List(DomainLocking("domain4", Urn("soundcloud:domain-lockings:4"), Urn("soundcloud:tracks:3")))
      )
    }

    "500 response"  in new GeoblockingsContext {
      when(jsonClient.get(session, path, Params("track_ids" -> urns.map(_.getIdentifier).toList), Params.empty))
        .thenReturn(Future.value(JsonResponse(InternalServerErrorStatus, JsNull)))

      Await.result(client.fetchTracksDomainLockings(session, urns)) ==== Map.empty
    }
  }

}
