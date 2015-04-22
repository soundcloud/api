package com.soundcloud.publicApiStrangler.rateLimiting

import java.util.concurrent.Executors

import com.soundcloud.jvmkit.config.Config
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.twitter.util.FuturePool
import io.prometheus.client.metrics.Counter
import play.api.libs.json.Json

class RateLimitEventListener(config: Config) extends EventListener[RateLimitEvent] {
  private val logger = SoundCloudLoggerFactory.getLogger(this.getClass)
  private val withOwnThreadPool = FuturePool(Executors.newFixedThreadPool(10))

  val telemetry = new Telemetry(config)

  val ratelimitReachedCounter =
    telemetry.counter(
      "public_api_ratelimit_reached_counter",
      "counter for public API clients having reached their rate limit",
      "client_urn")

  val ratelimitOverflowingCounter =
    telemetry.counter(
      "public_api_ratelimit_overflowing_counter",
      "counter for public API clients having reached their rate limit",
      "client_urn")

  def notify(event: RateLimitEvent): Unit = withOwnThreadPool {
    event match {
      case RateLimitEvent.Reached(status, client) =>
        logger.info(s"client-urn=${client.urn} rate-limit-status='${Json.stringify(Json.toJson(status))}'")
        inc(ratelimitReachedCounter, client)
      case RateLimitEvent.Overflowing(client) =>
        inc(ratelimitOverflowingCounter, client)
    }
  }

  private def inc(counter: Counter, client: ApiClient): Unit = {
    counter
      .newPartial()
      .labelPair("client_urn", client.urn.getString)
      .apply()
      .increment()
  }
}
