package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.config.BazookaConfig
import com.soundcloud.ratelimiting.core.RateLimit
import com.soundcloud.ratelimiting.core.RateLimit.EndpointGroupSpecific.EndpointGroup
import com.soundcloud.scalakit.{Urn, ResourceName}
import com.soundcloud.scalakit.cache.MemcachedClient
import com.twitter.conversions.time._
import com.twitter.util.{Duration, Await}
import org.jboss.netty.buffer.ChannelBuffers
import org.joda.time.Period
import org.specs2.matcher.MatchResult
import org.specs2.time.NoTimeConversions

class IndividualRateLimiterSpec extends UnitSpecification with NoTimeConversions {
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
  val duration = Period.seconds(5)
  val rateLimit = RateLimit.EndpointGroupSpecific(EndpointGroup("default", ".*".r), duration, 4)
  
  val rateLimiter = new IndividualRateLimiter(cache, rateLimit, ResourceName("TEST_APP"), Seq.empty)

  "The individual rate limiters" should {

    "Rate limiting flow" in {
      val apiClient = ApiClient(Urn("soundcloud", "applications", "1234"))

      "register an API client on its first request" in {
        val status = Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        status must beLike {
          case RateLimitStatus(_, 1, Some(_)) => ok
        }
      }

      "keep advancing while the limit is not reached" in {
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        val status = Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        status must beLike {
          case RateLimitStatus(_, 3, Some(_)) => ok
        }
      }

      "stop advancing once the limit is reached" in {
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        val status = Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        status must beLike {
          case RateLimitStatus(_, 4, Some(_)) => ok
        }
      }

      "advance again after the first time interval has passed" in {
        afterDuration(7.seconds) {
          val status = Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
          status must beLike {
            case RateLimitStatus(_, 1, Some(_)) => ok
          }
        }
      }
    }

    "Rate limiting query flow" in {
      val apiClient = ApiClient(Urn("soundcloud", "applications", "1235"))

      "Query for as yet unknown client" in {
        val status = Await.result(rateLimiter.rateLimitStatus(apiClient))
        status ==== RateLimitStatus(rateLimit, 0, None)
      }

      "Query for a client that has not reached its limit" in {
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        val status = Await.result(rateLimiter.rateLimitStatus(apiClient))
        status must beLike {
          case RateLimitStatus(_, 2, Some(_)) => ok
        }
      }

      "Query for a client that has reached its limit" in {
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        val status = Await.result(rateLimiter.rateLimitStatus(apiClient))
        status must beLike {
          case RateLimitStatus(`rateLimit`, 4, Some(_)) => ok
        }
      }
    }
  }
}
