package com.soundcloud.publicApiStrangler.rateLimiting.reporting

import com.soundcloud.jvmkit.{Geo, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.rateLimiting.EventListener
import com.soundcloud.ratelimiting.events.{Event, ReachedEventPayload}
import com.soundcloud.ratelimiting.gateways.RateLimitEventsGateway
import com.soundcloud.scalakit.Urn
import com.twitter.util.FuturePool

object ReportingRateLimitEventListener {
  val stranglerUrn = Urn("soundcloud", "systems", "public-api-strangler")
  val session = new UserSessionBuilder().setAgent(stranglerUrn).setGeo(Geo.UNKNOWN_GEO).build()
}

class ReportingRateLimitEventListener(service: RateLimitEventsGateway) extends EventListener[Event[ReachedEventPayload]] {

  def notify(event: Event[ReachedEventPayload]): Unit = FuturePool.unboundedPool {
    if (PublishingDecision.shouldPublish(event))
      service.publish(ReportingRateLimitEventListener.session, event)
  }
}