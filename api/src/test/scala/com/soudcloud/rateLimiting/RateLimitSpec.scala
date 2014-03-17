package com.soudcloud.rateLimiting

import com.soundcloud.scalakit.test.UnitSpecification
import org.joda.time.DateTime
import com.twitter.util.{Future, Await}
import com.soundcloud.jvmkit.ResourceName
import com.codahale.metrics.MetricRegistry
import scala.util.Random

class RateLimitSpec extends UnitSpecification {

  trait Context extends Scope {

    val testRunNumber = s"${System.nanoTime}-${Random.nextLong()}"
    val resource = new ResourceName(s"some-test-$testRunNumber")
    val firstTimeWindow = DefaultTimeWindow(new DateTime(2001, 1, 1, 1, 1, 1))
    val secondTimeWindow = DefaultTimeWindow(new DateTime(2001, 1, 1, 2, 2, 2))
    val consumer1 = Ip("127.0.0.1"): Consumer
    val consumer2 = Ip("10.23.131.255"): Consumer
    val limit = 3
    val clock = mock[() => TimeWindow]
    val counter = mock[RateLimitCounter]
    val rateLimit = new RateLimit(resource, counter, limit, clock, new MetricRegistry)

    def clockReturns(t: TimeWindow, o: TimeWindow*) = {
      clock.apply() returns(t, o: _*)
    }
  }

  "when consumer attempts to use resource" >> {
    "returns false once the limit is reached" in new Context {
      clockReturns(firstTimeWindow)
      val entry = UsageEntry(resource, consumer1, firstTimeWindow)
      counter.incr(===(entry)) returns Future.value(limit)

      Await.result(rateLimit.checkIfAllowed(consumer1)) must beFalse
    }

    "returns true if under limit" in new Context {
      clockReturns(firstTimeWindow)
      val entry = UsageEntry(resource, consumer1, firstTimeWindow)

      val fromZeroToLimit = (0L to limit.toLong - 1).toList.map(Future.value(_))
      counter.incr(===(entry)) returns(fromZeroToLimit.head, fromZeroToLimit.tail: _*)

      fromZeroToLimit.foreach {
        _ =>
          Await.result(rateLimit.checkIfAllowed(consumer1)) must beTrue
      }
    }

    "returns true if same consumer but different time window" in new Context {
      clockReturns(firstTimeWindow, secondTimeWindow)
      val entry1 = UsageEntry(resource, consumer1, firstTimeWindow)
      val entry2 = UsageEntry(resource, consumer1, secondTimeWindow)
      counter.incr(===(entry1)) returns Future.value(limit)
      counter.incr(===(entry2)) returns Future.value(0)

      Await.result(rateLimit.checkIfAllowed(consumer1)) must beFalse
      Await.result(rateLimit.checkIfAllowed(consumer1)) must beTrue
    }

    "wont let consumers interfere with one another" in new Context {
      clockReturns(firstTimeWindow)
      val entry1 = UsageEntry(resource, consumer1, firstTimeWindow)
      val entry2 = UsageEntry(resource, consumer2, firstTimeWindow)
      counter.incr(===(entry1)) returns Future.value(limit + 1)
      counter.incr(===(entry2)) returns Future.value(0)

      Await.result(rateLimit.checkIfAllowed(consumer1)) must beFalse
      Await.result(rateLimit.checkIfAllowed(consumer2)) must beTrue
    }
  }
}
