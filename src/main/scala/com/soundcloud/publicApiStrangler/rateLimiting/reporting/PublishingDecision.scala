package com.soundcloud.publicApiStrangler.rateLimiting.reporting

import com.soundcloud.publicApiStrangler.rateLimiting.RateLimitEvent

object PublishingDecision {

  type PublishingDecision = Option[RateLimitEvent.CheckpointReached]

  val DoNotPublish = None
  val DoPublish = Some

  private val percentagesToPublish = Set(100.0, 90.0, 75.0)
  private val requestCountsToPublish = Set(15000L, 65000L)

  def shouldPublish(event: RateLimitEvent): PublishingDecision = {
    event match {
      case reached: RateLimitEvent.CheckpointReached =>
        def threshold(percentage: Double): Long = (reached.status.maximumNrOfRequests * percentage / 100).toLong
        if (percentagesToPublish.map(threshold).contains(reached.status.requestCount) || requestCountsToPublish(reached.status.requestCount)) {
          DoPublish(reached)
        } else DoNotPublish
      case _ => DoNotPublish
    }
  }
}
