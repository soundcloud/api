package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.ratelimiting.types.RateLimit
import com.soundcloud.scalakit.test.UnitSpecification
import com.soundcloud.scalakit.{ResourceName, Urn}
import org.joda.time.Period
import org.specs2.time.NoTimeConversions

class ClientSpecificRateLimitSpec extends UnitSpecification with NoTimeConversions {

  "A ClientSpecificRateLimit" should {

    val limit = ClientSpecificRateLimit(
      ApiClient(Urn("soundcloud", "applications", "21329")),
      RateLimit.General(Period.seconds(3), 300),
      ResourceName("wobbly")
    )

    "provide the correct counter key" in {
      limit.counterCacheKey ==== "WOBBLY.rateLimit.client_21329.300_requests_per_3_seconds.counter"
    }

    "provide the correct expiry key" in {
      limit.expiryCacheKey ==== "WOBBLY.rateLimit.client_21329.300_requests_per_3_seconds.expiry"
    }
  }
}
