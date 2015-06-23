package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.ratelimiting.core.ClientApplication
import com.soundcloud.ratelimiting.events.{Event, ReachedEventPayload}
import com.soundcloud.ratelimiting.groups.RateLimitGroupLookupService
import com.soundcloud.scalakit.cache.Cache
import com.soundcloud.scalakit.{ResourceName, Urn}
import com.twitter.util.Future

class RateLimiterRegistry(
  rateLimitGroupLookupService: RateLimitGroupLookupService,
  rateLimitEventListeners: Seq[EventListener[Event[ReachedEventPayload]]],
  cache: Cache,
  applicationResourceName: ResourceName) {

  val application = Urn("soundcloud", "systems", applicationResourceName.getName)

  def lookup(clientApplication: ClientApplication): Future[RateLimiter] = {
    for {
      rateLimitGroup <- rateLimitGroupLookupService.rateLimitGroupFor(application, clientApplication.urn)
    } yield RateLimiter.from(cache, rateLimitGroup, rateLimitEventListeners, applicationResourceName)
  }

}
