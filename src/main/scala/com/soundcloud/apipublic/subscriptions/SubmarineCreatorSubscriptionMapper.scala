package com.soundcloud.apipublic.subscriptions

import play.api.libs.json.{JsNull, JsValue}

class SubmarineCreatorSubscriptionMapper {
  def apply(json: JsValue): Option[SubmarineCreatorSubscription] = {
    json match {
      case JsNull => None
      case json: JsValue =>
        Some(
          SubmarineCreatorSubscription(
            (json \ "recurring").asOpt[Boolean].getOrElse(false),
            Package(
              (json \ "package" \ "name").as[String],
              (json \ "package" \ "plan").as[String]
            )
          )
        )
    }
  }
}
