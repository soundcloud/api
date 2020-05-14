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
  TrackRepresentationLike,
  TrackRepresentationLikeSpecContext,
  TracksCollection
}
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures.contentsOf
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.joda.time.{DateTime, DateTimeZone}
import org.mockito.Mockito.when
import play.api.libs.json.Json

class UserTracksHandlerSpec extends UnitSpecification with TrackRepresentationLikeSpecContext {

  val mockTrackRepresentation = createTrackRepresentation()

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

      def stubService(
          user: Urn,
          trackUrn: Urn,
          trackRepresentation: Option[TrackRepresentationLike]
      ) = {
        when(likesService.userTrackLikeForUrn(session, user, trackUrn))
          .thenReturn(Future.value(trackRepresentation))
      }
    }

    trait SuccessfulResponse extends TrackRepresentationLikeSpecContext with TracksForUserContext {
      val trackRepresentation = createTrackRepresentation()
      val tracksCollection = TracksCollection(List(trackRepresentation), None)
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

  "Getting single track" >> {
    trait TrackForUserContext extends Context {
      def stubService(
          trackUrn: Urn
      ) = {
        when(userTracksService.userTrack(trackUrn, session, "1", Some("s3cret")))
          .thenReturn(Future.value(Some(mockTrackRepresentation)))

        when(userTracksService.userTrack(trackUrn, session, "2", Some("s3cret")))
          .thenReturn(Future.value(None))
      }
    }

    trait SuccessfulResponse {
      val expectedResponse = mockTrackRepresentation
    }

    trait ErrorResponse {
      val expected404Response = "{\"error\":\"404 - Not Found\"}"
      val expected500Response = "An unexpected error occured while fetching a track"
    }

    "GET users/:userId/tracks/:trackId" >> {
      "with a successful response from tracks service" >> {
        "return track" in new TrackForUserContext with SuccessfulResponse {
          val path = "/users/1/tracks/987"

          stubService(Urn("soundcloud", "tracks", "987"))

          val response = get(path, Map("secret_token" -> "s3cret"))
          response.status ==== Status.Ok
          response.contentString ==== Json.stringify(Json.toJson(expectedResponse))

        }
      }

      "with a 404 from tracks service" >> {
        "returns an error response" in new TrackForUserContext with ErrorResponse {
          val path = "/users/2/tracks/404"

          stubService(Urn("soundcloud", "tracks", "404"))

          val response = get(path, Map("secret_token" -> "s3cret"))

          response.status ==== Status(404)
          response.statusCode ==== 404
          response.contentString ==== expected404Response
        }
      }

      "with a 500 from tracks service" >> {
        "returns an error response" in new TrackForUserContext with ErrorResponse {
          val path = "/users/2/tracks/500"

          when(userTracksService.userTrack(Urn("soundcloud", "tracks", "500"), session, "2", Some("s3cret")))
            .thenReturn(Future.exception(new RuntimeException))

          val response = get(path, Map("secret_token" -> "s3cret"))

          response.status ==== Status.InternalServerError
          response.statusCode ==== 500
          response.contentString ==== expected500Response
        }
      }

      "GET me/tracks/:trackId" >> {
        "with a successful response from tracks service" >> {
          "return track" in new TrackForUserContext with SuccessfulResponse {
            val path = "/me/tracks/987"

            stubService(Urn("soundcloud", "tracks", "987"))
            val response = get(path, Map("secret_token" -> "s3cret"))

            response.status ==== Status.Ok
            response.statusCode ==== 200
            response.contentString ==== Json.stringify(Json.toJson(expectedResponse))
          }
        }

        "with a 404 from tracks service" >> {
          "returns an error response with message" in new TrackForUserContext with ErrorResponse {
            val path = "/me/tracks/404"

            when(userTracksService.userTrack(Urn("soundcloud", "tracks", "404"), session, "1", Some("s3cret")))
              .thenReturn(Future.value(None))

            val response = get(path, Map("secret_token" -> "s3cret"))

            response.status ==== Status(404)
            response.statusCode ==== 404
            response.contentString ==== expected404Response
          }
        }

        "with a 500 from tracks service" >> {
          "returns an error response" in new TrackForUserContext with ErrorResponse {
            val path = "/me/tracks/500"

            when(userTracksService.userTrack(Urn("soundcloud", "tracks", "500"), session, "1", Some("s3cret")))
              .thenReturn(Future.exception(new RuntimeException))

            val response = get(path, Map("secret_token" -> "s3cret"))

            response.status ==== Status.InternalServerError
            response.statusCode ==== 500
            response.contentString ==== expected500Response
          }
        }
      }
    }

    "GET /users/:userId/favorites/:trackId" >> {
      "with a successful response from tracks service" >> {
        "returns track" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "1")
          val urn = Urn("soundcloud", "tracks", "48786981")
          val path = s"/users/1/favorites/48786981$queryString"

          stubService(user, urn, Some(trackRepresentation))

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== Json.toJson(trackRepresentation).toString()
        }

        "returns not found if empty" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "1")
          val urn = Urn("soundcloud", "tracks", "48786981")
          val path = s"/users/1/favorites/48786981$queryString"

          stubService(user, urn, None)

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

          stubService(user, urn, Some(trackRepresentation))

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== Json.toJson(trackRepresentation).toString()
        }

        "returns not found if empty" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "1")
          val urn = Urn("soundcloud", "tracks", "48786981")
          val path = s"/me/favorites/48786981$queryString"

          stubService(user, urn, None)

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
  }
}
