package com.soundcloud.publicApiStrangler.client.liebling

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.test.Helpers._
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.finagle.IndividualRequestTimeoutException
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Await, Duration, Future}
import org.mockito.Mockito.when
import play.api.libs.json.Json

class LieblingClientSpec extends UnitSpecification {
  trait Context extends Scope {
    implicit val service = mock[JsonClient]
    implicit val session = new UserSessionBuilder().build()
    val telemetry = Telemetry.defaultInstance
    val exceptionCollector = new ExceptionCollector(telemetry)
    val client = new LieblingClient(service, exceptionCollector)

    val userUrn = Urn("soundcloud", "users", "10419549")
    val notFoundUserUrn = Urn("soundcloud", "users", "0")

    val playlistUrn = Urn("soundcloud", "playlists", "48786981")
    val playlistsUrns = List(playlistUrn, Urn("soundcloud", "playlists", "5"))
    val notFoundPlaylistUrn = Urn("soundcloud", "playlists", "0")

    val trackUrn = Urn("soundcloud", "tracks", "48786981")
    val tracksUrns = List(trackUrn, Urn("soundcloud", "tracks", "101"))
    val lieblingTrackLikes = contentsOf("liebling", "track_likes")
    val notFoundTrackUrn = Urn("soundcloud", "tracks", "0")

    val requestBodyString = s"""{"user_urn":"${userUrn.toString}"}"""

    val lieblingLikesCount = Json.parse("""{
        |  "likes_counts": [
        |    {
        |      "likes_count": 18,
        |      "target_urn": "soundcloud:playlists:48786981"
        |    },
        |    {
        |      "likes_count": 0,
        |      "target_urn": "soundcloud:tracks:48786981"
        |    }
        |  ],
        |  "liked_track_urns": [
        |    "soundcloud:tracks:48786981"
        |  ]
        |}""".stripMargin)
  }

  "#createPlaylistLike" >> {
    trait LikeCreatedContext extends Context {
      override implicit val session = loggedInSession(userUrn)
    }

    "creates a like response" in new LikeCreatedContext {
      when(
        service.postWithSession(
          session,
          Path() / "playlists" / playlistUrn.toString / "likes",
          Params.empty,
          Headers.empty,
          Some(requestBodyString)
        )
      ).thenReturn(Future {
        val response = Response(Status.Created)
        response.setContentString(lieblingLikeCreationSuccess)
        response
      })

      val actual = Await.result(client.createPlaylistLike(session, playlistUrn))
      actual ==== LikeCreated
    }
  }

  "#deletePlaylistLike" >> {
    trait LikeDeletedContext extends Context {
      override implicit val session = loggedInSession(userUrn)
    }

    "creates a like response" in new LikeDeletedContext {
      when(
        service.deleteWithSession(
          session,
          Path() / "playlists" / playlistUrn.toString / "likes",
          Params.empty,
          Headers.empty,
          Some(requestBodyString)
        )
      ).thenReturn(Future {
        val response = Response(Status.Ok)
        response.setContentString(lieblingLikeDeletionSuccess)
        response
      })

      val actual = Await.result(client.deletePlaylistLike(session, playlistUrn))
      actual must beAnInstanceOf[DeleteLikeResponse]
    }
  }

  "#likeCounts" >> {
    "successful response" in new Context() {
      val targets = Seq(playlistUrn, trackUrn)

      expectOkResponse(
        Path() / "likes_info",
        lieblingLikesCount,
        Map("for_urns" -> targets, "includes" -> "likes_counts")
      )
      val actual = Await.result(client.likeCounts(session, targets))

      actual must haveSize(2)
      actual ==== (lieblingLikesCount \ "likes_counts").as[List[LikesCount]]
    }

    "unsucessful response" in new Context {
      val targets = Seq(playlistUrn, trackUrn)
      service.getWithSession(
        session,
        Path() / "likes_info",
        Params("for_urns" -> targets, "includes" -> "likes_counts"),
        Headers.empty
      ) returns Future.exception(new IndividualRequestTimeoutException(Duration.fromMilliseconds(1000L)))

      val actual = Await.result(client.likeCounts(session, targets))

      actual must haveSize(0)
    }
  }

  "#userLikeCounts" >> {
    "successful response" in new Context() {
      val targets = Seq(playlistUrn, trackUrn)

      expectOkResponse(
        Path() / "likes_info",
        lieblingLikesCount,
        Map("for_urns" -> targets, "includes" -> "likes_counts,liked_track_urns", "user_urn" -> userUrn)
      )
      val actual = Await.result(client.userLikeCounts(session, targets, userUrn))

      actual.likes_counts must haveSize(2)
      actual.likes_counts ==== (lieblingLikesCount \ "likes_counts").as[List[LikesCount]]
      actual.liked_track_urns must haveSize(1)
      actual.liked_track_urns ==== (lieblingLikesCount \ "liked_track_urns").as[Set[Urn]]
    }

    "performs requests in batches if necessary" in new Context() {
      val targets = Seq(playlistUrn, trackUrn)
      val firstResponse = Json.obj(
        "likes_counts" -> Json.arr((lieblingLikesCount \ "likes_counts" \ 0).get),
        "liked_track_urns" -> Json.arr()
      )
      val secondResponse = Json.obj(
        "likes_counts" -> Json.arr((lieblingLikesCount \ "likes_counts" \ 1).get),
        "liked_track_urns" -> (lieblingLikesCount \ "liked_track_urns").get
      )

      expectOkResponse(
        Path() / "likes_info",
        firstResponse,
        Map("for_urns" -> Seq(playlistUrn), "includes" -> "likes_counts,liked_track_urns", "user_urn" -> userUrn)
      )
      expectOkResponse(
        Path() / "likes_info",
        secondResponse,
        Map("for_urns" -> Seq(trackUrn), "includes" -> "likes_counts,liked_track_urns", "user_urn" -> userUrn)
      )
      val actual = Await.result(client.userLikeCounts(session, targets, userUrn, batchSize = 1))

      actual.likes_counts must haveSize(2)
      actual.likes_counts ==== (lieblingLikesCount \ "likes_counts").as[List[LikesCount]]
      actual.liked_track_urns must haveSize(1)
      actual.liked_track_urns ==== (lieblingLikesCount \ "liked_track_urns").as[Set[Urn]]
    }
  }

