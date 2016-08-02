package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.UserSessionBuilder
import com.soundcloud.publicApiStrangler.SingleTrackEndpointRollout
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.VerifiedMocks
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Future

class SingleTrackControllerSpec extends InjectionBasedControllerSpecification {

  trait Context extends Scope with VerifiedMocks {
    val fallback = mock[DispatchToMothershipHandler]
    val tracksService = mock[DispatchToMothershipHandler]
    val singleTrackEndpointRollout = mock[SingleTrackEndpointRollout]

    val controller = new SingleTrackController(fakeUserAuthentication(session), fallback, tracksService, singleTrackEndpointRollout)

    val session = new UserSessionBuilder().build()
    val trackUrn = Urn("soundcloud:tracks:987")
  }

  "GET /tracks/:id" >> {

    "returns 404 on unexpected track id" in new Context {
      val response = get(controller, "/tracks/__12")
      response.status ==== Status.NotFound
      response.body ==== "Track id is not valid."
    }

    "fallsback when the rollout is off" >> {

      "without trailing slash" in new Context {
        when(singleTrackEndpointRollout.strangleSingleTrackEndpoint(trackUrn)).thenReturn(Future.False)
        when(fallback.dispatch(any[Request])).thenReturn(Future.value(new ResponseBuilder().status(200)))

        val response = get(controller, "/tracks/987")
        response.status ==== Status.Ok
      }

      "with trailing slash" in new Context {
        when(singleTrackEndpointRollout.strangleSingleTrackEndpoint(trackUrn)).thenReturn(Future.False)
        when(fallback.dispatch(any[Request])).thenReturn(Future.value(new ResponseBuilder().status(200)))

        val response = get(controller, "/tracks/987/")
        response.status ==== Status.Ok
      }
    }

    "uses tracks service when the rollout is on" >> {

      "without trailing slash" in new Context {
        when(singleTrackEndpointRollout.strangleSingleTrackEndpoint(trackUrn)).thenReturn(Future.True)
        when(tracksService.dispatch(any[Request])).thenReturn(Future.value(new ResponseBuilder().status(200)))

        val response = get(controller, "/tracks/987")
        response.status ==== Status.Ok
      }

      "with trailing slash" in new Context {
        when(singleTrackEndpointRollout.strangleSingleTrackEndpoint(trackUrn)).thenReturn(Future.True)
        when(tracksService.dispatch(any[Request])).thenReturn(Future.value(new ResponseBuilder().status(200)))

        val response = get(controller, "/tracks/987/")
        response.status ==== Status.Ok
      }
    }
  }
}
