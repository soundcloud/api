package com.soundcloud.publicApiStrangler.client.reposts

import com.soundcloud.jvmkit.Urn
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.{NotFoundStatus, OkStatus}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.{Await, Future}
import play.api.libs.json._

class RepostsClientUnitSpec extends UnitSpecification {

  trait Context extends Scope {
    lazy val jsonClient = mock[JsonClient]
    lazy val client = new RepostsClient(jsonClient)
    lazy val user = Urn("soundcloud", "users", "1")
  }

  "#repostsCountForUser" >> {

    "returns the total count if track and playlist counts are available" in new Context {
      jsonClient.get(anonymousSession,
        Path() / "users" / "soundcloud:users:1" / "track_reposts" / "count", Params.empty, Params.empty
      ).returns(Future.value(JsonResponse(OkStatus,
        Json.parse("""{ "counts": [ { "urn": "soundcloud:users:1" , "count": 12 } ] }""")
      )))

      jsonClient.get(anonymousSession,
        Path() / "users" / "soundcloud:users:1" / "playlist_reposts" / "count", Params.empty, Params.empty
      ).returns(Future.value(JsonResponse(OkStatus,
        Json.parse("""{ "counts": [ { "urn": "soundcloud:users:1" , "count": 5 } ] }""")
      )))

      val result = Await.result(client.repostsCountForUser(anonymousSession, user))

      result.urn ==== user
      result.count ==== 17L
    }

    "returns a failed future if either upstream call gets a non-OK response" in new Context {
      jsonClient.get(anonymousSession,
        Path() / "users" / "soundcloud:users:1" / "track_reposts" / "count", Params.empty, Params.empty
      ).returns(Future.value(JsonResponse(NotFoundStatus, JsNull)))

      jsonClient.get(anonymousSession,
        Path() / "users" / "soundcloud:users:1" / "playlist_reposts" / "count", Params.empty, Params.empty
      ).returns(Future.value(JsonResponse(OkStatus,
        Json.parse("""{ "counts": [ { "urn": "soundcloud:users:1" , "count": 5 } ] }""")
      )))

      Await.result(client.repostsCountForUser(anonymousSession, user).liftToTry).isThrow ==== true
    }

    "returns a failed future if either upstream call returns a non-parsable response" in new Context {
      jsonClient.get(anonymousSession,
        Path() / "users" / "soundcloud:users:1" / "track_reposts" / "count", Params.empty, Params.empty
      ).returns(Future.value(JsonResponse(OkStatus,
        Json.parse("""{ "counts": [ { "urn": "soundcloud:users:1" , "count": 12 } ] }""")
      )))

      jsonClient.get(anonymousSession,
        Path() / "users" / "soundcloud:users:1" / "playlist_reposts" / "count", Params.empty, Params.empty
      ).returns(Future.value(JsonResponse(OkStatus,
        Json.parse("""{}""")
      )))

      Await.result(client.repostsCountForUser(anonymousSession, user).liftToTry).isThrow ==== true
    }

    "returns a failed future if either upstream call fails" in new Context {
      jsonClient.get(anonymousSession,
        Path() / "users" / "soundcloud:users:1" / "track_reposts" / "count", Params.empty, Params.empty
      ).returns(Future.value(JsonResponse(OkStatus,
        Json.parse("""{ "counts": [ { "urn": "soundcloud:users:1" , "count": 12 } ] }""")
      )))

      jsonClient.get(anonymousSession,
        Path() / "users" / "soundcloud:users:1" / "playlist_reposts" / "count", Params.empty, Params.empty
      ).returns(Future.???)

      Await.result(client.repostsCountForUser(anonymousSession, user).liftToTry).isThrow ==== true
    }

  }

}
