package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.config.BazookaConfig
import com.soundcloud.ratelimiting.core._
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
  val rateLimitConfigurations @ Seq(byClientConfig, byUserConfig) = Seq(
    RateLimitConfiguration(Bucket.ByClient, duration, 4),
    RateLimitConfiguration(Bucket.ByUser, duration, 5)
  )
  val rateLimit = RateLimit(EndpointGroup("default", ".*".r), rateLimitConfigurations, RateLimitMode.Probing)

  val rateLimiter = new IndividualRateLimiter(cache, rateLimit, ResourceName("TEST_APP"), Seq.empty)

  trait App {
    def clientApplication: ClientApplication
  }
  
  trait App1234 extends App {
    lazy val clientApplication = ClientApplication(Urn("soundcloud", "applications", "1234"))
  }
  
  trait App1235 extends App {
    lazy val clientApplication = ClientApplication(Urn("soundcloud", "applications", "1235"))
  }
  
  trait AnonymousAccess { this: App =>
    val rateLimitIdentity = RateLimitIdentity.from(byClientConfig, rateLimit.group, rateLimit.mode)
    val accessMechanism = ActionableAccessMechanism.Anonymous(clientApplication)
  }

  trait LoggedInAccess { this: App =>
    val rateLimitIdentity = RateLimitIdentity.from(byUserConfig, rateLimit.group, rateLimit.mode)
    val user = Urn("soundcloud", "users", "derp")
    val accessMechanism = ActionableAccessMechanism.LoggedIn(clientApplication, user)
  }

  "The individual rate limiters" should {

    "Rate limiting flow" in {

      "for anonymous access" in {
        object Assembly extends AnonymousAccess with App1234; import Assembly._

        "register an API client on its first request" in {
          val status = Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
          status must beLike {
            case RateLimitStatus(_, 0, Some(_)) => ok
          }
        }

        "keep advancing while the limit is not reached" in {
          Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
          Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
          val status = Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
          status must beLike {
            case RateLimitStatus(_, 3, Some(_)) => ok
          }
        }

        "stop advancing once the limit is reached" in {
          Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
          val status = Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
          status must beLike {
            case RateLimitStatus(_, 4, Some(_)) => ok
          }
        }

        "advance again after the first time interval has passed" in {
          afterDuration(7.seconds) {
            val status = Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
            status must beLike {
              case RateLimitStatus(_, 0, Some(_)) => ok
            }
          }
        }
      }

      "for logged-in access, with a by-user bucket available" in {
        object Assembly extends LoggedInAccess with App1234; import Assembly._

        "register an API client on its first request" in {
          val status = Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
          status must beLike {
            case RateLimitStatus(_, 0, Some(_)) => ok
          }
        }

        "keep advancing while the limit is not reached" in {
          Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
          Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
          val status = Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
          status must beLike {
            case RateLimitStatus(_, 3, Some(_)) => ok
          }
        }

        "stop advancing once the limit is reached" in {
          Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
          Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
          val status = Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
          status must beLike {
            case RateLimitStatus(_, 5, Some(_)) => ok
          }
        }

        "advance again after the first time interval has passed" in {
          afterDuration(7.seconds) {
            val status = Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
            status must beLike {
              case RateLimitStatus(_, 0, Some(_)) => ok
            }
          }
        }
      }
    }

    "Rate limiting query flow" in {
      "for anonymous access" in {
        object Assembly extends AnonymousAccess with App1235; import Assembly._

        "Query for as yet unknown client" in {
          val status = Await.result(rateLimiter.rateLimitStatus(accessMechanism))
          status ==== RateLimitStatus(rateLimitIdentity, 0, None)
        }

        "Query for a client that has not reached its limit" in {
          Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
          Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
          val status = Await.result(rateLimiter.rateLimitStatus(accessMechanism))
          status must beLike {
            case RateLimitStatus(_, 2, Some(_)) => ok
          }
        }

        "Query for a client that has reached its limit" in {
          Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
          Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
          Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
          Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
          val status = Await.result(rateLimiter.rateLimitStatus(accessMechanism))
          status must beLike {
            case RateLimitStatus(`rateLimitIdentity`, 4, Some(_)) => ok
          }
        }
      }
    }

    "for logged-in access, with a by-user bucket available" in {
      object Assembly extends LoggedInAccess with App1235; import Assembly._

      "Query for as yet unknown client" in {
        val status = Await.result(rateLimiter.rateLimitStatus(accessMechanism))
        status ==== RateLimitStatus(rateLimitIdentity, 0, None)
      }

      "Query for a client that has not reached its limit" in {
        Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
        Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
        val status = Await.result(rateLimiter.rateLimitStatus(accessMechanism))
        status must beLike {
          case RateLimitStatus(_, 2, Some(_)) => ok
        }
      }

      "Query for a client that has reached its limit" in {
        Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
        Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
        Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
        Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
        Await.result(rateLimiter.advanceRateLimitStatus(accessMechanism))
        val status = Await.result(rateLimiter.rateLimitStatus(accessMechanism))
        status must beLike {
          case RateLimitStatus(`rateLimitIdentity`, 5, Some(_)) => ok
        }
      }
    }
  }
}
