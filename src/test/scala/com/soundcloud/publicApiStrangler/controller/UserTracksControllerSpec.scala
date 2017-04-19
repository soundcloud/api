package com.soundcloud.publicApiStrangler.controller

import java.net.URL
import java.util.TimeZone

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.Urn
import com.soundcloud.jvmkit.module.experimental.result.Good
import com.soundcloud.jvmkit.module.experimental.result.ResultF.lift
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.jvmkit.test.InMemoryConfig
import com.soundcloud.publicApiStrangler.{TrackRepresentationsService, TracksRepresentationResult}
import com.soundcloud.publicApiStrangler.representation.TrackRepresentationLikeSpecContext
import com.soundcloud.publicApiStrangler.service.TrackPagination
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.joda.time.{DateTimeZone, LocalDateTime}
import org.mockito.Mockito.when

class UserTracksControllerSpec extends InjectionBasedControllerSpecification {
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  DateTimeZone.setDefault(DateTimeZone.UTC)

  trait Context extends Scope {
    val session = loggedInSession(Urn("soundcloud:users:1"))
    val userAuthentication = fakeUserAuthentication(session)

    val mothershipDispatcher = mock[TrackMothershipDispatcherWithCounts]
    val tracksService = mock[TrackRepresentationsService]
    val telemetry = new Telemetry(new InMemoryConfig)

    def useTrackMetadata: Boolean

    val shouldUseTrackMetadata = () => Future.value(useTrackMetadata)
    val controller = new UserTracksController(
      userAuthentication,
      mothershipDispatcher,
      tracksService,
      telemetry,
      shouldUseTrackMetadata,
      "https://api.soundcloud.com")

    val mothershipSuccess = Future.value(new ResponseBuilder().status(200))
  }

  "useTrackMetadata = false" >> {
    trait ShouldNotCallTrackMetadataResponse extends Context {
      override def useTrackMetadata = false
    }

    "GET /users/:id/tracks" >> {
      "falls back to Mothership" in new ShouldNotCallTrackMetadataResponse {
        when(mothershipDispatcher.request(any[Request])).thenReturn(mothershipSuccess)

        List(
          "/users/7110/tracks",
          "/users/7110/tracks/",
          "/users/7110/tracks.json",
          "/users/7110/tracks.json/"
        ).foreach(path => {
          println(s"For path $path")
          val response = get(controller, path)
          response.status ==== Status.Ok
        })
      }
    }
  }

  "useTrackMetadata = true" >> {
    // with TrackRepresentationLikeSpecContext for easy creation of a default TrackRepresentation
    trait ShouldCallTrackMetadataResponse extends Context  with TrackRepresentationLikeSpecContext {
      override def useTrackMetadata = true

      val tracksServiceSuccess = lift(Good(TracksRepresentationResult(List(createTrackRepresentation()), None)))
      val expectedResponse = """{"collection":[{"kind":"track","id":1324,"created_at":"2015/02/15 16:47:27 +0000","user_id":3456,"duration":120,"commentable":false,"state":"finished","original_content_size":9001,"last_modified":"2016/08/08 13:28:53 +0000","sharing":"public","tag_list":"system:foo system:bar \"awesomeness:very high\" dubstep folk \"tag with spaces\"","permalink":"plsty-remix","streamable":true,"embeddable_by":"me","purchase_url":"http://example.com/buy/7890","purchase_title":"buy me pls","label_id":999,"genre":"future bass","title":"Baby Bash","description":"Follow @samstarling !","label_name":"Denis Owns","release":"DR012","track_type":"original","key_signature":"Emaj","isrc":"US-S1Z-99-00001","video_url":"http://example.com/video.mp4","bpm":120.7,"release_year":1991,"release_month":1,"release_day":2,"original_format":"vqf","license":"all-rights-reserved","uri":"https://api.soundcloud.com/tracks/1324","user":{"id":3456,"kind":"user","permalink":"giraffe","username":"Dr. G. Raffe","last_modified":"2016/10/10 11:21:36 +0000","uri":"https://api.soundcloud.com/users/3456","permalink_url":"https://soundcloud.com/denis","avatar_url":"https://example.com/giraffe.jpg"},"permalink_url":"http://soundcloud.com/nirvana/plsty-remix","artwork_url":"https://i1.sndcdn.com/artworks-FuwbhSJORvKH-0-large.jpg","stream_url":"https://api.soundcloud.com/tracks/1324/stream","download_url":"https://api.soundcloud.com/tracks/1324/download"}]}"""

      val user = Urn("soundcloud:users:7110")
    }

    "GET /users/:id/tracks" >> {
      "requests to both mothership and tracks service" in new ShouldCallTrackMetadataResponse {
        val queryString = "?limit=1&offset=2&linked_partitioning=yes-please&created_at[from]=2017-01-01%2010:00:00&created_at[to]=2017-01-15%2010:00:00"

        List(
          s"/users/7110/tracks$queryString",
          s"/users/7110/tracks/$queryString",
          s"/users/7110/tracks.json$queryString",
          s"/users/7110/tracks.json/$queryString"
        ).foreach(path => {
          println(s"For path $path")
          val paginationParams = TrackPagination(Some(1), Some(2), true,
            Some(new LocalDateTime(2017, 1, 1, 10, 0, 0)),
            Some(new LocalDateTime(2017, 1, 15, 10, 0, 0)),
            new URL("https://api.soundcloud.com" + path))
          when(tracksService.tracks(session, user, paginationParams)).thenReturn(tracksServiceSuccess)

          val response = get(controller, path)
          response.status ==== Status.Ok
          response.body ==== expectedResponse
        })
      }
    }
  }
}
