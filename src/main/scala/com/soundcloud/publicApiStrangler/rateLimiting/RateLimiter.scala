package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.ratelimiting.core.RateLimitGroup
import com.soundcloud.scalakit.ResourceName
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

  def from(
      cache: Cache,
      rateLimitGroup: RateLimitGroup,
      listeners: Seq[EventListener[RateLimitEvent]],
      applicationResourceName: ResourceName): RateLimiter = {

    val individualLimiters = rateLimitGroup.rateLimits.map { rateLimit =>
      new IndividualRateLimiter(cache, rateLimit, applicationResourceName, listeners)
    }
    new RateLimiter(rateLimitGroup.id, individualLimiters)
  }

}
