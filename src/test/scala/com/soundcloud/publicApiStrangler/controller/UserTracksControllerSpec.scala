package com.soundcloud.publicApiStrangler.controller

import java.util.TimeZone

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.Urn
import com.soundcloud.jvmkit.module.util.Good
import com.soundcloud.jvmkit.module.util.ResultF.lift
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.jvmkit.test.InMemoryConfig
import com.soundcloud.publicApiStrangler.TrackRepresentationsService
import com.soundcloud.publicApiStrangler.representation.TrackRepresentationLike
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

    def compareResponse: Boolean

    val shouldCompareResponse = () => Future.value(compareResponse)
    val controller = new UserTracksController(
      userAuthentication,
      mothershipDispatcher,
      tracksService,
      telemetry,
      shouldCompareResponse)

    val mothershipSuccess = Future.value(new ResponseBuilder().status(200))
  }

  "compareResponse = false" >> {
    trait ShouldNotCompareResponse extends Context {
      override def compareResponse = false
    }

    "GET /users/:id/tracks" >> {
      "falls back to Mothership" in new ShouldNotCompareResponse {
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

  "compareResponse = true" >> {
    trait ShouldCompareResponse extends Context {
      override def compareResponse = true

      val tracksServiceSuccess = lift(Good(List.empty[TrackRepresentationLike]))

      val user = Urn("soundcloud:users:7110")
      val paginationParams = TrackPagination(Some(1), Some(2), true,
        Some(new LocalDateTime(2017, 1, 1, 10, 0, 0)),
        Some(new LocalDateTime(2017, 1, 15, 10, 0, 0)))
    }

    "GET /users/:id/tracks" >> {
      "requests to both mothership and tracks service" in new ShouldCompareResponse {
        when(mothershipDispatcher.request(any[Request])).thenReturn(mothershipSuccess)
        when(tracksService.tracks(session, user, paginationParams)).thenReturn(tracksServiceSuccess)

        val queryString = "?limit=1&offset=2&linked_partitioning=yes-please&created_at[from]=2017-01-01%2010:00:00&created_at[to]=2017-01-15%2010:00:00"

        List(
          s"/users/7110/tracks$queryString",
          s"/users/7110/tracks/$queryString",
          s"/users/7110/tracks.json$queryString",
          s"/users/7110/tracks.json/$queryString"
        ).foreach(path => {
          println(s"For path $path")
          val response = get(controller, path)
          response.status ==== Status.Ok
        })
      }
    }
  }
}
