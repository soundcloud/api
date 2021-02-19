package com.soundcloud.publicApiStrangler

import com.soundcloud.testutilities.SpinningUpAppSupport
import com.twitter.finagle.http.{HeaderMap, Status}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class SessionExemptedEndpointsSpec extends Specification with SpinningUpAppSupport {

  "Public API Strangler" should {

    trait Context extends Scope {
      val server = TestServer("publicapistrangler", 5000)
    }

    "return success when probing crossdomain filters endpoint" in new Context {

      server.get("/crossdomain.xml").status ==== Status.Ok.code
      server.get("/robots.txt").status ==== Status.Ok.code
    }

    "return success for multipart request with oauth2 token" in new Context {
      private val multipartHeaders = HeaderMap(("Content-Type", "multipart/form-data;"))

      server.post("/oauth2/token", "", multipartHeaders).status ==== Status.Ok.code
    }
  }
}
