package com.soundcloud.publicApiStrangler.rateLimiting

class RateLimiterRegistry(defaultRateLimiter: RateLimiter) {

  def lookup(clientApplication: ApiClient): RateLimiter = defaultRateLimiter

}
