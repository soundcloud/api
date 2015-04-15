package com.soundcloud.publicApiStrangler.rateLimitingOld.framework

import com.soundcloud.jvmkit.circuitbreakers.CircuitBreaker
import com.soundcloud.jvmkit.config.ConfigConvention
import com.soundcloud.jvmkit.Clock
import com.soundcloud.publicApiStrangler.rateLimitingOld.web.RateLimitingFilter
import com.soundcloud.publicApiStrangler.rateLimitingOld.{DefaultTimeWindow, RateLimit, RateLimitCounter}
import com.soundcloud.scalakit.ResourceName
import com.soundcloud.scalakit.finagle.CircuitBreakerFilter
import com.soundcloud.scalakit.framework.ScAppComponent
import com.twitter.finagle.memcached.protocol.{Command, Response}
import com.twitter.finagle.memcached.{CacheNodeGroup, KetamaClientBuilder}
import org.joda.time.DateTime

trait RateLimitComponent {
  self: ScAppComponent =>

  val circuitBreakerFilter = {
    val name = new ResourceName("RATE-LIMIT")
    val circuitBreaker = new CircuitBreaker(name, config, Clock.FROM_SYSTEM, telemetry)
    new CircuitBreakerFilter[Command, Response](new ResourceName(name.getName), circuitBreaker)
  }

  val memcached = {
    val memcachedServers = config.get("RATE_LIMIT_MEMCACHED_SERVERS", true)
    require(memcachedServers != null, "$RATE_LIMIT_MEMCACHED_SERVERS must be set to a list of servers")

    KetamaClientBuilder()
      .group(CacheNodeGroup.apply(memcachedServers))
      .build()
  }

  val enforceDefaultRateLimiting: RateLimitingFilter = {
    val maxPerWindow = config.get(ResourceName("RATELIMIT"), ConfigConvention.MAX_CONNS, "180").toLong
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
