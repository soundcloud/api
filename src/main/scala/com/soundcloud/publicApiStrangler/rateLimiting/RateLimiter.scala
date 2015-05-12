package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.config.Config
import com.soundcloud.scalakit.cache.Cache
import com.twitter.util.Future

trait RateLimiter {
  def rateLimit: RateLimit
  def advanceRateLimitStatus(apiClient: ApiClient): Future[RateLimitStatus]
  def rateLimitStatus(apiClient: ApiClient): Future[RateLimitStatus]
}

object RateLimiter {
  def from(cache: Cache, config: Config, listeners: Seq[EventListener[RateLimitEvent]]): RateLimiter = {
    val rateLimits = RateLimit.General.parse(config.get("GENERAL_RATE_LIMITS"))
    val rateLimiters = rateLimits.map(new CacheBasedRateLimiter(cache, _, config.getApplicationResourceName, listeners))
    rateLimiters.head // We have just one atm
  }
}