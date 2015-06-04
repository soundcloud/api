package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.ratelimiting.types.RateLimit

case class RateLimitGroup(name: String, rateLimits: Seq[RateLimit])
