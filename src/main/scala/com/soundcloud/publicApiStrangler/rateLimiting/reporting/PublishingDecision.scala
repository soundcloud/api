package com.soundcloud.publicApiStrangler.rateLimiting.reporting

import com.soundcloud.publicApiStrangler.rateLimiting.RateLimitEvent

object PublishingDecision {

  type PublishingDecision = Option[RateLimitEvent.Reached]

  val DoNotPublish = None
  val DoPublish = Some

  private val percentagesToPublish = Set(100, 90, 75)
  private val requestCountsToPublish = Set(15000L, 65000L)

  def shouldPublish(event: RateLimitEvent): PublishingDecision = event match {
    case reached: RateLimitEvent.LimitReached =>
      DoPublish(reached)
    case reached: RateLimitEvent.PercentageReached =>
      if (percentagesToPublish(reached.status.percentageUsed) || requestCountsToPublish(reached.status.requestCount)) {
        DoPublish(reached)
      } else DoNotPublish
    case _ => DoNotPublish
  }

}
