package com.soundcloud.publicApiStrangler

import com.soundcloud.scalakit.test.UnitSpecification
import com.soundcloud.testutilities.SpinningUpAppSupport
import com.twitter.finagle.http.Status

class HealthCheckSpec extends UnitSpecification with SpinningUpAppSupport {

  "Public API Strangler" should {

    "return success when probing health check endpoint" in new Scope {
      val server = TestServer(dockerHostName, 5000)

      server.get("/-/health").status ==== Status.Ok.code
    }
  }
}
