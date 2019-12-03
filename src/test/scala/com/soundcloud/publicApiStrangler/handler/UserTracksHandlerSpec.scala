package com.soundcloud.publicApiStrangler.handler

import java.net.URL
import java.util.TimeZone

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentationLikeSpecContext,
  TrackRepresentationsService,
  TracksRepresentationResult
}
import com.soundcloud.publicApiStrangler.support.ResultF.lift
import com.soundcloud.publicApiStrangler.support.{Bad, Good, StringError}
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.joda.time.{DateTimeZone, DateTime}
import org.mockito.Mockito.when

class UserTracksHandlerSpec extends UnitSpecification {
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  DateTimeZone.setDefault(DateTimeZone.UTC)

  trait Context extends HandlerSpecificationScope {
    val session = loggedInSession(Urn("soundcloud", "users", "1"))
    val userAuthentication = new FakeUserAuthentication(session)

    val mothershipDispatcher = mock[TrackMothershipDispatcherWithCounts]
    val tracksService = mock[TrackRepresentationsService]
    val telemetry = Telemetry.createIsolatedInstance

    val shouldUseTrackMetadata = () => Future.value(true)
    val handler = new UserTracksHandler(
      userAuthentication,
      mothershipDispatcher,
      tracksService,
      telemetry,
      shouldUseTrackMetadata,
      "https://api.soundcloud.com"
    )

    override def routingDefinitions = Routing.forUserTracksHandler(handler)
  }

  "GET /users/:id/tracks" >> {
    trait TracksForUserContext extends Context {
      val user = Urn("soundcloud", "users", "7110")
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
    }

    // with TrackRepresentationLikeSpecContext for easy creation of a default TrackRepresentation
    trait SuccessfulResponse extends TrackRepresentationLikeSpecContext {
      val tracksServiceResponse = lift(Good(TracksRepresentationResult(List(createTrackRepresentation()), None)))
      val expectedResponse =
        """{"collection":[{"kind":"track","id":1324,"created_at":"2015/02/15 16:47:27 +0000","user_id":3456,"duration":120,"commentable":false,"state":"finished","original_content_size":9001,"last_modified":"2016/08/08 13:28:53 +0000","sharing":"public","tag_list":"system:foo system:bar \"awesomeness:very high\" dubstep folk \"tag with spaces\"","permalink":"plsty-remix","streamable":true,"embeddable_by":"me","purchase_url":"http://example.com/buy/7890","purchase_title":"buy me pls","label_id":999,"genre":"future bass","title":"Baby Bash","description":"Follow @samstarling !","label_name":"Denis Owns","release":"DR012","track_type":"original","key_signature":"Emaj","isrc":"US-S1Z-99-00001","video_url":"http://example.com/video.mp4","bpm":120.7,"release_year":1991,"release_month":1,"release_day":2,"original_format":"vqf","license":"all-rights-reserved","uri":"https://api.soundcloud.com/tracks/1324","user":{"id":3456,"kind":"user","permalink":"giraffe","username":"Dr. G. Raffe","last_modified":"2016/10/10 11:21:36 +0000","uri":"https://api.soundcloud.com/users/3456","permalink_url":"https://soundcloud.com/denis","avatar_url":"https://example.com/giraffe.jpg"},"permalink_url":"http://soundcloud.com/nirvana/plsty-remix","artwork_url":"https://i1.sndcdn.com/artworks-FuwbhSJORvKH-0-large.jpg","stream_url":"https://api.soundcloud.com/tracks/1324/stream","download_url":"https://api.soundcloud.com/tracks/1324/download"}]}"""
    }

    trait ErrorResponse {
      val errorMessage = "foobar"
      val tracksServiceResponse = lift(Bad(StringError(errorMessage)))
      // Note that exception text is _not_ included in expected response
      val expectedResponse =
        s"""{"error":"$errorMessage"}"""
    }

    "with a successful response from tracks service" >> {
      "returns tracks" in new TracksForUserContext with SuccessfulResponse {
        List(
          s"/users/7110/tracks$queryString",
          s"/users/7110/tracks/$queryString",
          s"/users/7110/tracks.json$queryString",
          s"/users/7110/tracks.json/$queryString"
        ).foreach(path => {
          println(s"For path $path")
          when(tracksService.tracks(session, user, paginationParams(path))).thenReturn(tracksServiceResponse)

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== expectedResponse
        })
      }
    }

    "with an error response from tracks service" >> {
      "returns an error response with message" in new TracksForUserContext with ErrorResponse {
        val path = s"/users/7110/tracks$queryString"
        when(tracksService.tracks(session, user, paginationParams(path))).thenReturn(tracksServiceResponse)

        val response = get(path)
        response.status ==== Status.InternalServerError
        response.contentString ==== expectedResponse
      }
    }
  }
}
