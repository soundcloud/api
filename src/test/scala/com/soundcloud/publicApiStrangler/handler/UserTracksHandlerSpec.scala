package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.handler.support.requestParser.AccessParams
import com.soundcloud.publicApiStrangler.service.UserTracksService
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackRepresentation,
  TrackRepresentationSpecContext
}
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Future
import org.joda.time.DateTimeZone
import org.mockito.Mockito.when
import play.api.libs.json.Json

import java.util.TimeZone

class UserTracksHandlerSpec extends UnitSpecification with TrackRepresentationSpecContext {

  val mockTrackRepresentation = createTrackRepresentationFromVisibleTrack()

  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  DateTimeZone.setDefault(DateTimeZone.UTC)

  trait Context extends HandlerSpecificationScope {
    val session = loggedInSession(Urn("soundcloud", "users", "1"))
    val userAuthentication = new FakeUserAuthentication(session)

    val userTracksService = mock[UserTracksService]

    val handler = new UserTracksHandler(
      userAuthentication,
      userTracksService
    )

    override def routingDefinitions = Routing.forUserTracksHandler(handler)
  }

  "Getting tracks" >> {
    trait TracksForUserContext extends Context {
      val queryString =
        "?limit=1&offset=2&linked_partitioning=yes-please&created_at[from]=2017-01-01%2010:00:00&created_at[to]=2017-01-15%2010:00:00"

      def paginationParams(path: String): CursorBasedPagination = {
        val mockRequest = Request(path)
        mockRequest.host = "localhost"
        CursorBasedPagination.build(mockRequest, Seq("linked_partitioning"))

      }

      def stubService(
          user: Urn,
          path: String,
          collection: Collection[TrackRepresentation],
          access: AccessParams = AccessParams.defaultAccess
      ) = {
        when(userTracksService.userTracks(session, user, access, paginationParams(path)))
          .thenReturn(Future.value(collection))
      }
    }

    trait SuccessfulResponse extends TrackRepresentationSpecContext with TracksForUserContext {
      val trackRepresentation = createTrackRepresentationFromVisibleTrack()
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

          stubService(user, path, tracksCollection, AccessParams.explicitAccess)

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
        "returns an error response with message" in new TrackForUserContext {
          val path = "/me/tracks/404"

          when(userTracksService.userTrack(Urn("soundcloud", "tracks", "404"), session, "1", Some("s3cret")))
            .thenReturn(Future.value(None))

          val response = get(path, Map("secret_token" -> "s3cret"))

          response.status ==== Status(404)
          response.statusCode ==== 404
        }
      }
    }
  }
}
