package com.soundcloud.publicApiStrangler

import com.soundcloud.scalakit.test.UnitSpecification
import com.soundcloud.testutilities.SpinningUpAppSupport
import com.twitter.finagle.http.{HeaderMap, Status}

class SessionExemptedEndpointsSpec extends UnitSpecification with SpinningUpAppSupport {

  "Public API Strangler" should {

    trait Context extends Scope {
      val server = TestServer(dockerHostName, 5000)
    }

    "return success when probing health check endpoint" in new Context {

      server.get("/-/health").status ==== Status.Ok.code
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
