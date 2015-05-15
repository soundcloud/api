package com.soundcloud.publicApiStrangler.rateLimiting.reporting

import com.soundcloud.publicApiStrangler.rateLimiting.RateLimitEvent

object PublishingDecision extends (RateLimitEvent => Either[Unit, RateLimitEvent.Reached]) {

  val DoNotPublish = Left(())
  val Publish = Right

  def apply(event: RateLimitEvent) = event match {
    case reached: RateLimitEvent.Reached =>
      def threshold(percentage: Int): Int = (reached.status.maxNrOfRequests * percentage / 100).toInt
      if (Set(reached.status.maxNrOfRequests, threshold(90), threshold(75))(reached.status.requestCount)) {
        Publish(reached)
      } else DoNotPublish
    case _ => DoNotPublish
  }

}
