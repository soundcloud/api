package com.soudcloud.rateLimiting.framework

import com.soudcloud.rateLimiting.web.RateLimitingFilter
import com.soundcloud.scalakit.framework.{StatsComponent, ScAppComponent}
import com.soudcloud.rateLimiting.{DefaultTimeWindow, RateLimit}
import com.soundcloud.jvmkit.ResourceName
import com.twitter.finagle.redis.{Client, Redis}
import org.joda.time.DateTime
import com.soundcloud.jvmkit.circuitbreakers.CircuitBreaker
import java.util.Timer
import com.soundcloud.scalakit.finagle.CircuitBreakerFilter
import com.twitter.finagle.redis.protocol.{Reply, Command}
import com.twitter.finagle.builder.ClientBuilder

trait RateLimitComponent {
  self: ScAppComponent with StatsComponent =>

  val circuitBreakerFilter = {
    val name = new ResourceName("RATE-LIMIT")
    val circuitBreaker = new CircuitBreaker(name, config, new Timer, metrics)
    new CircuitBreakerFilter[Command, Reply](name.getName, circuitBreaker)
  }

  val redis = {
    val redisHost = config.get("RATE_LIMIT_REDIS_HOST")

    val redisService = ClientBuilder()
      .hosts(redisHost)
      .hostConnectionLimit(1)
      .codec(Redis())
      .daemon(true)
      .build()

    Client(circuitBreakerFilter andThen redisService)
  }

  val rateLimitingFilter: RateLimitingFilter = {
    val maxPerWindow = config.get("RATELIMIT_MAX_PER_IP", "180").toLong
    val appName = new ResourceName(config.getApplicationName)
    val time = () => DefaultTimeWindow(new DateTime())
    val rateLimit = new RateLimit(appName, redis, maxPerWindow, time, metrics)
    new RateLimitingFilter(rateLimit)
  }
}
