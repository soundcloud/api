package com.soudcloud.rateLimiting

import com.twitter.finagle.redis.{Client => RedisClient}
import org.jboss.netty.buffer.{ChannelBuffer, ChannelBuffers}
import com.twitter.util.{Throw, Return, Future}
import com.soundcloud.jvmkit.ResourceName
import com.codahale.metrics.MetricRegistry

class RateLimit(protectedResource: ResourceName, redis: RedisClient, maximumPerWindow: Long, clock: () => TimeWindow, metrics: MetricRegistry) {
  val metricPrefix = s"ratelimit.${protectedResource.getName}"

  def checkIfAllowed(consumer: Consumer): Future[Boolean] = {
    val timeWindow = clock()
    val usage = UsageEntry(protectedResource, consumer, timeWindow)

    val redisKey = ChannelBuffers.copiedBuffer(usage.key.getBytes("UTF-8"))

    redis.incr(redisKey).map {
      case c if c < maximumPerWindow => makeSureItExpires(redisKey, timeWindow); true
      case other => makeSureItExpires(redisKey, timeWindow); false
    }.respond {
      case Return(r) if r == true => metrics.meter(s"$metricPrefix.allowed");
      case Return(r) if r == false => metrics.meter(s"$metricPrefix.limited");
      case Throw(e) => metrics.meter(s"$metricPrefix.errors").mark();
    }
  }

  private def makeSureItExpires(redisKey: ChannelBuffer, timeWindow: TimeWindow) {
    redis.expire(redisKey, timeWindow.length.inSeconds)
  }
}
