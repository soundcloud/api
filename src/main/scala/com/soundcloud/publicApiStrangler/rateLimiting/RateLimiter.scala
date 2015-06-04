package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.config.Config
import com.soundcloud.ratelimiting.types.RateLimit
import com.soundcloud.scalakit.cache.Cache
import com.twitter.finagle.http.Request
import com.twitter.util.Future

class RateLimiter(val groupName: String, individualRateLimiters: Seq[IndividualRateLimiter]) {
  private def applicableRateLimiters(request: Request): Seq[IndividualRateLimiter] = {
    individualRateLimiters.filter(_ appliesTo request)
  }

  def appliesTo(request: Request): Boolean = {
    applicableRateLimiters(request).nonEmpty
  }

  def advanceRateLimitStatus(apiClient: ApiClient, request: Request): Future[CompositeRateLimitStatus] = {
    val limiters = applicableRateLimiters(request)
    for {
      statuses <- Future.collect(limiters.map(_.rateLimitStatus(apiClient)))
      oldCompositeStatus = CompositeRateLimitStatus(statuses.toSet)
      updatedStatuses <- if (!oldCompositeStatus.hasReachedLimit) Future.collect(limiters.map(_.advanceRateLimitStatus(apiClient))).map(Some(_)) else Future.None
      updatedCompositeStatus = updatedStatuses.map(s => CompositeRateLimitStatus(s.toSet))
    } yield {
      updatedCompositeStatus getOrElse oldCompositeStatus
    }
  }
}

object RateLimiter {
  def from(cache: Cache, config: Config, listeners: Seq[EventListener[RateLimitEvent]]): RateLimiter = {
    val rateLimits = RateLimit.parse(config.get("RATE_LIMITS"))
    val individualRateLimiters = rateLimits.map(new IndividualRateLimiter(cache, _, config.getApplicationResourceName, listeners))
    new RateLimiter("default", individualRateLimiters)
  }
}
