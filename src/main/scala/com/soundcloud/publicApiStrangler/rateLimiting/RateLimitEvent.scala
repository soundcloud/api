package com.soundcloud.publicApiStrangler.rateLimiting

import org.joda.time.{DateTime, DateTimeZone}

sealed trait RateLimitEvent {
  def apiClient: ApiClient
  val occurredAt: DateTime = DateTime.now(DateTimeZone.UTC)
}

object RateLimitEvent {
  case class CheckpointReached(status: RateLimitStatus, apiClient: ApiClient) extends RateLimitEvent {
    def hasReachedLimit: Boolean = status.hasReachedLimit
  }
  case class Overflowing(apiClient: ApiClient) extends RateLimitEvent
}