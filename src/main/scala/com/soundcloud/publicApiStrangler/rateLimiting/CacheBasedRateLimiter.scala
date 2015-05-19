package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.ratelimiting.types.RateLimit
import com.soundcloud.scalakit.ResourceName
import com.soundcloud.scalakit.cache.Cache
import com.twitter.util.Future

class CacheBasedRateLimiter(cache: Cache,
                             val rateLimit: RateLimit,
                             applicationName: ResourceName,
                             listeners: Seq[EventListener[RateLimitEvent]]
                             ) extends RateLimiter {

  val Reached = RateLimitStatus.Reached.from(rateLimit) _
  val Advancing = RateLimitStatus.Advancing.from(rateLimit) _

  def advanceRateLimitStatus(apiClient: ApiClient): Future[RateLimitStatus] = {
    val mediator = new RateLimiterCacheMediator(cache, rateLimit, apiClient, applicationName)
    mediator.alreadyReached.flatMap { alreadyReached =>
      if (alreadyReached) {
        notifyListeners(RateLimitEvent.Overflowing(apiClient))
        mediator.expiry.map(Reached)
      } else {
        mediator.requestsMadeSoFar.flatMap {
          case Some(number) if number == rateLimit.maximumNrOfRequests =>
            Future.join(mediator.expiry, mediator.markAsReached).map { case (expiry, _) =>
              val status = Reached(expiry)
              notifyListeners(RateLimitEvent.LimitReached(status, apiClient))
              notifyListeners(RateLimitEvent.Overflowing(apiClient))
              status
            }
          case Some(_) =>
            Future.join(mediator.updateRequestCount, mediator.expiry).map { case (updatedRequestCount, expiry) =>
              val status = Advancing(updatedRequestCount.getOrElse(1), expiry)
              notifyListeners(RateLimitEvent.PercentageReached(status, apiClient))
              status
            }
          case None =>
            mediator.establish.map { expiry => Advancing(1, Some(expiry)) }
        }
      }
    }
  }

  def rateLimitStatus(apiClient: ApiClient): Future[RateLimitStatus] = {
    val mediator = new RateLimiterCacheMediator(cache, rateLimit, apiClient, applicationName)
    Future.join(mediator.expiry, mediator.alreadyReached) flatMap { case (expiry, alreadyReached) =>
      if (alreadyReached)
        Future(Reached(expiry))
      else
        mediator.requestsMadeSoFar map { requestsOpt =>
          val requests = requestsOpt.getOrElse(0L)
          Advancing(requests, expiry)
        }
    }
  }

  def notifyListeners(event: RateLimitEvent): Unit = {
    listeners.foreach(_.notify(event))
  }
}
