package com.soundcloud.publicApiStrangler.mapping.purchaselink

import com.soundcloud.publicApiStrangler.support.mapping.ObjectMapping
import com.soundcloud.service.response.representation.TrackPurchaseLink

trait PurchaseLink extends ObjectMapping[TrackPurchaseLink] {
  def url = resource.url
}
