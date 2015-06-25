package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.ratelimiting.core.{Bucket, RateLimitConfiguration, RateLimitIdentity, RateLimitMode}
import org.joda.time.Period

class CompositeRateLimitStatusSpec extends UnitSpecification {
  def createStatus(requestCount: Int, max: Int, mode: RateLimitMode) = {
    RateLimitStatus(
      RateLimitIdentity(Bucket.ByClient, max, Period.hours(1), mode, endpointGroupName = null),
      requestCount, None
    )
  }

  "Composite rate limit filter" should {
    "keep and return reached and enforced rate limit statuses" in {
      val status = CompositeRateLimitStatus(Set(
        createStatus(90, 90, RateLimitMode.Enforcing), // enforced and reached
        createStatus(80, 90, RateLimitMode.Enforcing), // enforced but advancing
        createStatus(100, 100, RateLimitMode.Enforcing), // enforced and reached
        createStatus(80, 90, RateLimitMode.Probing), // probed and advancing
        createStatus(90, 90, RateLimitMode.Probing) // probed and reached
      ))

      val filteredStatus = status.keepingReachedEnforcedRateLimitStatuses

      filteredStatus ==== Some(CompositeRateLimitStatus(Set(
        createStatus(90, 90, RateLimitMode.Enforcing),   // enforced and reached
        createStatus(100, 100, RateLimitMode.Enforcing)  // enforced and reached
      )))
    }

    "return None if no rate limit status is in 'reached' status and enforced" in {
      val status = CompositeRateLimitStatus(Set(
        createStatus(80, 90, RateLimitMode.Enforcing), // enforced but advancing
        createStatus(80, 90, RateLimitMode.Probing), // probed and advancing
        createStatus(90, 90, RateLimitMode.Probing) // probed and reached
      ))

      val filteredStatus = status.keepingReachedEnforcedRateLimitStatuses

      filteredStatus ==== None
    }
  }
}
