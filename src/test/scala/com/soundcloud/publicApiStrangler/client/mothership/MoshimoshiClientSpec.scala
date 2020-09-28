package com.soundcloud.publicApiStrangler.client.mothership

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.chrono.ChronoResponse
import com.soundcloud.publicApiStrangler.client.mothership.response.mapper._
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.test.Helpers._
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.finagle.http.{ParamMap, Status}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import play.api.libs.json._

class MoshimoshiClientSpec extends UnitSpecification {
  trait Context extends Scope {
    implicit val service = mock[JsonClient]

    implicit val session = loggedInSession(Urn("soundcloud", "users", "1"))
    val client = new MoshimoshiClient(
      service
    )
  }

  "#fetchUserObjects" >> {
    trait UsersContext extends Context {
      val urns = Set(Urn("soundcloud", "users", "10419549"), Urn("soundcloud", "users", "123123123"))

      def path = Path() / "users" / "fetch"

      def fetch = Await.result(client.fetchUserObjects(session, urns))
    }

    "found response" in new UsersContext {
      expectOkResponse(path, moshiUsers, urns.toList)

      fetch ==== List(UserMapper(moshiUser), UserMapper(moshiUser2))
    }

    "not found response" in new UsersContext {
      expectOkResponse(path, JsArray(), urns.toList)

      fetch ==== List()
    }

    "invalid response" in new UsersContext {
      expectInternalErrorResponse(path, urns.toList)

      fetch must throwA[IllegalStateException]
    }
  }

  "#userPlaylists" >> {
    trait PlaylistsByUser extends Context {
      val userUrn = Urn("soundcloud", "users", "1")

      val path = Path("/users") / userUrn / "playlists" / "chrono"
      val pagination = CursorBasedPagination(
        "https://api.soundcloud.com",
        "/users/1/playlists/",
        ParamMap(),
        Some("2"),
        2
      )
    }

    "200 status" in new PlaylistsByUser {
      when(
        service.getWithSession(
          anonymousSession,
          path,
          Params("cursor" -> "2", "limit" -> "2", "direction" -> "desc"),
          Headers.empty
        )
      ).thenReturn(Future(jsonResponse(Status.Ok, moshimoshiPlaylistsChrono)))

      val result = Await.result(client.userPlaylists(anonymousSession, userUrn, pagination))
      result.items must haveSize(2)
    }

    "500 status" in new PlaylistsByUser {
      when(
        service.getWithSession(
          anonymousSession,
          path,
          Params("cursor" -> "2", "limit" -> "2", "direction" -> "desc"),
          Headers.empty
        )
      ).thenReturn(Future(jsonResponse(Status.InternalServerError, JsNull)))

      Await.result(client.userPlaylists(anonymousSession, userUrn, pagination)) ==== ChronoResponse.emptyResponse
    }
  }
}
