package com.soundcloud.apipublic.client.liebling

import com.soundcloud.apipublic.test.Helpers._
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.finagle.IndividualRequestTimeoutException
import com.twitter.util.{Await, Duration, Future}
import play.api.libs.json.Json

class LieblingClientSpec extends UnitSpecification {
  trait Context extends Scope {
    implicit val service = mock[JsonClient]
    implicit val session = new UserSessionBuilder().build()
    val client = new LieblingClient(service)

    val userUrn = Urn("soundcloud", "users", "10419549")
    val notFoundUserUrn = Urn("soundcloud", "users", "0")

    val playlistUrn = Urn("soundcloud", "playlists", "48786981")
    val playlistsUrns = List(playlistUrn, Urn("soundcloud", "playlists", "5"))
    val notFoundPlaylistUrn = Urn("soundcloud", "playlists", "0")

    val trackUrn = Urn("soundcloud", "tracks", "48786981")
    val tracksUrns = List(trackUrn, Urn("soundcloud", "tracks", "101"))
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
}
