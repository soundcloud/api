package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.scalakit.test.UnitSpecification
import org.specs2.time.NoTimeConversions
import com.twitter.util.TimeConversions._

class RateLimitSpec extends UnitSpecification with NoTimeConversions {

  "RateLimit" should {
    "parse a comma-separated list of general rate limits into a set" in {
      val s = "15000 per 1.day,500 per 30.minutes, 30 per 2.seconds"
      RateLimit.General.parse(s) ==== Set(
        RateLimit.General(1.day, 15000),
        RateLimit.General(30.minutes, 500),
        RateLimit.General(2.seconds, 30)
      )
    }

    "parse a single rate limit into a one-element set" in {
      val s = "15000 per 1.day"
      RateLimit.General.parse(s) ==== Set(RateLimit.General(1.day, 15000))
    }
  }
}
