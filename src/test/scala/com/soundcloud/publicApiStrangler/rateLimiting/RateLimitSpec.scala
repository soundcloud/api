package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.scalakit.test.UnitSpecification
import org.specs2.time.NoTimeConversions
import com.twitter.util.TimeConversions._

class RateLimitSpec extends UnitSpecification with NoTimeConversions {

  "RateLimit" should {
    "parse a comma-separated list of general rate limits into a set" in {
      val s = "15000 per PT24H,500 per PT30M, 30 per PT2S"
      RateLimit.General.parse(s) ==== Set(
        RateLimit.General(1.day, 15000),
        RateLimit.General(30.minutes, 500),
        RateLimit.General(2.seconds, 30)
      )
    }

    "parse a single rate limit into a one-element set" in {
      val s = "15000 per PT24H"
      RateLimit.General.parse(s) ==== Set(RateLimit.General(1.day, 15000))
    }
  }
}
