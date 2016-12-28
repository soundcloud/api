package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.policies.ContentAuthorization
import com.soundcloud.scalakit.json.{UntypedJson}
import play.api.libs.json.{JsObject, JsValue, Json}

class Track(val json: JsValue) {
  def withContentAuthorization(auth: ContentAuthorization): JsObject = {
    withoutContentAuthorization ++ Json.obj(
      "policy" -> auth.getPolicy.toString,
      "monetization_model" -> auth.getMonetizationModel.toString
    )
  }

  def withoutContentAuthorization: JsObject = {
    json.as[JsObject]
  }
}
