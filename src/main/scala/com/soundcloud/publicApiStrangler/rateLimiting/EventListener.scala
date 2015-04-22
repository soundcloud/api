package com.soundcloud.publicApiStrangler.rateLimiting

trait EventListener[E] {
  def notify(event: E): Unit
}
