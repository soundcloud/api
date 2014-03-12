package com.soudcloud.rateLimiting.framework

import com.soudcloud.rateLimiting.web.RateLimitingFilter
import com.soundcloud.scalakit.framework.{StatsComponent, ScAppComponent}
import com.soudcloud.rateLimiting.{DefaultTimeWindow, RateLimit}
import com.soundcloud.jvmkit.ResourceName
import org.joda.time.DateTime
import com.soundcloud.jvmkit.circuitbreakers.CircuitBreaker
import java.util.Timer
import com.soundcloud.scalakit.finagle.CircuitBreakerFilter
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.memcached.{Client => MemcachedClient}
import com.twitter.finagle.memcached.protocol.{Response, Command}
import com.twitter.finagle.memcached.protocol.text.Memcached
import com.soundcloud.scalakit.stats.MetricsStatsReceiver
import scala.collection.JavaConversions._
import com.twitter.finagle.Group
import java.net.{SocketAddress, InetSocketAddress}

trait RateLimitComponent {
  self: ScAppComponent with StatsComponent =>

  val circuitBreakerFilter = {
    val name = new ResourceName("RATE-LIMIT")
    val circuitBreaker = new CircuitBreaker(name, config, new Timer, metrics)
    new CircuitBreakerFilter[Command, Response](name.getName, circuitBreaker)
  }

  val memcached = {
    val statsReceiver = new MetricsStatsReceiver(metrics)
    val memcachedServers = config.getList("RATE_LIMIT_MEMCACHED_SERVERS", "")
    require(memcachedServers.size() == 0, "$RATE_LIMIT_MEMCACHED_SERVERS must be set to a list of servers")

    val socketAddresses = memcachedServers.map {
      server =>
        val pieces = server.split(",")
        val host = pieces(0)
        val port = pieces(1).toInt
        logger.info(s"Configuring memcached server [$host:$port]")
        new InetSocketAddress(host, port):SocketAddress
    }

    val memcachedGroup = Group(socketAddresses: _*)

    val memcachedService = ClientBuilder()
      .group(memcachedGroup)
      .hostConnectionLimit(1)
      .codec(new Memcached(statsReceiver))
      .daemon(true)
      .build()

    MemcachedClient(circuitBreakerFilter andThen memcachedService)
  }

  val rateLimitingFilter: RateLimitingFilter = {
    val maxPerWindow = config.get("RATELIMIT_MAX_PER_IP", "180").toLong
    val appName = new ResourceName(config.getApplicationName)
    val time = () => DefaultTimeWindow(new DateTime())
    val rateLimit = new RateLimit(appName, memcached, maxPerWindow, time, metrics)
    new RateLimitingFilter(rateLimit)
  }
}
