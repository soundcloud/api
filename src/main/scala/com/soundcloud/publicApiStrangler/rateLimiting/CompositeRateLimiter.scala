package com.soundcloud.publicApiStrangler.rateLimiting

import com.twitter.finagle.http.Request
import com.twitter.util.Future

class CompositeRateLimiter(rateLimiters: Seq[RateLimiter]) {
  private def applicableRateLimiters(request: Request): Seq[RateLimiter] = {
    rateLimiters.filter(_ appliesTo request)
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
