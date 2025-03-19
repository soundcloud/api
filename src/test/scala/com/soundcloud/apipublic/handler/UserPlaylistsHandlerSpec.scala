package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.Routing
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.UserPlaylistsService
import com.soundcloud.apipublic.service.pagination.{CursorBasedPagination, OffsetBasedPagination}
import com.soundcloud.apipublic.service.playlists.PlaylistBuilder
import com.soundcloud.apipublic.service.playlists.representation.Playlist
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Future
import org.joda.time.DateTimeZone
import org.mockito.Mockito.when
import play.api.libs.json.Json

import java.util.TimeZone

class UserPlaylistsHandlerSpec extends UnitSpecification {
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  DateTimeZone.setDefault(DateTimeZone.UTC)

  trait Context extends HandlerSpecificationScope {
    val playlist = new PlaylistBuilder().build
    val session = loggedInSession(Urn("soundcloud", "users", "1"))
    val userAuthentication = new FakeUserAuthentication(session)
    val baseUrl = "localhost"
    val userPlaylistsService = mock[UserPlaylistsService]

    val handler = new UserPlaylistsHandler(
      userAuthentication,
      userPlaylistsService,
      baseUrl
    )

    override def routingDefinitions = Routing.forUserPlaylistsHandler(handler)

  }

  "Getting multiple playlists" >> {
    trait PlaylistsForUserContext extends Context {
      val queryString =
        "?page_size=1&cursor=2&linked_partitioning=1&access=playable,preview"

      def paginationParams(path: String): CursorBasedPagination = {
        val mockRequest = Request(path)
        CursorBasedPagination.build("localhost", mockRequest, Seq("linked_partitioning", "access"))
      }

      def stubService(
          user: Urn,
          path: String,
          collection: Collection[Playlist],
          access: AccessParams = AccessParams.defaultAccess
      ) = {
        when(userPlaylistsService.userPlaylists(session, user, access, paginationParams(path), None))
          .thenReturn(Future.value(collection))
      }
    }

    trait SuccessfulResponse extends PlaylistsForUserContext {
      val playlistsCollection = Collection(List(playlist), None)
      val expectedResponse = Collection.getRepresentation(playlistsCollection, true)
    }

    trait SuccessfulEmptyResponse extends PlaylistsForUserContext {
      val playlistsCollection = Collection[Playlist](List.empty, None)
      val expectedResponse = Collection.getRepresentation(playlistsCollection, true)
    }

    "GET /users/:id/playlists" >> {
      "with a successful response from playlists service" >> {
        "returns playlists" in new PlaylistsForUserContext with SuccessfulResponse {
          val userUrn = Urn("soundcloud", "users", "7110")
          val path = s"/users/7110/playlists$queryString"

          stubService(userUrn, path, playlistsCollection)

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== expectedResponse
        }
        "returns empty collection of playlists" in new PlaylistsForUserContext with SuccessfulEmptyResponse {
          val userUrn = Urn("soundcloud", "users", "7110")
          val path = s"/users/7110/playlists$queryString"

          stubService(userUrn, path, playlistsCollection)

          val response = get(path)
          response.status ==== Status.Ok
          response.headerMap.get("Cache-Control").get === "public, max-age=60, must-revalidate"
          response.contentString ==== expectedResponse
        }
      }

      "invalid user id requested" in new PlaylistsForUserContext {
        val path = s"/users/NaN/playlists$queryString"

        val response = get(path)
        response.status ==== Status.BadRequest
      }
    }

    "GET /me/playlists" >> {
      "with a successful response from playlists service" >> {
        "returns playlists" in new PlaylistsForUserContext with SuccessfulResponse {
          override val queryString = "?page_size=1&cursor=2&linked_partitioning=1"
          val userUrn = Urn("soundcloud", "users", "1")
          val path = s"/me/playlists$queryString"

          stubService(userUrn, path, playlistsCollection, AccessParams.explicitAccess)

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== expectedResponse
        }
        "returns empty collection of playlists" in new PlaylistsForUserContext with SuccessfulEmptyResponse {
          val userUrn = Urn("soundcloud", "users", "1")
          val path = s"/me/playlists$queryString"

          stubService(userUrn, path, playlistsCollection, AccessParams.explicitAccess)

          val response = get(path)
          response.status ==== Status.Ok
          response.headerMap.get("Cache-Control").get === "private, max-age=0"
          response.contentString ==== expectedResponse
        }
      }
    }

  }

  "Getting single playlist" >> {
    trait PlaylistsForUserContext extends Context {
      val queryString =
        "?limit=1&offset=2&linked_partitioning=1&secret_token=s3cret&access=playable,preview"

      def paginationParams(path: String): OffsetBasedPagination = {
        val mockRequest = Request(path)
        OffsetBasedPagination.build("localhost", mockRequest, Seq("linked_partitioning", "secret_token", "access"))
      }

      def stubService(
          playlistUrn: Urn,
          path: String,
          userId: String,
          response: Outcome[Playlist],
          access: AccessParams = AccessParams.defaultAccess
      ) = {
        when(
          userPlaylistsService.userPlaylist(
            session,
            playlistUrn,
            Some("s3cret"),
            Some(paginationParams(path)),
            userId,
            access,
            None
          )
        ).thenReturn(Future.value(response))
      }
    }

    trait SuccessfulResponse extends Context {
      val expectedResponse = playlist
    }

    "GET /users/:userId/playlists/:id" >> {
      "with a successful response from playlists service" >> {
        "return playlist" in new PlaylistsForUserContext with SuccessfulResponse {
          val userId = "1"
          val path = s"/users/$userId/playlists/987$queryString"

          stubService(Urn("soundcloud", "playlists", "987"), path, userId, playlist.good)
          val response = get(path)

          response.status ==== Status.Ok
          response.contentString ==== Json.stringify(Json.toJson(expectedResponse))

        }
      }

      "with a 404 from playlists service" >> {
        "returns an error response" in new PlaylistsForUserContext {
          val userId = "2"
          val path = s"/users/$userId/playlists/404$queryString"

          stubService(Urn("soundcloud", "playlists", "404"), path, userId, NotFound("playlist not found").bad)
          val response = get(path)

          response.status ==== Status.NotFound
        }
      }
    }
  }

}
