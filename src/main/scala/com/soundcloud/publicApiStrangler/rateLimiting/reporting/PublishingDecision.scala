package com.soundcloud.publicApiStrangler.rateLimiting.reporting

import com.soundcloud.publicApiStrangler.rateLimiting.RateLimitEvent

object PublishingDecision {

  type PublishingDecision = Option[RateLimitEvent.Reached]

  val DoNotPublish = None
  val DoPublish = Some

  def shouldPublish(event: RateLimitEvent): PublishingDecision = event match {
    case reached: RateLimitEvent.Reached =>
      def threshold(percentage: Int): Int = (reached.status.maxNrOfRequests * percentage / 100).toInt
      if (Set(reached.status.maxNrOfRequests, threshold(90), threshold(75))(reached.status.requestCount)) {
        DoPublish(reached)
      } else DoNotPublish
    case _ => DoNotPublish
  }

}
