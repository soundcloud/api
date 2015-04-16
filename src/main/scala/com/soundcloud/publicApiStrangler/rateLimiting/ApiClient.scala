package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.scalakit.Urn

case class ApiClient(urn: Urn) {
  def identifier: String = s"client_${urn.getIdentifier}"
}
