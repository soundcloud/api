package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.ratelimiting.core.ClientApplication
import org.joda.time.{DateTime, DateTimeZone}

sealed trait RateLimitEvent {
  def clientApplication: ClientApplication
  val occurredAt: DateTime = DateTime.now(DateTimeZone.UTC)
}

object RateLimitEvent {
  case class CheckpointReached(status: RateLimitStatus, clientApplication: ClientApplication) extends RateLimitEvent {
    def hasReachedLimit: Boolean = status.hasReachedLimit
  }
  case class Overflowing(clientApplication: ClientApplication) extends RateLimitEvent
}