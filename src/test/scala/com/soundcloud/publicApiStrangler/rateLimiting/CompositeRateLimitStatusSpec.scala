package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.ratelimiting.core.{RateLimitMode, RateLimit}
import org.joda.time.Period

class CompositeRateLimitStatusSpec extends UnitSpecification {
  "Composite rate limit filter" should {
    "keep and return reached and enforced rate limit statuses" in {
      val status = CompositeRateLimitStatus(Set(
        RateLimitStatus(RateLimit(null, Period.hours(1), 90, RateLimitMode.Enforcing), 90, None),   // enforced and reached
        RateLimitStatus(RateLimit(null, Period.hours(1), 90, RateLimitMode.Enforcing), 80, None),   // enforced but advancing
        RateLimitStatus(RateLimit(null, Period.hours(1), 100, RateLimitMode.Enforcing), 100, None), // enforced and reached
        RateLimitStatus(RateLimit(null, Period.hours(1), 90, RateLimitMode.Probing), 80, None),     // probed and advancing
        RateLimitStatus(RateLimit(null, Period.hours(1), 90, RateLimitMode.Probing), 90, None)      // probed and reached
      ))

      val filteredStatus = status.keepingReachedEnforcedRateLimitStatuses

      filteredStatus ==== Some(CompositeRateLimitStatus(Set(
        RateLimitStatus(RateLimit(null, Period.hours(1), 90, RateLimitMode.Enforcing), 90, None),   // enforced and reached
        RateLimitStatus(RateLimit(null, Period.hours(1), 100, RateLimitMode.Enforcing), 100, None)  // enforced and reached
      )))
    }

    "return None if no rate limit status is in 'reached' status and enforced" in {
      val status = CompositeRateLimitStatus(Set(
        RateLimitStatus(RateLimit(null, Period.hours(1), 90, RateLimitMode.Enforcing), 80, None),   // enforced but advancing
        RateLimitStatus(RateLimit(null, Period.hours(1), 90, RateLimitMode.Probing), 80, None),     // probed and advancing
        RateLimitStatus(RateLimit(null, Period.hours(1), 90, RateLimitMode.Probing), 90, None)      // probed and reached
      ))

      val filteredStatus = status.keepingReachedEnforcedRateLimitStatuses

      filteredStatus ==== None
    }
  }
}
