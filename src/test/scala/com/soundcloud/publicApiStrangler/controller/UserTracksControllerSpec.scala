package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.TrackRepresentationsService
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.mockito.Mockito.when
import org.specs2.mock.mockito.MockitoMatchers.any

class UserTracksControllerSpec extends InjectionBasedControllerSpecification {

  trait Context extends Scope {
    val userAuthentication = fakeUserAuthentication(anonymousSession)
    val mothershipDispatcher = mock[TrackMothershipDispatcherWithCounts]
    val tracksService = mock[TrackRepresentationsService]
    val telemetry = mock[Telemetry]

    val userRelatedMothershipDispatcher = mock[UserRelatedMothershipDispatcher]

    def compareResponse: Boolean

    val shouldCompareResponse = () => Future.value(compareResponse)
    val controller = new UserTracksController(
      userAuthentication,
      mothershipDispatcher,
      tracksService,
      telemetry,
      shouldCompareResponse)

    val success = Future.value(new ResponseBuilder().status(200))
  }

  "compareResponse = false" >> {
    trait ShouldNotCompareResponse extends Context {
      override def compareResponse = false
    }

    "GET /users/:id/tracks" >> {
      "falls back to Mothership" in new ShouldNotCompareResponse {
        when(mothershipDispatcher.request(any[Request])).thenReturn(success)

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
}
