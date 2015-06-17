package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.ratelimiting.clients.ClientConfiguration
import com.soundcloud.ratelimiting.core.{RateLimitMode, ClientApplication, RateLimitGroup}
import com.soundcloud.scalakit._
import com.soundcloud.scalakit.cache.Cache
import com.twitter.finagle.http.Request
import com.twitter.util.Future

class RateLimiter(val groupName: String, individualRateLimiters: Seq[IndividualRateLimiter]) {

  private val logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  private def applicableRateLimiters(request: Request): Seq[IndividualRateLimiter] = {
    individualRateLimiters.filter { limiter =>
      limiter.appliesTo(request) && limiter.rateLimit.mode != RateLimitMode.Disabled
    }
  }

  def appliesTo(request: Request): Boolean = {
    applicableRateLimiters(request).nonEmpty
  }

  def advanceRateLimitStatus(clientApplication: ClientApplication, request: Request): Future[CompositeRateLimitStatus] = {
    val limiters = applicableRateLimiters(request)
    for {
      statuses <- Future.collect(limiters.map(_.rateLimitStatus(clientApplication)))
      oldCompositeStatus = CompositeRateLimitStatus(statuses.toSet)
      updatedStatuses <- if (!oldCompositeStatus.hasReachedLimit) Future.collect(limiters.map(_.advanceRateLimitStatus(clientApplication))).map(Some(_)) else Future.None
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
