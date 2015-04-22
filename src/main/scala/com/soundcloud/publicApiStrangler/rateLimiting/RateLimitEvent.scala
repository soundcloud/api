package com.soundcloud.publicApiStrangler.rateLimiting

import org.joda.time.LocalDateTime

sealed trait RateLimitEvent {
  def apiClient: ApiClient
  val occurredAt = LocalDateTime.now
}

object RateLimitEvent {
  case class Reached(status: RateLimitStatus.Reached, apiClient: ApiClient) extends RateLimitEvent
  case class Overflowing(apiClient: ApiClient) extends RateLimitEvent
}
