package com.soundcloud.publicApiStrangler.rateLimiting

import org.joda.time.{DateTime, DateTimeZone}

sealed trait RateLimitEvent {
  def apiClient: ApiClient
  val occurredAt: DateTime = DateTime.now(DateTimeZone.UTC)
}

object RateLimitEvent {
  sealed trait Reached extends RateLimitEvent {
    def status: RateLimitStatus
    def apiClient: ApiClient
  }

  case class LimitReached(status: RateLimitStatus.Reached, apiClient: ApiClient) extends Reached
  case class PercentageReached(status: RateLimitStatus.Advancing, apiClient: ApiClient) extends Reached
  case class Overflowing(apiClient: ApiClient) extends RateLimitEvent
}