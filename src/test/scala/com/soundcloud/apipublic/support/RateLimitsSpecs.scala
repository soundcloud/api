package com.soundcloud.apipublic.support

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.apipublic.test.UnitSpecification
import com.twitter.finagle.http.Request

class RateLimitsSpecs extends UnitSpecification {
  trait Context extends Scope {
    def trackPlayClassifierMatches(request: HandlerRequest): Boolean =
      RateLimits.playsRateLimiter.classifier
        .applyOrElse(request, (_: HandlerRequest) => false)
  }

  "track play" >> {
    "does not match requests to get streams" in new Context {
      trackPlayClassifierMatches(HandlerRequest(Request("http://api/tracks/soundcloud:tracks:2"))) ==== false
    }

    "call does match classifier" in new Context {
      trackPlayClassifierMatches(HandlerRequest(Request("http://api/tracks/soundcloud:tracks:2/streams"))) ==== true
    }
  }
}
