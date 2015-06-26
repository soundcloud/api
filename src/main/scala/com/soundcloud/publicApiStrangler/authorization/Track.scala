package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.Json
import com.soundcloud.jvmkit.policies.ContentAuthorization
import play.api.libs.json.{JsObject, JsValue}

class Track(val json: JsValue) {

  def withContentAuthorization(auth: ContentAuthorization) = {
    json.as[JsObject] ++ Json.toJsValue(Map("policy" -> auth.getPolicy, "monetization_model" -> auth.getMonetizationModel))
  }
}
