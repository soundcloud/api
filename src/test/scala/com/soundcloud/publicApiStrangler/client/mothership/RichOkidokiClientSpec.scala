package com.soundcloud.publicApiStrangler.client.mothership

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.service.users.UserBuilder
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import play.api.libs.json.{JsNull, Json}

class RichOkidokiClientSpec extends UnitSpecification {
  trait GenericContext extends Scope {
    val session = anonymousSession

    val jsonClient = mock[JsonClient]
    val exceptionCollector = mock[ExceptionCollector]
    lazy val client = new RichOkidokiClient(jsonClient, exceptionCollector)
  }

  "track audio" >> {
    trait TrackAudioMetadataContext extends GenericContext {
      def resultF = client.fetchTrackAudioMetadata(session, urn)

      def resultT = Await.result(resultF.liftToTry)

      def result = Await.result(resultF)

      lazy val path = Path() / "tracks" / urn / "audio"
      val urn = Urn("soundcloud", "tracks", "123")

      def mockTrackAudioMetadata: TrackAudioMetadata = TrackAudioMetadata("finished", Some("vqf"), Some(9001))

      def mockResponseContents =
        Json.obj(
          "state" -> mockTrackAudioMetadata.state,
          "original_content_size" -> mockTrackAudioMetadata.original_content_size,
          "original_format" -> mockTrackAudioMetadata.original_format
        )

      def mockResponseStatus: Status = Status.Ok

      def mockResponse = Future.value(jsonResponse(mockResponseStatus, mockResponseContents))

      when(jsonClient.getWithSession(session, path, Params.empty, Headers.empty)).thenReturn(mockResponse)
    }

    "200 response" in new TrackAudioMetadataContext {
      result ==== Some(TrackAudioMetadata("finished", Some("vqf"), Some(9001)))
    }

    "200 response with null values for size and format" in new TrackAudioMetadataContext {
      override def mockResponse =
        Future.value(
          jsonResponse(
            mockResponseStatus,
            Json.obj("state" -> "storing", "original_content_size" -> JsNull, "original_format" -> JsNull)
          )
        )

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

  "#fetchTrackGeoblockings" >> {
    trait GeoblockingsContext extends GenericContext {
      val path = Path() / "tracks" / "geo_blockings" / ""

      val urns = Set(
        Urn("soundcloud", "tracks", "1"),
        Urn("soundcloud", "tracks", "2"),
        Urn("soundcloud", "tracks", "3"),
        Urn("soundcloud", "tracks", "4")
      )

      val (firstBatch, secondBatch) = urns.splitAt(2)

      val firstBatchJson = Json.parse(s"""
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

      val secondBatchJson = Json.parse(s"""
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
        Urn("soundcloud", "tracks", "4")
      )

      val (firstBatch, secondBatch) = urns.splitAt(2)

      val firstBatchJson = Json.parse(s"""
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

      val secondBatchJson = Json.parse(s"""
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

  "#fetchUsersMap" >> {
    trait FetchUsersContext extends GenericContext {
      val path = Path() / "users" / "fetch"

      val urns = Set(
        Urn("soundcloud", "users", "1"),
        Urn("soundcloud", "users", "2"),
        Urn("soundcloud", "users", "3"),
        Urn("soundcloud", "users", "4")
      )

      val (firstBatch, secondBatch) = urns.splitAt(2)

      val firstBatchJson = Json.parse(s"""
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
           | }
           |]
        """.stripMargin)

      val secondBatchJson = Json.parse(s"""
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
        Urn("soundcloud", "users", "1") ->
          new UserBuilder()
            .setUrn(firstBatch.head)
            .setPermalink("permalink1")
            .setUsername("username1")
            .setAvatarUrl("avatar_url1")
            .setPermalinkUrl("permalink_url1")
            .setCity(None)
            .setCountry(None)
            .setTracksCount(1)
            .setFollowersCount(None)
            .setFollowingsCount(None)
            .setVerified(false)
            .setDescription(None)
            .setCreatedAt(None)
            .setUpdatedAt(None)
            .setPublicTracksCount(None)
            .setPublicPlaylistsCount(None)
            .setDiscogsName(None)
            .setFirstName(None)
            .setLastName(None)
            .setFullName(None)
            .setMyspaceName(None)
            .setWebsite(None)
            .setWebsiteTitle(None)
            .setPlan(None)
            .setSubscriptions(Seq.empty)
            .setPublicFavouritesCount(None)
            .setCommentsCount(None)
            .setLikesCount(None)
            .setRepostsCount(None)
            .build,
        Urn("soundcloud", "users", "2") ->
          new UserBuilder()
            .setUrn(firstBatch.last)
            .setPermalink("permalink2")
            .setUsername("username2")
            .setAvatarUrl("avatar_url2")
            .setPermalinkUrl("permalink_url2")
            .setCity(None)
            .setCountry(None)
            .setTracksCount(2)
            .setFollowersCount(None)
            .setFollowingsCount(None)
            .setVerified(true)
            .setDescription(None)
            .setCreatedAt(None)
            .setUpdatedAt(None)
            .setPublicTracksCount(None)
            .setPublicPlaylistsCount(None)
            .setDiscogsName(None)
            .setFirstName(None)
            .setLastName(None)
            .setFullName(None)
            .setMyspaceName(None)
            .setWebsite(None)
            .setWebsiteTitle(None)
            .setPlan(None)
            .setSubscriptions(Seq.empty)
            .setPublicFavouritesCount(None)
            .setCommentsCount(None)
            .setLikesCount(None)
            .setRepostsCount(None)
            .build,
        Urn("soundcloud", "users", "3") ->
          new UserBuilder()
            .setUrn(secondBatch.head)
            .setPermalink("permalink3")
            .setUsername("username3")
            .setAvatarUrl("avatar_url3")
            .setPermalinkUrl("permalink_url3")
            .setCity(None)
            .setCountry(None)
            .setTracksCount(3)
            .setFollowersCount(None)
            .setFollowingsCount(None)
            .setVerified(false)
            .setDescription(None)
            .setCreatedAt(None)
            .setUpdatedAt(None)
            .setPublicTracksCount(None)
            .setPublicPlaylistsCount(None)
            .setDiscogsName(None)
            .setFirstName(None)
            .setLastName(None)
            .setFullName(None)
            .setMyspaceName(None)
            .setWebsite(None)
            .setWebsiteTitle(None)
            .setPlan(None)
            .setSubscriptions(Seq.empty)
            .setPublicFavouritesCount(None)
            .setCommentsCount(None)
            .setLikesCount(None)
            .setRepostsCount(None)
            .build
      )
    }

    "500 response" in new FetchUsersContext {
      when(jsonClient.getWithSession(session, path, Params("urns" -> urns.mkString(",")), Headers.empty))
        .thenReturn(Future.value(jsonResponse(Status.InternalServerError, JsNull)))

      Await.result(client.fetchUsersMap(session, urns)) ==== Map.empty
    }
  }
}
