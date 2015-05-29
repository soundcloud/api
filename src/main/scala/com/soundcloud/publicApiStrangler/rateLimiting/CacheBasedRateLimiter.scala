package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.ratelimiting.types.RateLimit
import com.soundcloud.scalakit.ResourceName
import com.soundcloud.scalakit.cache.Cache
import com.twitter.finagle.http.Request
import com.twitter.util.{Time, Future}

class CacheBasedRateLimiter(cache: Cache,
                            rateLimit: RateLimit,
                            applicationName: ResourceName,
                            listeners: Seq[EventListener[RateLimitEvent]]) extends RateLimiter {

  val advancing = RateLimitStatus(rateLimit, _: Int, _: Option[Time])
  val reached = RateLimitStatus.reached(rateLimit, _: Option[Time])

  def appliesTo(request: Request) = {
    rateLimit.appliesTo(request.path)
  }

  def advanceRateLimitStatus(apiClient: ApiClient): Future[RateLimitStatus] = {
    val mediator = new RateLimiterCacheMediator(cache, rateLimit, apiClient, applicationName)
    mediator.alreadyReached.flatMap { alreadyReached =>
      if (alreadyReached) {
        notifyListeners(RateLimitEvent.Overflowing(apiClient))
        mediator.expiry.map(reached)
      } else {
        mediator.requestsMadeSoFar.flatMap {
          case Some(number) if number >= rateLimit.maximumNrOfRequests =>
            Future.join(mediator.expiry, mediator.markAsReached).map { case (expiry, _) =>
              val status = reached(expiry)
              notifyListeners(RateLimitEvent.LimitReached(status, apiClient))
              notifyListeners(RateLimitEvent.Overflowing(apiClient))
              status
            }
          case Some(_) =>
            Future.join(mediator.updateRequestCount, mediator.expiry).map { case (updatedRequestCount, expiry) =>
              val status = advancing(updatedRequestCount.map(_.toInt).getOrElse(1), expiry)
              notifyListeners(RateLimitEvent.PercentageReached(status, apiClient))
              status
            }
          case None =>
            mediator.establish.map { expiry => advancing(1, Some(expiry)) }
        }
      }
    }
  }

  def rateLimitStatus(apiClient: ApiClient): Future[RateLimitStatus] = {
    val mediator = new RateLimiterCacheMediator(cache, rateLimit, apiClient, applicationName)
    Future.join(mediator.expiry, mediator.alreadyReached) flatMap { case (expiry, alreadyReached) =>
      if (alreadyReached)
        Future(reached(expiry))
      else
        mediator.requestsMadeSoFar map { requestsOpt =>
          val requests = requestsOpt.map(_.toInt).getOrElse(0)
          advancing(requests, expiry)
        }
    }
  }

  def notifyListeners(event: RateLimitEvent): Unit = {
    listeners.foreach(_.notify(event))
  }
}
