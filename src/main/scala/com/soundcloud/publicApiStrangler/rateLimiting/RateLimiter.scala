package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.config.Config
import com.soundcloud.ratelimiting.types.RateLimit
import com.soundcloud.scalakit.cache.Cache
import com.twitter.finagle.http.Request
import com.twitter.util.Future

trait RateLimiter {
  def appliesTo(request: Request): Boolean
  def advanceRateLimitStatus(apiClient: ApiClient): Future[RateLimitStatus]
  def rateLimitStatus(apiClient: ApiClient): Future[RateLimitStatus]
}

object RateLimiter {
  def from(cache: Cache, config: Config, listeners: Seq[EventListener[RateLimitEvent]]): Seq[RateLimiter] = {
    val rateLimits = RateLimit.parse(config.get("RATE_LIMITS"))
    rateLimits.map(new CacheBasedRateLimiter(cache, _, config.getApplicationResourceName, listeners))
  }
}