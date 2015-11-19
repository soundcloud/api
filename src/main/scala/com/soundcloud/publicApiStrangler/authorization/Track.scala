package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.Json
import com.soundcloud.jvmkit.policies.ContentAuthorization
import play.api.libs.json.{JsObject, JsValue}

class Track(val json: JsValue) {
  def withContentAuthorization(auth: ContentAuthorization): JsObject = {
    withoutContentAuthorization ++ Json.toJsValue(Map("policy" -> auth.getPolicy, "monetization_model" -> auth.getMonetizationModel))
  }

  def withoutContentAuthorization: JsObject = {
    json.as[JsObject]
  }
}
