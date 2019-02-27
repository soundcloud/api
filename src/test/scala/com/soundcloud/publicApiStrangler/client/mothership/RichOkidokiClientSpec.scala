package com.soundcloud.publicApiStrangler.client.mothership

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.User
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.Status
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
      val urn = Urn("soundcloud", "tracks", "123")

      def mockTrackAudioMetadata: TrackAudioMetadata = TrackAudioMetadata("finished", Some("vqf"), Some(9001))

      def mockResponseContents = Json.obj(
        "state" -> mockTrackAudioMetadata.state,
        "original_content_size" -> mockTrackAudioMetadata.original_content_size,
        "original_format" -> mockTrackAudioMetadata.original_format)

      def mockResponseStatus: Status = Status.Ok

      def mockResponse = Future.value(jsonResponse(mockResponseStatus, mockResponseContents))

      when(jsonClient.getWithSession(session, path, Params.empty, Headers.empty)).thenReturn(mockResponse)
    }

    "200 response" in new TrackAudioMetadataContext {
      result ==== Some(TrackAudioMetadata("finished", Some("vqf"), Some(9001)))
    }

    "200 response with null values for size and format" in new TrackAudioMetadataContext {
      override def mockResponse = Future.value(jsonResponse(mockResponseStatus, Json.obj("state" -> "storing", "original_content_size" -> JsNull, "original_format" -> JsNull)))

      result.get.state ==== "storing"
      result.get.original_format ==== None
      result.get.original_content_size ==== None
    }

    "404 response" in new TrackAudioMetadataContext {
      override def mockResponseStatus = Status.InternalServerError

      resultT.isThrow === true
    }

    "500 response" in new TrackAudioMetadataContext {
      override def mockResponseStatus = Status.InternalServerError

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

      lazy val path = Path() / "tracks" / urn.identifier / "domain_lockings"
      val urn = Urn("soundcloud", "tracks", "123")

      def mockTrackDomainLockings: Seq[DomainLocking] = Seq(
        DomainLocking(
          domain = "example.com",
          urn = Urn("soundcloud", "domain-lockings", "1"),
          trackUrn = Urn("soundcloud", "tracks", "2")))

      def mockResponseContents: JsValue = JsArray(
        Seq(
          Json.obj(
            "domain" -> "example.com",
            "self" -> Json.obj(
              "urn" -> "soundcloud:domain-lockings:112358"
            ),
            "track_urn" -> "soundcloud:tracks:12")))

      def mockResponseStatus: Status = Status.Ok

      def mockResponse = Future.value(jsonResponse(mockResponseStatus, mockResponseContents))

      when(jsonClient.getWithSession(beTypedEqualTo(session), beTypedEqualTo(path), any, any))
        .thenReturn(mockResponse)
    }

    "200 response" in new TrackDomainLockingsContext {
      result ==== Seq(DomainLocking(domain = "example.com", urn = Urn("soundcloud", "domain-lockings", "112358"), trackUrn = Urn("soundcloud", "tracks", "12")))
    }

    "404 response" in new TrackDomainLockingsContext {
      override def mockResponseStatus = Status.InternalServerError

      resultT.isThrow === true
    }

    "500 response" in new TrackDomainLockingsContext {
      override def mockResponseStatus = Status.InternalServerError

      resultT.isThrow === true
    }

    "exception response" in new TrackDomainLockingsContext {
      override def mockResponse = Future.exception(new RuntimeException("kaboom"))

      resultT.isThrow === true
    }
  }

  "#fetchTrackGeoblockings" >> {
    trait GeoblockingsContext extends GenericContext {
      val path = Path() / "tracks" / "geo_blockings" / ""

      val urns = Set(
        Urn("soundcloud", "tracks", "1"),
        Urn("soundcloud", "tracks", "2"),
        Urn("soundcloud", "tracks", "3"),
        Urn("soundcloud", "tracks", "4"))

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
      when(jsonClient.getWithSession(session, path, Map("urns" -> firstBatch.mkString(",")), Headers.empty))
        .thenReturn(Future.value(jsonResponse(Status.Ok, firstBatchJson)))
      when(jsonClient.getWithSession(session, path, Map("urns" -> secondBatch.mkString(",")), Headers.empty))
        .thenReturn(Future.value(jsonResponse(Status.Ok, secondBatchJson)))

      val batchSize = 2
      Await.result(client.fetchTrackGeoblockings(session, urns, batchSize)) ==== Map(
        Urn("soundcloud", "tracks", "1") -> List("DE", "BR"),
        Urn("soundcloud", "tracks", "2") -> List.empty,
        Urn("soundcloud", "tracks", "3") -> List("US", "UK")
      )
    }

    "500 response" in new GeoblockingsContext {
      when(jsonClient.getWithSession(session, path, Params("urns" -> urns.mkString(",")), Headers.empty))
        .thenReturn(Future.value(jsonResponse(Status.InternalServerError, JsNull)))

      Await.result(client.fetchTrackGeoblockings(session, urns)) ==== Map.empty
    }
  }

  "#fetchTracksAudioMetadata" >> {
    trait GeoblockingsContext extends GenericContext {
      val path = Path() / "tracks" / "audio" / ""

      val urns = Set(
        Urn("soundcloud", "tracks", "1"),
        Urn("soundcloud", "tracks", "2"),
        Urn("soundcloud", "tracks", "3"),
        Urn("soundcloud", "tracks", "4"))

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
      when(jsonClient.getWithSession(session, path, Map("urns" -> firstBatch.mkString(",")), Headers.empty))
        .thenReturn(Future.value(jsonResponse(Status.Ok, firstBatchJson)))
      when(jsonClient.getWithSession(session, path, Map("urns" -> secondBatch.mkString(",")), Headers.empty))
        .thenReturn(Future.value(jsonResponse(Status.Ok, secondBatchJson)))

      val batchSize = 2
      Await.result(client.fetchTracksAudioMetadata(session, urns, batchSize)) ==== Map(
        Urn("soundcloud", "tracks", "1") -> TrackAudioMetadata("finished", Some("mp3"), Some(4)),
        Urn("soundcloud", "tracks", "2") -> TrackAudioMetadata("failed", None, None),
        Urn("soundcloud", "tracks", "3") -> TrackAudioMetadata("finished", Some("ogg"), Some(5))
      )
    }

    "500 response" in new GeoblockingsContext {
      when(jsonClient.getWithSession(session, path, Params("urns" -> urns.mkString(",")), Headers.empty))
        .thenReturn(Future.value(jsonResponse(Status.InternalServerError, JsNull)))

      Await.result(client.fetchTracksAudioMetadata(session, urns)) ==== Map.empty
    }
  }

  "#fetchTracksDomainLockings" >> {
    trait GeoblockingsContext extends GenericContext {
      val path = Path() / "domain_lockings"

      val urns = Set(
        Urn("soundcloud", "tracks", "1"),
        Urn("soundcloud", "tracks", "2"),
        Urn("soundcloud", "tracks", "3"),
        Urn("soundcloud", "tracks", "4"))

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
      when(jsonClient.getWithSession(session, path, Map("track_ids" -> firstBatch.map(_.getIdentifier).mkString(",")), Headers.empty))
        .thenReturn(Future.value(jsonResponse(Status.Ok, firstBatchJson)))
      when(jsonClient.getWithSession(session, path, Map("track_ids" -> secondBatch.map(_.getIdentifier).mkString(",")), Headers.empty))
        .thenReturn(Future.value(jsonResponse(Status.Ok, secondBatchJson)))

      val batchSize = 2
      Await.result(client.fetchTracksDomainLockings(session, urns, batchSize)) ==== Map(
        Urn("soundcloud", "tracks", "1") -> List(DomainLocking("domain1", Urn("soundcloud", "domain-lockings", "1"), Urn("soundcloud", "tracks", "1")),
          DomainLocking("domain2", Urn("soundcloud", "domain-lockings", "2"), Urn("soundcloud", "tracks", "1"))),
        Urn("soundcloud", "tracks", "2") -> List(DomainLocking("domain3", Urn("soundcloud", "domain-lockings", "3"), Urn("soundcloud", "tracks", "2"))),
        Urn("soundcloud", "tracks", "3") -> List(DomainLocking("domain4", Urn("soundcloud", "domain-lockings", "4"), Urn("soundcloud", "tracks", "3")))
      )
    }

    "500 response" in new GeoblockingsContext {
      when(jsonClient.getWithSession(session, path, Params("track_ids" -> urns.map(_.getIdentifier).mkString(",")), Headers.empty))
        .thenReturn(Future.value(jsonResponse(Status.InternalServerError, JsNull)))

      Await.result(client.fetchTracksDomainLockings(session, urns)) ==== Map.empty
    }
  }

  "#fetchUsersMap" >> {
    trait FetchUsersContext extends GenericContext {
      val path = Path() / "users" / "fetch"

      val urns = Set(
        Urn("soundcloud", "users", "1"),
        Urn("soundcloud", "users", "2"),
        Urn("soundcloud", "users", "3"),
        Urn("soundcloud", "users", "4"))

      val (firstBatch, secondBatch) = urns.splitAt(2)

      val firstBatchJson = Json.parse(
        s"""
           |[
           |  {
           |    "permalink": "permalink1",
           |    "username": "username1",
           |    "avatar_url": "avatar_url1",
           |    "permalink_url": "permalink_url1",
           |    "tracks_count": 1,
           |    "verified": false,
           |    "self": {
           |      "urn": "soundcloud:users:1"
           |    }
           |  }, {
           |    "permalink": "permalink2",
           |    "username": "username2",
           |    "avatar_url": "avatar_url2",
           |    "permalink_url": "permalink_url2",
           |    "tracks_count": 2,
           |    "verified": true,
           |    "self": {
           |      "urn": "soundcloud:users:2"
           |    }
           |  }
           |]
        """.stripMargin)

      val secondBatchJson = Json.parse(
        s"""
           |[
           |  {
           |    "permalink": "permalink3",
           |    "username": "username3",
           |    "avatar_url": "avatar_url3",
           |    "permalink_url": "permalink_url3",
           |    "tracks_count": 3,
           |    "verified": false,
           |    "self": {
           |      "urn": "soundcloud:users:3"
           |    }
           |  }
           |]
        """.stripMargin)
    }

    "200 response" in new FetchUsersContext {
      when(jsonClient.getWithSession(session, path, Params("urns" -> firstBatch.mkString(",")), Headers.empty))
        .thenReturn(Future.value(jsonResponse(Status.Ok, firstBatchJson)))
      when(jsonClient.getWithSession(session, path, Params("urns" -> secondBatch.mkString(",")), Headers.empty))
        .thenReturn(Future.value(jsonResponse(Status.Ok, secondBatchJson)))

      val batchSize = 2
      Await.result(client.fetchUsersMap(session, urns, batchSize)) ==== Map(
        Urn("soundcloud", "users", "1") -> User(firstBatch.head, "permalink1", "username1", "avatar_url1", "permalink_url1", None, None, 1, None, None, false, None, None),
        Urn("soundcloud", "users", "2") -> User(firstBatch.last, "permalink2", "username2", "avatar_url2", "permalink_url2", None, None, 2, None, None, true, None, None),
        Urn("soundcloud", "users", "3") -> User(secondBatch.head, "permalink3", "username3", "avatar_url3", "permalink_url3", None, None, 3, None, None, false, None, None)
      )
    }

    "500 response" in new FetchUsersContext {
      when(jsonClient.getWithSession(session, path, Params("urns" -> urns.mkString(",")), Headers.empty))
        .thenReturn(Future.value(jsonResponse(Status.InternalServerError, JsNull)))

      Await.result(client.fetchUsersMap(session, urns)) ==== Map.empty
    }
  }

}
