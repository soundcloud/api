package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.TrackRepresentationsService
import com.twitter.finagle.http.Status
import com.twitter.util.Future

class UserTracksControllerSpec extends InjectionBasedControllerSpecification {

  trait Context extends Scope {
    val userAuthentication = fakeUserAuthentication(anonymousSession)
    val mothershipDispatcher = mock[TrackMothershipDispatcherWithCounts]
    val tracksService = mock[TrackRepresentationsService]
    val telemetry = mock[Telemetry]

    val userRelatedMothershipDispatcher = mock[UserRelatedMothershipDispatcher]
    val controller = new UserTracksController(
      userAuthentication,
      mothershipDispatcher,
      tracksService,
      telemetry)

    val success = Future.value(new ResponseBuilder().status(200))
  }


  "GET /users/:id/tracks" >> {
    "falls back to Mothership" in new Context {
      val response = get(controller, "/users/7110/tracks")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with trailing slash" in new Context {
      val response = get(controller, "/users/7110/tracks/")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with .json" in new Context {
      val response = get(controller, "/users/7110/tracks.json")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with .json and trailing slash" in new Context {
      val response = get(controller, "/users/7110/tracks.json/")
      response.status ==== Status.Ok
    }
  }
}
