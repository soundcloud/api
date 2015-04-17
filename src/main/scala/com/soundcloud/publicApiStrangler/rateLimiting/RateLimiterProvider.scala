package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.config.Config
import com.soundcloud.scalakit.cache.Cache

class RateLimiterProvider(config: Config, cache: Cache) {
  private val rateLimits: Set[RateLimit] = RateLimit.General.parse(config.get("RATE_LIMITS.GENERAL"))

  def rateLimiters: Set[RateLimiter] = {
    rateLimits.map(new CacheBasedRateLimiter(cache, _, config.getApplicationResourceName))
  }
}
