package com.soundcloud.publicApiStrangler.rateLimiting.reporting

import com.soundcloud.jvmkit.Geo
import com.soundcloud.publicApiStrangler.rateLimiting.{EventListener, RateLimitEvent}
import com.soundcloud.ratelimiting.client.RateLimitEventsClient
import com.soundcloud.scalakit.{AnonymousUserSession, Urn}
import com.twitter.util.FuturePool

object ReportingRateLimitEventListener {
  val stranglerUrn = Urn("soundcloud", "systems", "public-api-strangler")
  val session = AnonymousUserSession(stranglerUrn, Geo.UNKNOWN_GEO, Set.empty)
}

class ReportingRateLimitEventListener(service: RateLimitEventsClient) extends EventListener[RateLimitEvent] {

  def notify(event: RateLimitEvent): Unit = FuturePool.unboundedPool {
    PublishingDecision(event).right.foreach { reached =>
      service.publish(ReportingRateLimitEventListener.session, EventMapper(reached, event.occurredAt))
    }
  }

}