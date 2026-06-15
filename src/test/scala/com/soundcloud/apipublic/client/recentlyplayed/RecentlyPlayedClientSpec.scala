package com.soundcloud.apipublic.client.recentlyplayed

import com.soundcloud.apipublic.client.support.UnhandledResponseException
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.apipublic.test.fixtures.Fixtures
import com.soundcloud.jvmkit.module.http.client.JsonClient
import com.soundcloud.jvmkit.module.http.server.ResponseBuilder
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.util.{Await, Future}
import play.api.libs.json.Json

class RecentlyPlayedClientSpec extends UnitSpecification {

  trait Context extends org.specs2.specification.Scope {
    val session = mock[UserSession]
    val apiClient = mock[JsonClient]
    val user = Urn("soundcloud", "users", "1")
    val tracks = List(
      RecentlyPlayedTrack(Urn("soundcloud", "tracks", "1"), 1473850000001L),
      RecentlyPlayedTrack(Urn("soundcloud", "tracks", "2"), 1473850000002L)
    )
    val client = new RecentlyPlayedClient(apiClient)
  }

  "getTracks" >> {
    "calls the api client" in new Context {
      apiClient.getWithSession(
        session,
        Path() / "users" / user / "recently-played" / "tracks",
        Map("limit" -> "25")
      ) returns Future.value(
        ResponseBuilder.ok(Json.stringify(Fixtures.contentsOf("recentlyplayed", "tracks")))
      )

      Await.result(client.getTracks(session, user, 25)) ==== tracks
    }

    "throws an exception when server doesn't return success status" in new Context {
      apiClient.getWithSession(
        session,
        Path() / "users" / user / "recently-played" / "tracks",
        Map("limit" -> "25")
      ) returns Future.value(ResponseBuilder.internalServerError())

      Await.result(client.getTracks(session, user, 25)) must throwA[UnhandledResponseException]
    }
  }
}
