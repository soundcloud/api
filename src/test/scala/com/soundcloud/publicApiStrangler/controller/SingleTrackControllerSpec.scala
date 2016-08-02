package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.UserSessionBuilder
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.test.VerifiedMocks
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Future

class SingleTrackControllerSpec extends InjectionBasedControllerSpecification {

  trait Context extends Scope with VerifiedMocks {
    val fallback = mock[DispatchToMothershipHandler]
    val controller = new SingleTrackController(fakeUserAuthentication(session), fallback)
    val session = new UserSessionBuilder().build()

    when(fallback.dispatch(any[Request])).thenReturn(Future.value(new ResponseBuilder().status(200)))
  }

  "GET /tracks/:id" >> {
    "falls back to Mothership" in new Context {
      val response = get(controller, "/tracks/999")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with trailing slash" in new Context {
      val response = get(controller, "/tracks/999/")
      response.status ==== Status.Ok
    }
  }
}
