package com.soundcloud.rateLimiting

import com.twitter.finagle.memcached.{Client => MemcachedClient, MockClient}
import scala.util.Random
import com.soundcloud.jvmkit.ResourceName
import com.twitter.util.{Duration, Await}
import com.soundcloud.scalakit.test.UnitSpecification

class RateLimitCounterSpec extends UnitSpecification {
  sequential

  trait ExternalMemcachedProcess extends Scope {
    val memcached = new MockClient
    val rateLimitCounter = new RateLimitCounter(memcached)
  }

  "incrementing keys" >> {
   "increments first time " in new ExternalMemcachedProcess {
     val keyThatDoestExist = UsageEntry(new ResourceName(s"something-random-${Random.nextLong()}"), Ip("1.3.4.5"), TimeWindow(Duration.fromSeconds(21), Random.nextInt))
     Await.result(rateLimitCounter.incr(keyThatDoestExist)) must be_==(1)
   }

    "increments by one from there on"in new ExternalMemcachedProcess {
      val keyToBeIncremented = UsageEntry(new ResourceName(s"something-random-${Random.nextLong()}"), Ip("10.10.10.10"), TimeWindow(Duration.fromSeconds(21), Random.nextInt))
      Await.result(rateLimitCounter.incr(keyToBeIncremented)) must be_==(1)
      Await.result(rateLimitCounter.incr(keyToBeIncremented)) must be_==(2)
      Await.result(rateLimitCounter.incr(keyToBeIncremented)) must be_==(3)
      Await.result(rateLimitCounter.incr(keyToBeIncremented)) must be_==(4)
      Await.result(rateLimitCounter.incr(keyToBeIncremented)) must be_==(5)
    } 
  }

  "cant reach memcached" >> {
    "returns zero" in {
      val brokenMemcachedClient = MemcachedClient(s"localhost:101")
      val rateLimitCounter = new RateLimitCounter(brokenMemcachedClient)
      val someKey = UsageEntry(new ResourceName(s"something-random-${Random.nextLong()}"), Ip("10.10.10.10"), TimeWindow(Duration.fromSeconds(21), Random.nextInt))
      Await.result(rateLimitCounter.incr(someKey)) must be_==(0)
    }
  }
}
