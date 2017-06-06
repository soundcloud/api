package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.publicApiStrangler.authorization.policies
import play.api.libs.json.{JsObject, JsValue, Json}

class Track(val json: JsValue) {
  def withContentAuthorization(auth: policies.ContentAuthorization): JsObject = {
    withoutContentAuthorization ++ Json.obj(
      "policy" -> auth.getPolicy.toString,
      "monetization_model" -> auth.getMonetizationModel.toString
    )
  }

  def withoutContentAuthorization: JsObject = {
    json.as[JsObject]
  }
}
