package com.soundcloud.publicApiStrangler.handler

import java.net.URL
import java.util.TimeZone

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.service.UserTracksService
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentation,
  TrackRepresentationSpecContext
}
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.joda.time.{DateTime, DateTimeZone}
import org.mockito.Mockito.when
import play.api.libs.json.Json

class UserTracksHandlerSpec extends UnitSpecification with TrackRepresentationSpecContext {

  val mockTrackRepresentation = createTrackRepresentation()

  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  DateTimeZone.setDefault(DateTimeZone.UTC)

  trait Context extends HandlerSpecificationScope {
    val session = loggedInSession(Urn("soundcloud", "users", "1"))
    val userAuthentication = new FakeUserAuthentication(session)

    val userTracksService = mock[UserTracksService]
    val telemetry = Telemetry.createIsolatedInstance

    val handler = new UserTracksHandler(
      userAuthentication,
      userTracksService,
      "https://api.soundcloud.com",
      telemetry
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
          path: String,
          collection: Collection[TrackRepresentation]
      ) = {
        when(userTracksService.userTracks(session, user, paginationParams(path)))
          .thenReturn(Future.value(collection))
      }
    }

    trait SuccessfulResponse extends TrackRepresentationSpecContext with TracksForUserContext {
      val trackRepresentation = createTrackRepresentation()
      val tracksCollection = Collection(List(trackRepresentation), None)
      val expectedResponse = Collection.getRepresentation(tracksCollection, true)
    }

    "GET /users/:id/tracks" >> {
      "with a successful response from tracks service" >> {
        "returns tracks" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "7110")

          val path = s"/users/7110/tracks$queryString"

          stubService(user, path, tracksCollection)

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== expectedResponse
        }
      }
    }

    "GET /me/tracks" >> {
      "with a successful response from tracks service" >> {
        "returns tracks" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "1")
          val path = s"/me/tracks$queryString"

          stubService(user, path, tracksCollection)

          val response = get(path)
          response.status ==== Status.Ok
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
      val expected404Response = "{\"errors\":[{\"error_message\":\"404 - Not Found\"}]}"
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
      }
    }
  }
}
