package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.ratelimiting.core.{ActionableAccessMechanism, RateLimitGroup, RateLimitMode}
import com.soundcloud.ratelimiting.events.{Event, ReachedEventPayload}
import com.soundcloud.scalakit._
import com.soundcloud.scalakit.cache.Cache
import com.twitter.finagle.http.Request
import com.twitter.util.Future

class RateLimiter(val groupName: String, individualRateLimiters: Seq[IndividualRateLimiter]) {

  private def applicableRateLimiters(request: Request): Seq[IndividualRateLimiter] = {
    individualRateLimiters.filter { limiter =>
      limiter.appliesTo(request) && limiter.rateLimit.mode != RateLimitMode.Disabled
    }
  }

  private def visibleRateLimiters: Seq[IndividualRateLimiter] = {
    individualRateLimiters.filter(_.rateLimit.mode == RateLimitMode.Enforcing)
  }

  def appliesTo(request: Request): Boolean = {
    applicableRateLimiters(request).nonEmpty
  }

  def currentStatus(accessMechanism: ActionableAccessMechanism): Future[CompositeRateLimitStatus] = {
    for {
      statuses <- Future.collect(visibleRateLimiters.map(_.rateLimitStatus(accessMechanism)))
    } yield CompositeRateLimitStatus(statuses.toSet)
  }

  def advanceRateLimitStatus(accessMechanism: ActionableAccessMechanism, request: Request): Future[CompositeRateLimitStatus] = {
    val limiters = applicableRateLimiters(request)
    for {
      statuses <- Future.collect(limiters.map(_.rateLimitStatus(accessMechanism)))
      oldCompositeStatus = CompositeRateLimitStatus(statuses.toSet)
      updatedStatuses <- if (!oldCompositeStatus.hasReachedLimit) Future.collect(limiters.map(_.advanceRateLimitStatus(accessMechanism))).map(Some(_)) else Future.None
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
      listeners: Seq[EventListener[Event[ReachedEventPayload]]],
      applicationResourceName: ResourceName): RateLimiter = {

    val individualLimiters = rateLimitGroup.rateLimits.map { rateLimit =>
      new IndividualRateLimiter(cache, rateLimit, applicationResourceName, listeners)
    }
    new RateLimiter(rateLimitGroup.id, individualLimiters)
  }
}
