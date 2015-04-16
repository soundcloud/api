package com.soundcloud.publicApiStrangler.rateLimiting

import com.twitter.util.Future

trait RateLimiter {
  def rateLimit: RateLimit
  def advanceRateLimitStatus(apiClient: ApiClient): Future[RateLimitStatus]
}
