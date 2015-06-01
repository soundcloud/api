package com.soundcloud.publicApiStrangler.rateLimiting

case class CompositeRateLimitStatus(statuses: Set[RateLimitStatus]) {
  lazy val hasReachedLimit: Boolean = statuses.exists(_.hasReachedLimit)
}
