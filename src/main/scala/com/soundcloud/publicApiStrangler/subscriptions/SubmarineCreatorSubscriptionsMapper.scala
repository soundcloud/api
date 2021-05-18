package com.soundcloud.publicApiStrangler.subscriptions

import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.JsValue

class SubmarineCreatorSubscriptionsMapper(
    subscriptionMapper: SubmarineCreatorSubscriptionMapper = new SubmarineCreatorSubscriptionMapper()
) {
  def apply(json: JsValue): Map[Urn, Option[SubmarineCreatorSubscription]] = {
    (json \ "data").as[Map[String, JsValue]].map {
      case (urn, json) => {
        val creatorSubscription = subscriptionMapper(json)
        (Urn.parse(urn).get, creatorSubscription)
      }
    }
  }
}
