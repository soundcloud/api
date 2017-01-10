package com.soundcloud.publicApiStrangler.client.liebling

import com.soundcloud.jvmkit.{UserSession, Urn}
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.{OkStatus, StatusCode}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.finagle.IndividualRequestTimeoutException
import com.twitter.util.{Await, Duration, Future}
import org.jboss.netty.handler.codec.http.HttpMethod
import play.api.libs.json.{JsNull, JsValue, Json}
import org.mockito.Mockito.when

class LieblingClientSpec extends UnitSpecification {

  trait Context extends Scope {
    implicit val service = mock[JsonClient]
    implicit val session = mock[UserSession]
    val client = new LieblingClient(service)

    val userUrn = Urn("soundcloud:users:10419549")
    val notFoundUserUrn = Urn("soundcloud:users:0")

    val playlistUrn = Urn("soundcloud:playlists:48786981")
    val playlistsUrns = List(playlistUrn, Urn("soundcloud:playlists:5"))
    val notFoundPlaylistUrn = Urn("soundcloud:playlists:0")

    val trackUrn = Urn("soundcloud:tracks:48786981")
    val tracksUrns = List(trackUrn, Urn("soundcloud:tracks:101"))
    val notFoundTrackUrn = Urn("soundcloud:tracks:0")

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

      expectOkResponse(Path() / "likes_info", lieblingLikesCount, Map("for_urns" -> targets, "includes" -> "likes_counts"))
      val actual = Await.result(client.likeCounts(session, targets))

      actual must haveSize(2)
      actual ==== (lieblingLikesCount \ "likes_counts").as[List[LikesCount]]
    }

    "unsucessful response" in new Context {
      val targets = Seq(playlistUrn, trackUrn)
      service.get(session,
        Path() / "likes_info",
        Map("for_urns" -> targets, "includes" -> "likes_counts"),
        Params.empty) returns Future.exception(new IndividualRequestTimeoutException(Duration.fromMilliseconds(1000L)))

      val actual = Await.result(client.likeCounts(session, targets))

      actual must haveSize(0)
    }
  }

  "#userLikeCounts" >> {
    "successful response" in new Context() {
      val targets = Seq(playlistUrn, trackUrn)

      expectOkResponse(Path() / "likes_info", lieblingLikesCount, Map("for_urns" -> targets, "includes" -> "likes_counts,liked_track_urns", "user_urn" -> userUrn))
      val actual = Await.result(client.userLikeCounts(session, targets, userUrn))

      actual.likes_counts must haveSize(2)
      actual.likes_counts ==== (lieblingLikesCount \ "likes_counts").as[List[LikesCount]]
      actual.liked_track_urns must haveSize(1)
      actual.liked_track_urns ==== (lieblingLikesCount \ "liked_track_urns").as[Set[String]].map(Urn(_))
    }

    "performs requests in batches if necessary" in new Context() {
      val targets = Seq(playlistUrn, trackUrn)
      val firstResponse = Json.obj(
        "likes_counts" -> Json.arr((lieblingLikesCount \ "likes_counts")(0)),
        "liked_track_urns" -> Json.arr()
      )
      val secondResponse = Json.obj(
        "likes_counts" -> Json.arr((lieblingLikesCount \ "likes_counts")(1)),
        "liked_track_urns" -> (lieblingLikesCount \ "liked_track_urns")
      )

      expectOkResponse(Path() / "likes_info", firstResponse, Map("for_urns" -> Seq(playlistUrn), "includes" -> "likes_counts,liked_track_urns", "user_urn" -> userUrn))
      expectOkResponse(Path() / "likes_info", secondResponse, Map("for_urns" -> Seq(trackUrn), "includes" -> "likes_counts,liked_track_urns", "user_urn" -> userUrn))
      val actual = Await.result(client.userLikeCounts(session, targets, userUrn, batchSize = 1))

      actual.likes_counts must haveSize(2)
      actual.likes_counts ==== (lieblingLikesCount \ "likes_counts").as[List[LikesCount]]
      actual.liked_track_urns must haveSize(1)
      actual.liked_track_urns ==== (lieblingLikesCount \ "liked_track_urns").as[Set[String]].map(Urn(_))
    }
  }

  "#userTotalLikeCounts" >> {
    trait UserTotalLikeCounts extends Context {
      val userUrn2 = Urn("soundcloud:users:1293871")
      val userUrn3 = Urn("soundcloud:users:29874198372")

      val lieblingUserTotalLikeCount = Json.parse(
        s"""{
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
      expectOkResponse(Path() / "users_counts", lieblingUserTotalLikeCount, Map("for_urns" -> List(userUrn, userUrn2, userUrn3)))

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
      service.get(
        session,
        Path() / "users_counts",
        Map("for_urns" -> targetUrns),
        Params.empty) returns Future.exception(new RuntimeException("noooo"))

      result.size ==== 0
    }
  }

  "#userLikedTracks" >> {
    trait UserLikedTracksContext extends Context {
      val track1 = Urn("soundcloud:tracks:48786981")
      val track2 = Urn("soundcloud:tracks:2")
      val trackUrns = Seq(track1, track2)

    }

    "returns if the user has liked the provided tracks" in new UserLikedTracksContext {
      expectOkResponse(Path() / "likes_info", lieblingLikesCount,
        Map("for_urns" -> trackUrns, "includes" -> "likes_counts,liked_track_urns", "user_urn" -> userUrn))

      Await.result(client.userLikedTracks(session, trackUrns.toSet, userUrn)) ==== Map(track1 -> true, track2 -> false)
    }
  }

  private def expectOkResponse(path: Path, expected: JsValue, params: Params = Params.empty, headers: Params = Params.empty)
                      (implicit service: JsonClient, session: UserSession) =
    expectResponse(path, params, HttpMethod.GET, headers, OkStatus, ExpectedBody(expected))

  private def expectResponse(path: Path, params: Params, method: HttpMethod, headers: Params, code: StatusCode, expectedBody: MockedBody = new ExpectedBody(JsNull, None))
                    (implicit service: JsonClient, session: UserSession) = {
    val bodyString = expectedBody.requestBodyString

    when(
      method match {
        case HttpMethod.GET => if (session != null) service.get(session, path, params, headers) else service.getWithoutSession(path, params, headers)
        case HttpMethod.POST => service.post(session, path, params, headers, bodyString)
        case HttpMethod.PATCH => service.patch(session, path, params, headers, bodyString)
        case HttpMethod.DELETE => service.delete(session, path, params, headers, bodyString)
        case HttpMethod.PUT => service.put(session, path, params, headers, bodyString)
        case HttpMethod.OPTIONS => service.options(session, path, params, params, bodyString)
        case HttpMethod.HEAD => service.head(session, path, params, params, bodyString)
        case HttpMethod.TRACE => service.trace(session, path, params, params, bodyString)
        case HttpMethod.CONNECT => service.connect(session, path, params, params, bodyString)
      }
    ).thenReturn(Future(JsonResponse(code, expectedBody.responseBody)))
  }
}

trait MockedBody {
  val responseBody: JsValue
  def requestBodyString: Option[String]
}

case class ExpectedBody(responseBody: JsValue = JsNull, requestBody: Option[JsValue] = None) extends MockedBody {
  override def requestBodyString = requestBody.map(Json.stringify)
}
