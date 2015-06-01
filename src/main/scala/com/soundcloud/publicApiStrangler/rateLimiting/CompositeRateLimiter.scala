package com.soundcloud.publicApiStrangler.rateLimiting

import com.twitter.finagle.http.Request
import com.twitter.util.Future

class CompositeRateLimiter(rateLimiter: RateLimiter) {

  def appliesTo(request: Request): Boolean = rateLimiter.appliesTo(request)

  def advanceRateLimitStatus(apiClient: ApiClient): Future[CompositeRateLimitStatus] = {
    rateLimiter.advanceRateLimitStatus(apiClient).map { status =>
      CompositeRateLimitStatus(Set(status))
    }
  }

}
