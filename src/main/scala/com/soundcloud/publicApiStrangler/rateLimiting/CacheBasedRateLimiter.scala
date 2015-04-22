package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.scalakit.ResourceName
import com.soundcloud.scalakit.cache.Cache
import com.twitter.util.Future
import org.joda.time.LocalDateTime

class CacheBasedRateLimiter(
  cache: Cache,
  val rateLimit: RateLimit,
  applicationName: ResourceName,
  listener: EventListener[RateLimitEvent]
) extends RateLimiter {

  def advanceRateLimitStatus(apiClient: ApiClient): Future[RateLimitStatus] = {
    val mediator = new RateLimiterCacheMediator(cache, rateLimit, apiClient, applicationName)
    mediator.alreadyReached.flatMap { alreadyReached =>
      if (alreadyReached) {
        listener.notify(RateLimitEvent.Overflowing(apiClient))
        mediator.expiry.map(RateLimitStatus.Reached(rateLimit.maximumNrOfRequests, _))
      } else {
        mediator.requestsMadeSoFar.flatMap {
          case Some(number) if number == rateLimit.maximumNrOfRequests =>
            Future.join(mediator.expiry, mediator.markAsReached).map { case (expiry, _) =>
              val status = RateLimitStatus.Reached(rateLimit.maximumNrOfRequests, expiry)
              val time = LocalDateTime.now
              listener.notify(RateLimitEvent.Reached(status, apiClient))
              listener.notify(RateLimitEvent.Overflowing(apiClient))
              status
            }
          case Some(_) =>
            Future.join(mediator.updateRequestCount, mediator.expiry).map { case (updatedRequestCount, expiry) =>
              RateLimitStatus.Advancing(rateLimit.maximumNrOfRequests - updatedRequestCount, expiry)
            }
          case None =>
            mediator.establish.map { expiry =>
              RateLimitStatus.Advancing(rateLimit.maximumNrOfRequests - 1, Some(expiry))
            }
        }
      }
    }
  }

  def rateLimitStatus(apiClient: ApiClient): Future[RateLimitStatus] = {
    val mediator = new RateLimiterCacheMediator(cache, rateLimit, apiClient, applicationName)
    Future.join(mediator.expiry, mediator.alreadyReached) flatMap { case (expiry, alreadyReached) =>
      if (alreadyReached)
        Future(RateLimitStatus.Reached(rateLimit.maximumNrOfRequests, expiry))
      else
        mediator.requestsMadeSoFar map { requestsOpt =>
          val requests = requestsOpt.getOrElse(0L)
          RateLimitStatus.Advancing(rateLimit.maximumNrOfRequests - requests, expiry)
        }
    }
  }
}
