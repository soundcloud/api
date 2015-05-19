package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.config.BazookaConfig
import com.soundcloud.publicApiStrangler.support.TimeConversions._
import com.soundcloud.ratelimiting.types.RateLimit
import com.soundcloud.scalakit.{Urn, ResourceName}
import com.soundcloud.scalakit.cache.MemcachedClient
import com.twitter.conversions.time._
import com.twitter.util.{Duration, Await}
import org.jboss.netty.buffer.ChannelBuffers
import org.joda.time.Period
import org.specs2.matcher.MatchResult
import org.specs2.time.NoTimeConversions

class CacheBasedRateLimiterSpec extends UnitSpecification with NoTimeConversions {
  sequential

  final def toChannelBuffer(value: String) = {
    ChannelBuffers.copiedBuffer(value.getBytes("UTF-8"))
  }

  def afterDuration[A](duration: Duration)(assertion: => MatchResult[A]): MatchResult[A] = {
    Thread.sleep(duration.inMilliseconds)
    assertion
  }

  val config = new BazookaConfig()

  val cache = MemcachedClient(config, ResourceName("MEMCACHED_TEST"))
  val duration = Period.seconds(1)
  val rateLimit = RateLimit.General(duration, 4)
  
  val rateLimiter = new CacheBasedRateLimiter(cache, rateLimit, ResourceName("TEST_APP"), Seq.empty)

  "The CacheBasedRateLimiter" should {

    "Rate limiting flow" in {
      val apiClient = ApiClient(Urn("soundcloud", "applications", "1234"))

      "register an API client on its first request" in {
        val status = Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        status must beLike {
          case RateLimitStatus.Advancing(1, _, _, Some(_)) => ok
        }
      }

      "keep advancing while the limit is not reached" in {
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        val status = Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        status must beLike {
          case RateLimitStatus.Advancing(3, _, _, Some(_)) => ok
        }
      }

      "stop advancing once the limit is reached" in {
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        val status = Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        status must beLike {
          case RateLimitStatus.Reached(_, _, Some(_)) => ok
        }
      }

      "advance again after the first time interval has passed" in {
        afterDuration(4.seconds) {
          val status = Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
          status must beAnInstanceOf[RateLimitStatus.Advancing]
          status must beLike {
            case RateLimitStatus.Advancing(1, _, _, Some(_)) => ok
          }
        }
      }
    }

    "Rate limiting query flow" in {
      val apiClient = ApiClient(Urn("soundcloud", "applications", "1235"))

      "Query for as yet unknown client" in {
        val status = Await.result(rateLimiter.rateLimitStatus(apiClient))
        status ==== RateLimitStatus.Advancing(0, 4, duration.toTwitterDuration, None)
      }

      "Query for a client that has not reached its limit" in {
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        val status = Await.result(rateLimiter.rateLimitStatus(apiClient))
        status must beLike {
          case RateLimitStatus.Advancing(2, _, _, Some(_)) => ok
        }
      }

      "Query for a client that has reached its limit" in {
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        val status = Await.result(rateLimiter.rateLimitStatus(apiClient))
        status must beLike {
          case RateLimitStatus.Reached(4, _, Some(_)) => ok
        }
      }
    }
  }
}
