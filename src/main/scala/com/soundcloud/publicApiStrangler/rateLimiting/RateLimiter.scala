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
  def from(cache: Cache, config: Config, listeners: Seq[EventListener[RateLimitEvent]]): RateLimiter = {
    val rateLimits = RateLimit.EndpointGroupSpecific.parse(config.get("ENDPOINT_GROUP_SPECIFIC_RATE_LIMITS"))
    val rateLimiters = rateLimits.map(new CacheBasedRateLimiter(cache, _, config.getApplicationResourceName, listeners))
    rateLimiters.head // We have just one atm
  }
}