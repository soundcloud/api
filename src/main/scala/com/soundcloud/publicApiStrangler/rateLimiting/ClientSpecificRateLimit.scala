package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.ResourceName

case class ClientSpecificRateLimit(apiClient: ApiClient, rateLimit: RateLimit, applicationName: ResourceName) {
  private[this] val prefix = s"${applicationName.getName}.rateLimit.${apiClient.cacheKey}.${rateLimit.identifier}"
  val counterKey = s"$prefix.counter"
  val expiryKey = s"$prefix.expiry"
  val reachedKey = s"$prefix.reached"
}
