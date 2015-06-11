package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.ratelimiting.core.RateLimitMode

case class CompositeRateLimitStatus(statuses: Set[RateLimitStatus]) {
  lazy val hasReachedLimit: Boolean = statuses.exists(_.hasReachedLimit)

  def keepingReachedEnforcedRateLimitStatuses: Option[CompositeRateLimitStatus] = {
    val relevantStatuses = statuses.filter(status => status.hasReachedLimit && status.rateLimit.mode == RateLimitMode.Enforcing)
    if (relevantStatuses.isEmpty) None
    else Some(CompositeRateLimitStatus(relevantStatuses))
  }
}
