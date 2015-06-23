package com.soundcloud.publicApiStrangler.rateLimiting.reporting

import com.soundcloud.ratelimiting.events.{Event, ReachedEventPayload}

object PublishingDecision {
  private val percentagesToPublish = Set(100.0, 90.0, 75.0)
  private val requestCountsToPublish = Set(15000L, 65000L)

  def shouldPublish(event: Event[ReachedEventPayload]): Boolean = {
    def threshold(percentage: Double): Long = (event.payload.rateLimit.maxNrOfRequests * percentage / 100).toLong
    percentagesToPublish.map(threshold).contains(event.payload.requestCount) || requestCountsToPublish(event.payload.requestCount)
  }
}
