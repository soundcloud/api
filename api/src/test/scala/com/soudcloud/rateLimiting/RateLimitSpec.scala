package com.soudcloud.rateLimiting

import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.finagle.redis.{Client => RedisClient}
import org.joda.time.DateTime
import org.mockito.Mockito._
import java.net.InetAddress
import com.twitter.util.Await
import org.jboss.netty.buffer.{ChannelBuffers, ChannelBuffer}
import java.lang.{Long => JvmLong}
import com.soundcloud.jvmkit.ResourceName
import com.codahale.metrics.MetricRegistry

class RateLimitSpec extends UnitSpecification {
  sequential

  implicit def string2channelBuffer(s: String): ChannelBuffer = ChannelBuffers.copiedBuffer(s.getBytes("UTF-8"))

  val resource = new ResourceName("some-test")

  val redis = RedisClient("localhost:6379")

  def allKeys = redis.keys("*")

  override def before = {
    Await.result(allKeys.onSuccess {
      case s@x :: xs => redis.del(s)
      case other =>
    })
  }

  trait Context extends Scope {
    val firstTimeWindow = DefaultTimeWindow(new DateTime(2001, 1, 1, 1, 1, 1))
    val secondTimeWindow = DefaultTimeWindow(new DateTime(2001, 1, 1, 2, 2, 2))
    val consumer1 = InetAddress.getByName("localhost")
    val consumer2 = InetAddress.getByName("10.23.131.255")
    val limit = 3
    val clock = mock[() => TimeWindow]
    val rateLimit = new RateLimit(resource, redis, limit, clock, new MetricRegistry)


    def clockReturns(t: TimeWindow, o: TimeWindow*) = {
      when(clock.apply()).thenReturn(t, o: _*)
    }

    def reachLimitFor(timeWindow: TimeWindow, consumer: Consumer) = {
      clockReturns(timeWindow)
      (0 to limit - 2).foreach(_ => Await.result(rateLimit.checkIfAllowed(consumer)))
      Await.result(rateLimit.checkIfAllowed(consumer))
    }
  }

  "when consumer needs to be rate limited" >> {
    "returns fail once the limit is reached" in new Context {
      reachLimitFor(firstTimeWindow, consumer1) must beFalse
    }

    "returns fail for any subsequent request in the currentTime window" in new Context {
      reachLimitFor(firstTimeWindow, consumer2)
      val subsequentAttempts = (0 to 100).map(_ => Await.result(rateLimit.checkIfAllowed(consumer2)))
      subsequentAttempts.toSet must be_==(Set(false))
    }

    "returns success after the window is over" in new Context {
      reachLimitFor(firstTimeWindow, consumer1)
      clockReturns(secondTimeWindow)
      Await.result(rateLimit.checkIfAllowed(consumer1)) must beTrue
    }

    "wont let consumers interfere with each other" in new Context {
      reachLimitFor(firstTimeWindow, consumer1)
      clockReturns(firstTimeWindow)
      Await.result(rateLimit.checkIfAllowed(consumer2)) must beTrue

    }
  }

  "when limit is never reached" >> {
    "returns success if requests below limit" in new Context {
      clockReturns(firstTimeWindow)
      val attemptsBeforeReachingLimit = (0 to limit - 2).map(_ => Await.result(rateLimit.checkIfAllowed(consumer1)))
      attemptsBeforeReachingLimit.toSet must be_==(Set(true))
    }
  }

  "expiring windows" >> {
    "time window expires after time" in new Context {
      clockReturns(firstTimeWindow)
      (0 to limit - 1).foreach {
        _ => Await.result(rateLimit.checkIfAllowed(consumer1))
      }

      val ttl = Await.result(allKeys.flatMap {
        a => redis.ttl(a.head)
      })

      ttl must beSome[JvmLong]
    }
  }
}
