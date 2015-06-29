package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.ratelimiting.core._
import com.soundcloud.ratelimiting.events.{Event, ReachedEventPayload}
import com.soundcloud.scalakit.ResourceName
import com.soundcloud.scalakit.cache.Cache
import com.twitter.finagle.http.Request

class IndividualRateLimiter(cache: Cache,
                            val rateLimit: RateLimit,
                            applicationName: ResourceName,
                            listeners: Seq[EventListener[Event[ReachedEventPayload]]]) {

  def appliesTo(request: Request) = rateLimit.appliesTo(request.path)

  def notifyListeners(event: Event[ReachedEventPayload]): Unit = {
    listeners.foreach(_.notify(event))
  }

  def bind(accessMechanism: ActionableAccessMechanism): BoundIndividualRateLimiter = {
    new BoundIndividualRateLimiter(
      cache, listeners, AccessDependentRateLimit(accessMechanism, rateLimit, applicationName))
  }

}
