package com.soundcloud.publicApiStrangler.handler

import java.util.TimeZone

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.service.UserPlaylistsService
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.playlists.PlaylistBuilder
import com.soundcloud.publicApiStrangler.service.playlists.representation.Playlist
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Future
import org.joda.time.DateTimeZone
import org.mockito.Mockito.when

class UserPlaylistsHandlerSpec extends UnitSpecification {
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  DateTimeZone.setDefault(DateTimeZone.UTC)

  trait Context extends HandlerSpecificationScope {
    val session = loggedInSession(Urn("soundcloud", "users", "1"))
    val userAuthentication = new FakeUserAuthentication(session)

    val userPlaylistsService = mock[UserPlaylistsService]

    val handler = new UserPlaylistsHandler(
      userAuthentication,
      userPlaylistsService
    )

    override def routingDefinitions = Routing.forUserPlaylistsHandler(handler)
  }

  "Getting playlists" >> {
    trait PlaylistsForUserContext extends Context {
      val queryString =
        "?page_size=1&offset=2&linked_partitioning=1"

      def paginationParams(path: String): CursorBasedPagination = {
        val mockRequest = Request(path)
        mockRequest.host = "localhost"
        CursorBasedPagination.build(mockRequest, Seq("linked_partitioning"))

      }

      def stubService(
          user: Urn,
          path: String,
          collection: Collection[Playlist]
      ) = {
        when(userPlaylistsService.userPlaylists(session, user, paginationParams(path)))
          .thenReturn(Future.value(collection))
      }
    }

    trait SuccessfulResponse extends PlaylistsForUserContext {
      val playlist = new PlaylistBuilder().build
      val playlistsCollection = Collection(List(playlist), None)
      val expectedResponse = Collection.getRepresentation(playlistsCollection, true)
    }

    trait SuccessfulEmptyResponse {
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
          response.contentString ==== expectedResponse
        }
      }

      "invalid user id requested" in new PlaylistsForUserContext {
        val path = s"/users/NaN/playlists$queryString"

        val response = get(path)
        response.status ==== Status.NotFound
        response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
      }
    }

    "GET /me/playlists" >> {
      "with a successful response from playlists service" >> {
        "returns playlists" in new PlaylistsForUserContext with SuccessfulResponse {
          val userUrn = Urn("soundcloud", "users", "1")
          val path = s"/me/playlists$queryString"

          stubService(userUrn, path, playlistsCollection)

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== expectedResponse
        }
        "returns empty collection of playlists" in new PlaylistsForUserContext with SuccessfulEmptyResponse {
          val userUrn = Urn("soundcloud", "users", "1")
          val path = s"/me/playlists$queryString"

          stubService(userUrn, path, playlistsCollection)

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== expectedResponse
        }
      }
    }

  }
}
