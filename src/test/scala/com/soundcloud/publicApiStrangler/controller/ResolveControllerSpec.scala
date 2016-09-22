package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.test.VerifiedMocks
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Future

class ResolveControllerSpec extends InjectionBasedControllerSpecification with Fixtures {

  trait Context extends Scope with VerifiedMocks {
    val fallback = mock[DispatchToMothershipHandler]
    lazy val controller = new ResolveController(fallback)
    when(fallback.dispatch(any[Request])).thenReturn(Future.value(new ResponseBuilder().status(200)))
  }

  "GET /resolve" >> {
    "falls back to Mothership" in new Context {
      val response = get(controller, "/resolve")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with .json" in new Context {
      val response = get(controller, "/resolve.json")
      response.status ==== Status.Ok
    }
  }
}
