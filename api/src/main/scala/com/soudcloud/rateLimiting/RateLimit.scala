package com.soudcloud.rateLimiting

import com.twitter.util.{Throw, Return, Future}
import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.codahale.metrics.MetricRegistry
import org.jboss.netty.buffer.ChannelBuffers

class RateLimit(protectedResource: ResourceName, counter: RateLimitCounter, maximumPerWindow: Long, clock: () => TimeWindow, metrics: MetricRegistry) {
  val logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  val zero = ChannelBuffers.copiedBuffer("0".getBytes("UTF-8"))
  val whatToReturnWhenAnErrorHappens = 0L
  val unusedFlag = 0

  val metricPrefix = s"ratelimit.${protectedResource.getName}"
  val errorMeter = metrics.meter(s"$metricPrefix.errors")
  val limitedMeter = metrics.meter(s"$metricPrefix.limited")
  val allowedMeter = metrics.meter(s"$metricPrefix.allowed")

  def checkIfAllowed(consumer: Consumer): Future[Boolean] = {
    val timeWindow = clock()
    val usage = UsageEntry(protectedResource, consumer, timeWindow)

    counter.incr(usage).map{
      count => logger.debug(s"Consumer [${usage.key}}] has [$count] hits")
        count
    }.map(_ < maximumPerWindow).respond {
      case Return(true) => allowedMeter.mark()
      case Return(false) => limitedMeter.mark()
      case Throw(e) => logger.error("Error on rate limiting", e); errorMeter.mark()
    }
  }
}
