package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.ratelimiting.core.{ClientApplication, RateLimit}

case class ClientSpecificRateLimit(clientApplication: ClientApplication, rateLimit: RateLimit, applicationName: ResourceName) {
  private[this] val prefix = s"${applicationName.getName}.rateLimit.${clientApplication.cacheKey}.${rateLimit.identifier}"
  val counterCacheKey = s"$prefix.counter"
  val expiryCacheKey = s"$prefix.expiry"
  val reachedCacheKey = s"$prefix.reached"
}
