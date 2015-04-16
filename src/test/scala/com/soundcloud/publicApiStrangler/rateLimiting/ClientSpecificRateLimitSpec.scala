package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.scalakit.{ResourceName, Urn}
import com.soundcloud.scalakit.test.UnitSpecification
import org.specs2.time.NoTimeConversions

class ClientSpecificRateLimitSpec extends UnitSpecification with NoTimeConversions {
  import com.twitter.conversions.time.longToTimeableNumber

  "A ClientSpecificRateLimit" should {

    val limit = ClientSpecificRateLimit(
      ApiClient(Urn("soundcloud", "applications", "21329")),
      RateLimit.General(3L.seconds, 300),
      ResourceName("wobbly")
    )

    "provide the correct counter key" in {
      limit.counterKey ==== "WOBBLY.rateLimit.client_21329.300_requests_per_3_seconds.counter"
    }

    "provide the correct expiry key" in {
      limit.expiryKey ==== "WOBBLY.rateLimit.client_21329.300_requests_per_3_seconds.expiry"
    }
  }
}
