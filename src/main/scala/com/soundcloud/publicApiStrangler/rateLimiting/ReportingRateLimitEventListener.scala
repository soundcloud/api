package com.soundcloud.publicApiStrangler.rateLimiting

import java.util.concurrent.Executors

import com.soundcloud.jvmkit.config.Config
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.twitter.util.FuturePool
import play.api.libs.json.Json

class ReportingRateLimitEventListener(config: Config) extends EventListener[RateLimitEvent] {

  private val logger = SoundCloudLoggerFactory.getLogger(this.getClass)
  private val bulkheaded = FuturePool(Executors.newFixedThreadPool(4))

  def notify(event: RateLimitEvent): Unit = bulkheaded {
    event match {
      case reached: RateLimitEvent.Reached =>
        onReached(reached)
      case _ =>
    }
  }

  private def onReached(reached: RateLimitEvent.Reached): Unit = {
    if (shouldPublishEvent(reached.status)) {
      logger.info(s"client-urn=${reached.apiClient.urn} rate-limit-status='${Json.stringify(Json.toJson(reached.status))}'")
      //
    }
  }

  private def shouldPublishEvent(status: RateLimitStatus): Boolean = {
    Set(status.maxNrOfRequests, status.maxNrOfRequests / 90, status.maxNrOfRequests / 75)(status.requestCount)
  }

}