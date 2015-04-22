package com.soundcloud.publicApiStrangler.rateLimiting

import java.util.{TimerTask, Timer}

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.config.BazookaConfig
import com.soundcloud.scalakit.{Urn, ResourceName}
import com.soundcloud.scalakit.cache.MemcachedClient
import com.twitter.conversions.time.longToTimeableNumber
import com.twitter.util.{Duration, Await}
import org.jboss.netty.buffer.ChannelBuffers
import org.specs2.matcher.MatchResult
import org.specs2.time.NoTimeConversions
import scala.{concurrent => Stdlib}

class CacheBasedRateLimiterSpec extends UnitSpecification with NoTimeConversions {
  sequential

  final def toChannelBuffer(value: String) = {
    ChannelBuffers.copiedBuffer(value.getBytes("UTF-8"))
  }

  val config = new BazookaConfig()

  val cache = MemcachedClient(config, ResourceName("MEMCACHED_TEST"))
  val duration = 1L.second
  val rateLimit = RateLimit.General(duration, 4)

  val noOpListener = mock[RateLimitEventListener]
  
  val rateLimiter = new CacheBasedRateLimiter(cache, rateLimit, ResourceName("TEST_APP"), noOpListener)

  def dispatchAfterDuration[A](duration: Duration, promise: Stdlib.Promise[A])(f: => A): Unit = {
    new Timer().schedule(new TimerTask {
      override def run(): Unit = {
        promise.success(f)
      }
    }, duration.inMillis)
  }

  "The CacheBasedRateLimiter" should {

    "Rate limiting flow" in {
      val apiClient = ApiClient(Urn("soundcloud", "applications", "1234"))

      val finalTestToken = Stdlib.Promise[MatchResult[RateLimitStatus]]()
      def finalTest: MatchResult[RateLimitStatus] = {
        val status = Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        status must beLike {
          case RateLimitStatus.Advancing(_, Some(_)) => ok
        }
      }

      "register an API client on its first request" in {
        val status = Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        dispatchAfterDuration(duration + 2L.seconds, finalTestToken)(finalTest) // Unfortunately we have to chime this in here
        status must beLike {
          case RateLimitStatus.Advancing(3, Some(_)) => ok
        }
      }

      "keep advancing while the limit is not reached" in {
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        val status = Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        status must beLike {
          case RateLimitStatus.Advancing(1, Some(_)) => ok
        }
      }

      "stop advancing once the limit is reached" in {
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        val status = Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        status must beLike {
          case RateLimitStatus.Reached(_, Some(_)) => ok
        }
      }

      "advance again after the first time interval has passed" in {
        Stdlib.Await.result(finalTestToken.future, Stdlib.duration.Duration.Inf)
      }
    }

    "Rate limiting query flow" in {
      val apiClient = ApiClient(Urn("soundcloud", "applications", "1235"))

      "Query for as yet unknown client" in {
        val status = Await.result(rateLimiter.rateLimitStatus(apiClient))
        status ==== RateLimitStatus.Advancing(4, None)
      }

      "Query for a client that has not reached its limit" in {
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        val status = Await.result(rateLimiter.rateLimitStatus(apiClient))
        status must beLike {
          case RateLimitStatus.Advancing(2, Some(_)) => ok
        }
      }

      "Query for a client that has reached its limit" in {
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        Await.result(rateLimiter.advanceRateLimitStatus(apiClient))
        val status = Await.result(rateLimiter.rateLimitStatus(apiClient))
        status must beLike {
          case RateLimitStatus.Reached(4, Some(_)) => ok
        }
      }

    }
  }
}
