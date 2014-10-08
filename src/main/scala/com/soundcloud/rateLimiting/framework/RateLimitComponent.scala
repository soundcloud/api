package com.soundcloud.rateLimiting.framework

import com.soundcloud.rateLimiting.web.RateLimitingFilter
import com.soundcloud.scalakit.framework.ScAppComponent
import com.soundcloud.rateLimiting.{RateLimitCounter, DefaultTimeWindow, RateLimit}
import com.soundcloud.jvmkit.{ResourceName, Clock}
import org.joda.time.DateTime
import com.soundcloud.jvmkit.circuitbreakers.CircuitBreaker
import java.util.Timer
import com.soundcloud.scalakit.finagle.CircuitBreakerFilter
import com.twitter.finagle.memcached.{Client => MemcachedClient, CacheNodeGroup, KetamaClientBuilder}
import com.twitter.finagle.memcached.protocol.{Response, Command}

trait RateLimitComponent {
  self: ScAppComponent =>

  val circuitBreakerFilter = {
    val name = new ResourceName("RATE-LIMIT")
    val circuitBreaker = new CircuitBreaker(name, config, Clock.FROM_SYSTEM, telemetry)
    new CircuitBreakerFilter[Command, Response](new ResourceName(name.getName), circuitBreaker)
  }

  val memcached = {
    val memcachedServers = config.get("RATE_LIMIT_MEMCACHED_SERVERS", "")
    require(memcachedServers != null, "$RATE_LIMIT_MEMCACHED_SERVERS must be set to a list of servers")

    KetamaClientBuilder()
      .dest(memcachedServers)
      .build()
  }

  val enforceDefaultRateLimiting: RateLimitingFilter = {
    val maxPerWindow = config.get("RATELIMIT_MAX_PER_IP", "180").toLong
    enforceMaxHitsPerHour(maxPerWindow)
  }

  def enforceMaxHitsPerHour(maxPerHour: Long): RateLimitingFilter = {
    val appName = new ResourceName(config.getApplicationName)
    val time = () => DefaultTimeWindow(new DateTime())
    val counter = new RateLimitCounter(memcached)
    val rateLimit = new RateLimit(appName, counter, maxPerHour, time)
    new RateLimitingFilter(rateLimit)
  }
}
