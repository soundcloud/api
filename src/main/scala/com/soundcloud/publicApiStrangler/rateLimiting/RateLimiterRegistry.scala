package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.ratelimiting.groups.RateLimitGroupRepository
import com.soundcloud.scalakit.cache.Cache
import com.soundcloud.scalakit.{ResourceName, Urn}
import com.twitter.util.Future

class RateLimiterRegistry(
  rateLimitGroupRepository: RateLimitGroupRepository,
  rateLimitEventListeners: Seq[EventListener[RateLimitEvent]],
  cache: Cache,
  applicationResourceName: ResourceName) {

  val application = Urn("soundcloud", "systems", applicationResourceName.getName)

  def lookup(clientApplication: ApiClient): Future[RateLimiter] = {
    for {
      defaultGroup <- findDefaultGroup
    } yield RateLimiter.from(cache, defaultGroup, rateLimitEventListeners, applicationResourceName)
  }

  private def findDefaultGroup = {
    rateLimitGroupRepository.forId(application, "default") map (_.getOrElse(throw new RuntimeException(
      s"No default rate limit configured for $application")))
  }

}
