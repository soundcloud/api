package com.soundcloud.publicApiStrangler.rateLimiting

import java.util.concurrent.Executors

import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.twitter.util.FuturePool
import play.api.libs.json.Json

class RateLimitEventListener(rateLimitMetrics: RateLimitMetrics) extends EventListener[RateLimitEvent] {
  private val logger = SoundCloudLoggerFactory.getLogger(this.getClass)
  private val withOwnThreadPool = FuturePool(Executors.newFixedThreadPool(10))

  def notify(event: RateLimitEvent): Unit = withOwnThreadPool {
    event match {
      case RateLimitEvent.Reached(status, client) =>
        logger.info(s"client-urn=${client.urn} rate-limit-status='${Json.stringify(Json.toJson(status))}'")
        rateLimitMetrics.incrementRateLimitReachedCounter(client)
      case RateLimitEvent.Overflowing(client) =>
        rateLimitMetrics.incrementRateLimitOverflowingCounter(client)
    }
  }
}
