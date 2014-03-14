package com.soudcloud.rateLimiting

import com.twitter.finagle.memcached.{Client => MemcachedClient}
import com.twitter.util.{Time, Throw, Return, Future}
import com.soundcloud.jvmkit.{SoundCloudLoggerFactory, ResourceName}
import com.codahale.metrics.MetricRegistry
import org.jboss.netty.buffer.ChannelBuffers

class RateLimit(protectedResource: ResourceName, memcached: MemcachedClient, maximumPerWindow: Long, clock: () => TimeWindow, metrics: MetricRegistry) {
  val logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  val zero = ChannelBuffers.copiedBuffer("0".getBytes("UTF-8"))
  val whatToReturnWhenAnErrorHappens = 0L
  val unusedFlag = 0

  val metricPrefix = s"ratelimit.${protectedResource.getName}"
  val errorMeter = metrics.meter(s"$metricPrefix.errors")
  val limitedMeter = metrics.meter(s"$metricPrefix.limited")
  val allowedMeter = metrics.meter(s"$metricPrefix.allowed")
  val newEntryMeter = metrics.meter(s"$metricPrefix.new_entry")
  val oldEntryMeter = metrics.meter(s"$metricPrefix.old_entry")

  def checkIfAllowed(consumer: Consumer): Future[Boolean] = {
    val timeWindow = clock()
    val usage = UsageEntry(protectedResource, consumer, timeWindow)

    val memcachedKey = usage.key

    incrementedKey(memcachedKey, timeWindow).map(_ < maximumPerWindow).respond {
      case Return(true) => allowedMeter.mark()
      case Return(false) => limitedMeter.mark()
      case Throw(e) => logger.error("Error on rate limiting", e); errorMeter.mark()
    }
  }

  private def incrementedKey(memcachedKey: String, timeWindow: TimeWindow) = {
    def makeSureCounterExists() = {
      val whenToExpire = Time.now + timeWindow.length
      memcached.add(memcachedKey, unusedFlag, whenToExpire, zero).onSuccess {
        wasNewEntry =>
          if (wasNewEntry)
            newEntryMeter.mark()
          else
            oldEntryMeter.mark()
      }
    }

    def incrementExistingCounter(ignored: Any): Future[Long] = {
      memcached.incr(memcachedKey).map {
        case Some(value) => logger.debug(s"UsageEntry [$memcachedKey] has [$value] hits"); value
        case None => logger.error(s"Could not increment key [$memcachedKey]"); errorMeter.mark(); whatToReturnWhenAnErrorHappens
      }
    }

    makeSureCounterExists().flatMap(incrementExistingCounter(_))
  }
}
