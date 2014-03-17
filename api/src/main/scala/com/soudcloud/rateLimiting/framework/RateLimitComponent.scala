package com.soudcloud.rateLimiting.framework

import com.soudcloud.rateLimiting.web.RateLimitingFilter
import com.soundcloud.scalakit.framework.{StatsComponent, ScAppComponent}
import com.soudcloud.rateLimiting.{RateLimitCounter, DefaultTimeWindow, RateLimit}
import com.soundcloud.jvmkit.ResourceName
import org.joda.time.DateTime
import com.soundcloud.jvmkit.circuitbreakers.CircuitBreaker
import java.util.Timer
import com.soundcloud.scalakit.finagle.CircuitBreakerFilter
import com.twitter.finagle.memcached.{Client => MemcachedClient, CacheNodeGroup, KetamaClientBuilder}
import com.twitter.finagle.memcached.protocol.{Response, Command}

trait RateLimitComponent {
  self: ScAppComponent with StatsComponent =>

  val circuitBreakerFilter = {
    val name = new ResourceName("RATE-LIMIT")
    val circuitBreaker = new CircuitBreaker(name, config, new Timer, metrics)
    new CircuitBreakerFilter[Command, Response](name.getName, circuitBreaker)
  }

  val memcached = {
    val memcachedServers = config.get("RATE_LIMIT_MEMCACHED_SERVERS", "")
    require(memcachedServers != null, "$RATE_LIMIT_MEMCACHED_SERVERS must be set to a list of servers")

    KetamaClientBuilder()
      .group(CacheNodeGroup.apply(memcachedServers))
      .build()
  }

  val enforceDefaultRateLimiting: RateLimitingFilter = {
    val maxPerWindow = config.get("RATELIMIT_MAX_PER_IP", "180").toLong
    val appName = new ResourceName(config.getApplicationName)
    val time = () => DefaultTimeWindow(new DateTime())
    val counter = new RateLimitCounter(memcached)
    val rateLimit = new RateLimit(appName, counter, maxPerWindow, time, metrics)
    new RateLimitingFilter(rateLimit)
  }
}
