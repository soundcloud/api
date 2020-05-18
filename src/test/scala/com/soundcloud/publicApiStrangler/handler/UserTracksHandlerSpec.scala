package com.soundcloud.publicApiStrangler.handler

import java.net.URL
import java.util.TimeZone

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.service.UserTracksService
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentationLikeSpecContext,
  TracksCollection
}
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures.contentsOf
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.joda.time.{DateTime, DateTimeZone}
import org.mockito.Mockito.when

class UserTracksHandlerSpec extends UnitSpecification {
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  DateTimeZone.setDefault(DateTimeZone.UTC)

  trait Context extends HandlerSpecificationScope {
    val session = loggedInSession(Urn("soundcloud", "users", "1"))
    val userAuthentication = new FakeUserAuthentication(session)

    val userTracksService = mock[UserTracksService]
    val telemetry = Telemetry.createIsolatedInstance
    val exceptionCollector = new ExceptionCollector(telemetry)

    val handler = new UserTracksHandler(
      userAuthentication,
      userTracksService,
      "https://api.soundcloud.com",
      exceptionCollector
    )

    override def routingDefinitions = Routing.forUserTracksHandler(handler)
  }

  "Getting tracks" >> {
    trait TracksForUserContext extends Context {
      val queryString =
        "?limit=1&offset=2&linked_partitioning=yes-please&created_at[from]=2017-01-01%2010:00:00&created_at[to]=2017-01-15%2010:00:00"

      def paginationParams(path: String) =
        TrackPagination(
          Some(1),
          Some(2),
          true,
          Some(new DateTime(2017, 1, 1, 10, 0, 0)),
          Some(new DateTime(2017, 1, 15, 10, 0, 0)),
          new URL("https://api.soundcloud.com" + path)
        )

      def stubService(
          user: Urn,
          paths: List[String],
          tracksCollection: TracksCollection
      ) = {
        paths.foreach(path => {
          when(userTracksService.userTracks(session, user, paginationParams(path)))
            .thenReturn(Future.value(tracksCollection))
        })
      }
    }

    trait SuccessfulResponse extends TrackRepresentationLikeSpecContext with TracksForUserContext {
      val tracksCollection = TracksCollection(List(createTrackRepresentation()), None)
      val expectedResponse = contentsOf("tracks", "track_representation_response").toString()
    }

    trait ErrorResponse extends TracksForUserContext {
      val errorMessage = "500 - Internal Server Error"
      val trackRepresentationResult =
        Future.exception(new RuntimeException("An unexpected error occurred while fetching a tracks"))
      // Note that exception text is _not_ included in expected response
      val expectedResponse =
        s"""{"error":"$errorMessage"}"""
    }

    "GET /users/:id/tracks" >> {
      "with a successful response from tracks service" >> {
        "returns tracks" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "7110")

          val userTracksPaths = List(
            s"/users/7110/tracks$queryString",
            s"/users/7110/tracks/$queryString",
            s"/users/7110/tracks.json$queryString",
            s"/users/7110/tracks.json/$queryString"
          )

          stubService(user, userTracksPaths, tracksCollection)

          userTracksPaths.foreach(path => {
            val response = get(path)
            response.status ==== Status.Ok
            response.contentString ==== expectedResponse
          })
        }
      }

      "with an error response from tracks service" >> {
        "returns an error response with message" in new TracksForUserContext with ErrorResponse {
          val user = Urn("soundcloud", "users", "7110")
          val path = s"/users/7110/tracks$queryString"

          when(userTracksService.userTracks(session, user, paginationParams(path)))
            .thenReturn(trackRepresentationResult)

          val response = get(path)
          response.status ==== Status.InternalServerError
          response.contentString ==== expectedResponse
        }
      }
    }

    "GET /me/tracks" >> {
      "with a successful response from tracks service" >> {
        "returns tracks" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "1")

          val meTracksPaths = List(
            s"/me/tracks$queryString",
            s"/me/tracks/$queryString",
            s"/me/tracks.json$queryString",
            s"/me/tracks.json/$queryString"
          )

          stubService(user, meTracksPaths, tracksCollection)

          meTracksPaths.foreach(path => {
            val response = get(path)
            response.status ==== Status.Ok
            response.contentString ==== expectedResponse
          })
        }
      }

      "with an error response from tracks service" >> {
        "returns an error response with message" in new TracksForUserContext with ErrorResponse {
          val user = Urn("soundcloud", "users", "1")
          val path = s"/me/tracks$queryString"

          when(userTracksService.userTracks(session, user, paginationParams(path)))
            .thenReturn(trackRepresentationResult)

          val response = get(path)
          response.status ==== Status.InternalServerError
          response.contentString ==== expectedResponse
        }
      }
    }
  }
}
