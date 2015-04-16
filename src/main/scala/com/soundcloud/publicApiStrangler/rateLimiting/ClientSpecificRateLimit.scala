package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.ResourceName

case class ClientSpecificRateLimit(apiClient: ApiClient, rateLimit: RateLimit, applicationName: ResourceName) {
  lazy val prefix = s"${applicationName.getName}.rateLimit.${apiClient.identifier}.${rateLimit.identifier}"
  lazy val counterKey = s"$prefix.counter"
  lazy val expiryKey = s"$prefix.expiry"
}