  "#userTotalLikeCounts" >> {
    trait UserTotalLikeCounts extends Context {
      val userUrn2 = Urn("soundcloud", "users", "1293871")
      val userUrn3 = Urn("soundcloud", "users", "29874198372")

      val lieblingUserTotalLikeCount = Json.parse(s"""{
           |  "users": [{
           |    "user_urn": "${userUrn.toString}",
           |    "track_likes_count": 194,
           |    "playlist_likes_count": 19
           |  }, {
           |    "user_urn": "${userUrn2.toString}",
           |    "track_likes_count": 100,
           |    "playlist_likes_count": 200
           |  }]
           |}
      """.stripMargin)

      val targetUrns = List(userUrn, userUrn2, userUrn3)

      lazy val result = Await.result(client.userTotalLikeCount(session, targetUrns))
    }

    "successful response" in new UserTotalLikeCounts {
      expectOkResponse(
        Path() / "users_counts",
        lieblingUserTotalLikeCount,
        Map("for_urns" -> List(userUrn, userUrn2, userUrn3))
      )

      result.size ==== 2
      result(0).user_urn ==== userUrn
      result(0).track_likes_count ==== 194
      result(0).playlist_likes_count ==== 19
      result(0).totalLikeCount ==== 213

      result(1).user_urn ==== userUrn2
      result(1).track_likes_count ==== 100
      result(1).playlist_likes_count ==== 200
      result(1).totalLikeCount ==== 300
    }

    "unsuccessful response" in new UserTotalLikeCounts {
      service.getWithSession(session, Path() / "users_counts", Params("for_urns" -> targetUrns), Headers.empty) returns Future
        .exception(new RuntimeException("noooo"))

      result.size ==== 0
    }
  }

  "#userLikedTracks" >> {
    trait UserLikedTracksContext extends Context {
      val track1 = Urn("soundcloud", "tracks", "48786981")
      val track2 = Urn("soundcloud", "tracks", "2")
      val trackUrns = Seq(track1, track2)
    }

    "returns if the user has liked the provided tracks" in new UserLikedTracksContext {
      expectOkResponse(
        Path() / "likes_info",
        lieblingLikesCount,
        Map("for_urns" -> trackUrns, "includes" -> "likes_counts,liked_track_urns", "user_urn" -> userUrn)
      )

      Await.result(client.userLikedTracks(session, trackUrns.toSet, userUrn)) ==== Map(track1 -> true, track2 -> false)
    }
  }

  "#userTracksLikesForUrns" >> {
    trait TracksLikedByUserContext extends Context {
      val track1 = Urn("soundcloud", "tracks", "48786981")
      val track2 = Urn("soundcloud", "tracks", "2")
      val trackUrns = Seq(track1, track2)
      lazy val result = Await.result(client.userTracksLikesForUrns(session, userUrn, trackUrns.toList))
    }

    "for a given set of urns, returns the urns of tracks that have been liked" in new TracksLikedByUserContext {
      expectOkResponse(
        Path() / "likes_info",
        lieblingLikesCount,
        Map("for_urns" -> trackUrns, "includes" -> "liked_track_urns", "user_urn" -> userUrn)
      )

      result ==== List(Urn("soundcloud", "tracks", "48786981"))
    }

    "unsuccessful response" in new TracksLikedByUserContext {
      expectInternalErrorResponse(
        Path() / "likes_info",
        Map("for_urns" -> trackUrns, "includes" -> "liked_track_urns", "user_urn" -> userUrn)
      )

      result ==== List.empty
    }
  }

  "#userTrackLikes" >> {
    "successful response" in new Context {
      expectOkResponse(
        Path() / "users" / userUrn / "track_likes",
        lieblingTrackLikes,
        Params("cursor" -> "1234567890123456", "page_size" -> "2")
      )

      val result = Await.result(client.userTracksLikes(session, userUrn, Some("1234567890123456"), 2))
      result.likes must haveSize(2)
      result.meta.cursor.next_params ==== Some(LikesPageNextParams("1358467797123456", 2))
    }

    "non successful response" in new Context {
      expectInternalErrorResponse(
        Path() / "users" / userUrn / "track_likes",
        Map("cursor" -> "1234567890123456", "page_size" -> "2")
      )

      val result = Await.result(client.userTracksLikes(session, userUrn, Some("1234567890123456"), 2))
      result ==== client.emptyLikesPage
    }
  }
}
