package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.ratelimiting.types.RateLimit

case class ClientSpecificRateLimit(apiClient: ApiClient, rateLimit: RateLimit, applicationName: ResourceName) {
  private[this] val prefix = s"${applicationName.getName}.rateLimit.${apiClient.cacheKey}.${rateLimit.identifier}"
  val counterCacheKey = s"$prefix.counter"
  val expiryCacheKey = s"$prefix.expiry"
  val reachedCacheKey = s"$prefix.reached"
}
