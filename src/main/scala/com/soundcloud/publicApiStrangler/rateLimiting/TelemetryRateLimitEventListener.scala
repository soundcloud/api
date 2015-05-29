package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.twitter.util.FuturePool
import play.api.libs.json.Json

class TelemetryRateLimitEventListener(rateLimitMetrics: RateLimitMetrics) extends EventListener[RateLimitEvent] {

  private val logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  def notify(event: RateLimitEvent): Unit = FuturePool.unboundedPool {
    event match {
      case RateLimitEvent.LimitReached(status, client)  =>
        rateLimitMetrics.incrementRateLimitReachedCounter(client)
        logger.info(s"client-urn=${client.urn} rate-limit-status='${Json.stringify(Json.toJson(status))}'")
      case RateLimitEvent.Overflowing(client) =>
        rateLimitMetrics.incrementRateLimitOverflowingCounter(client)
      case _ =>
    }
  }

  def reportingThresholds(status: RateLimitStatus): Set[Long] = {
    Set(status.maximumNrOfRequests / 90, status.maximumNrOfRequests / 75)
  }
}