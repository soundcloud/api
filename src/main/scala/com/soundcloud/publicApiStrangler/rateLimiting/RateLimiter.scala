package com.soundcloud.publicApiStrangler.rateLimiting

import com.twitter.util.Future

trait RateLimiter {
  def advanceRateLimitStatus(apiClient: ApiClient): Future[RateLimitStatus]
}
