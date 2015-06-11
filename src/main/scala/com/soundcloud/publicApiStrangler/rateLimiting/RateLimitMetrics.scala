package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.config.Config
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.ratelimiting.core.ClientApplication
import com.twitter.util.Future
import io.prometheus.client.metrics.Counter

class RateLimitMetrics(prometheusLabelsSafeGuard: PrometheusLabelsSafeGuard, config: Config) {
  private val telemetry = new Telemetry(config)

  private val ratelimitReachedCounter =
    telemetry.counter(
      "public_api_ratelimit_reached_counter",
      "counter for public API clients having reached their rate limit",
      "client_urn")

  private val ratelimitOverflowingCounter =
    telemetry.counter(
      "public_api_ratelimit_overflowing_counter",
      "counter for public API clients having reached their rate limit",
      "client_urn")

  val incrementRateLimitReachedCounter = inc(ratelimitReachedCounter) _
  val incrementRateLimitOverflowingCounter = inc(ratelimitOverflowingCounter) _

  private def inc(counter: Counter)(client: ClientApplication): Future[Unit] = {
    prometheusLabelsSafeGuard.getSafeLabel(client).map { label =>
      counter
        .newPartial()
        .labelPair("client", label)
        .apply()
        .increment()
    }
  }
}
