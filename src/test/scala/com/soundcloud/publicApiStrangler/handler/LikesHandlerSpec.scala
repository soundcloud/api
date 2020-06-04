package com.soundcloud.publicApiStrangler.handler

import java.util.TimeZone

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.service.LikesService
import com.soundcloud.publicApiStrangler.service.pagination.{CursorBasedPagination, Pagination}
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackRepresentationLike,
  TrackRepresentationLikeSpecContext,
  TracksCollection
}
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures.contentsOf
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Future
import org.joda.time.DateTimeZone
import org.mockito.Mockito.when
import play.api.libs.json.Json

class LikesHandlerSpec extends UnitSpecification {
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  DateTimeZone.setDefault(DateTimeZone.UTC)

  trait Context extends HandlerSpecificationScope {
    val session = loggedInSession(Urn("soundcloud", "users", "1"))
    val userAuthentication = new FakeUserAuthentication(session)

    val likesService = mock[LikesService]
    val telemetry = Telemetry.createIsolatedInstance
    val exceptionCollector = new ExceptionCollector(telemetry)

    val handler = new LikesHandler(
      userAuthentication,
      likesService,
      "https://api.soundcloud.com",
      exceptionCollector
    )

    override def routingDefinitions = Routing.forLikesHandler(handler)
  }

  "Getting tracks" >> {
    trait TracksForUserContext extends Context {
      val queryString =
        "?page_size=1&cursor=2&linked_partitioning=1"

      def paginationParams(path: String): CursorBasedPagination = {
        val mockRequest = Request(path)
        mockRequest.host = "localhost"
        Pagination.buildCursorBasedPagination(mockRequest)

      }

      def stubUserTrackLikeForUrn(
          user: Urn,
          trackUrn: Urn,
          trackRepresentation: Option[TrackRepresentationLike]
      ) = {
        when(likesService.userTrackLikeForUrn(session, user, trackUrn))
          .thenReturn(Future.value(trackRepresentation))
      }

      def stubUserTracksLikes(
          user: Urn,
          path: String,
          tracksCollection: TracksCollection
      ) = {
        when(likesService.userTracksLikes(session, user, paginationParams(path)))
          .thenReturn(Future.value(tracksCollection))
      }
    }

    trait SuccessfulResponse extends TrackRepresentationLikeSpecContext with TracksForUserContext {
      val trackRepresentation = createTrackRepresentation()
      val tracksCollection = TracksCollection(List(trackRepresentation), None)
      val expectedResponse = contentsOf("tracks", "track_representation_response").toString()
    }

    trait ErrorResponse extends TracksForUserContext {
      val errorMessage = "an unexpected error occurred"
      val trackRepresentationResult =
        Future.exception(new RuntimeException("An unexpected error occurred while fetching a tracks"))
      val expectedResponse =
        s"""{"error":"$errorMessage"}"""
    }

    "GET /users/:userId/favorites/:trackId" >> {
      "with a successful response from tracks service" >> {
        "returns track" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "1")
          val urn = Urn("soundcloud", "tracks", "48786981")
          val path = s"/users/1/favorites/48786981$queryString"

          stubUserTrackLikeForUrn(user, urn, Some(trackRepresentation))

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== Json.toJson(trackRepresentation).toString()
        }

        "returns not found if empty" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "1")
          val urn = Urn("soundcloud", "tracks", "48786981")
          val path = s"/users/1/favorites/48786981$queryString"

          stubUserTrackLikeForUrn(user, urn, None)

          val response = get(path)
          response.status ==== Status.NotFound
          response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
        }
      }

      "with an error response from tracks service" >> {
        "returns an error response with message" in new TracksForUserContext with ErrorResponse {
          val user = Urn("soundcloud", "users", "1")
          val trackUrn = Urn("soundcloud", "tracks", "48786981")
          val path = s"/users/1/favorites/48786981$queryString"

          when(likesService.userTrackLikeForUrn(session, user, trackUrn))
            .thenReturn(trackRepresentationResult)

          val response = get(path)
          response.status ==== Status.InternalServerError
          response.contentString ==== expectedResponse
        }
      }
    }

    "GET /me/favorites/:trackId" >> {
      "with a successful response from tracks service" >> {
        "returns track" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "1")
          val urn = Urn("soundcloud", "tracks", "48786981")
          val path = s"/me/favorites/48786981$queryString"

          stubUserTrackLikeForUrn(user, urn, Some(trackRepresentation))

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== Json.toJson(trackRepresentation).toString()
        }

        "returns not found if empty" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "1")
          val urn = Urn("soundcloud", "tracks", "48786981")
          val path = s"/me/favorites/48786981$queryString"

          stubUserTrackLikeForUrn(user, urn, None)

          val response = get(path)
          response.status ==== Status.NotFound
          response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
        }
      }

      "with an error response from tracks service" >> {
        "returns an error response with message" in new TracksForUserContext with ErrorResponse {
          val user = Urn("soundcloud", "users", "1")
          val trackUrn = Urn("soundcloud", "tracks", "48786981")
          val path = s"/me/favorites/48786981$queryString"

          when(likesService.userTrackLikeForUrn(session, user, trackUrn))
            .thenReturn(trackRepresentationResult)

          val response = get(path)
          response.status ==== Status.InternalServerError
          response.contentString ==== expectedResponse
        }
      }
    }

    "GET /users/:userId/favorites" >> {
      "with a successful response from tracks service" >> {
        "returns tracks" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "1")
          val path = s"/users/1/favorites/$queryString"

          stubUserTracksLikes(user, path, tracksCollection)

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== expectedResponse
        }
      }

      "with an error response from tracks service" >> {
        "returns an error response with message" in new TracksForUserContext with ErrorResponse {
          val user = Urn("soundcloud", "users", "1")
          val path = s"/users/1/favorites/$queryString"

          when(likesService.userTracksLikes(session, user, paginationParams(path)))
            .thenReturn(trackRepresentationResult)

          val response = get(path)
          response.status ==== Status.InternalServerError
          response.contentString ==== "{\"error\":\"500 - Internal Server Error\"}"
        }
      }
    }

    "GET /me/favorites" >> {
      "with a successful response from tracks service" >> {
        "returns tracks" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "1")
          val path = s"/me/favorites/$queryString"

          stubUserTracksLikes(user, path, tracksCollection)

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== expectedResponse
        }
      }

      "with an error response from tracks service" >> {
        "returns an error response with message" in new TracksForUserContext with ErrorResponse {
          val user = Urn("soundcloud", "users", "1")
          val path = s"/me/favorites/$queryString"

          when(likesService.userTracksLikes(session, user, paginationParams(path)))
            .thenReturn(trackRepresentationResult)

          val response = get(path)
          response.status ==== Status.InternalServerError
          response.contentString ==== "{\"error\":\"500 - Internal Server Error\"}"
        }
      }
    }
  }
}
