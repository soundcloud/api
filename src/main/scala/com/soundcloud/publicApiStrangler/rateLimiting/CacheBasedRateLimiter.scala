package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.scalakit.cache.Cache
import com.twitter.util.Future
import play.api.libs.json.Json

class CacheBasedRateLimiter(cache: Cache, val rateLimit: RateLimit, applicationName: ResourceName) extends RateLimiter {

  val logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  def advanceRateLimitStatus(apiClient: ApiClient): Future[RateLimitStatus] = {
    val mediator = new RateLimiterCacheMediator(cache, rateLimit, apiClient, applicationName)
    mediator.alreadyReached.flatMap { alreadyReached =>
      if (alreadyReached) {
        mediator.expiry.map(RateLimitStatus.Reached(rateLimit.maximumNrOfRequests, _))
      } else {
        mediator.requestsMadeSoFar.flatMap {
          case Some(number) if number == rateLimit.maximumNrOfRequests =>
            Future.join(mediator.expiry, mediator.markAsReached).map { case (expiry, _) =>
              val status = RateLimitStatus.Reached(rateLimit.maximumNrOfRequests, expiry)
              logger.info(s"client-urn=${apiClient.urn} rate-limit-status='${Json.stringify(Json.toJson(status))}'")
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
