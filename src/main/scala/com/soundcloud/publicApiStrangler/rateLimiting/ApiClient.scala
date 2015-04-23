package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.scalakit.Urn

case class ApiClient(urn: Urn) {
  def cacheKey: String = s"client_${urn.getIdentifier}"
  def identifier: String = urn.getIdentifier
}
