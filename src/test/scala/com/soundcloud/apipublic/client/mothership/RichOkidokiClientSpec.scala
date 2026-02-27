package com.soundcloud.apipublic.client.mothership

import com.soundcloud.apipublic.client.mothership.response.mapper.UserRepresentationMapper.MockTimestampForOtherUsers
import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.apipublic.service.users.UserBuilder
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.jvmkit.module.http.server.ResponseBuilder
import com.soundcloud.jvmkit.module.outcome.Good
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import play.api.libs.json.{JsNull, Json}

class RichOkidokiClientSpec extends UnitSpecification {
  trait GenericContext extends Scope {
    lazy val client = new RichOkidokiClient(jsonClient, exceptionCollector)
    val session = anonymousSession
    val jsonClient = mock[JsonClient]
    val exceptionCollector = mock[ExceptionCollector]
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
            .setPermalinkUrl("permalink_url1?utm_medium=api&utm_campaign=social_sharing&utm_source=id_1")
            .setCity(None)
            .setCountry(None)
            .setTracksCount(1)
            .setFollowersCount(None)
            .setFollowingsCount(None)
            .setVerified(false)
            .setDescription(None)
            .setCreatedAt(Some(MockTimestampForOtherUsers))
            .setUpdatedAt(Some(MockTimestampForOtherUsers))
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
            .setPermalinkUrl("permalink_url2?utm_medium=api&utm_campaign=social_sharing&utm_source=id_1")
            .setCity(None)
            .setCountry(None)
            .setTracksCount(2)
            .setFollowersCount(None)
            .setFollowingsCount(None)
            .setVerified(true)
            .setDescription(None)
            .setCreatedAt(Some(MockTimestampForOtherUsers))
            .setUpdatedAt(Some(MockTimestampForOtherUsers))
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
            .setPermalinkUrl("permalink_url3?utm_medium=api&utm_campaign=social_sharing&utm_source=id_1")
            .setCity(None)
            .setCountry(None)
            .setTracksCount(3)
            .setFollowersCount(None)
            .setFollowingsCount(None)
            .setVerified(false)
            .setDescription(None)
            .setCreatedAt(Some(MockTimestampForOtherUsers))
            .setUpdatedAt(Some(MockTimestampForOtherUsers))
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

  "#fetchMutings" >> {
    trait Mutings extends GenericContext {
      val userUrn = Urn.parse("soundcloud:users:123").get
      def jsonClientResponse: Future[Response]

      def result = Await.result(client.mutings(session, userUrn).value)

      val path = Path("/users") / userUrn / "mutings" / "urns"
      jsonClient.getWithSession(===(session), ===(path), any[Params], any[Headers]) returns jsonClientResponse
    }

    "it returns a list of urns when successful" in new Mutings {
      override def jsonClientResponse = Future.value(ResponseBuilder.ok("""["soundcloud:users:1"]"""))

      result ==== Good(List(Urn("soundcloud", "users", "1")))
    }

    "it returns an empty list on bad results" in new Mutings {
      override def jsonClientResponse = Future.value(ResponseBuilder.internalServerError())

      result ==== Good(List.empty)
    }

    "it returns an empty list on exceptions" in new Mutings {
      override def jsonClientResponse = Future.exception(new RuntimeException("error"))

      result ==== Good(List.empty)
    }
  }
}
