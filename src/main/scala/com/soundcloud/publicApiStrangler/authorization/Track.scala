package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.policies.ContentAuthorization
import com.soundcloud.scalakit.json.{Json => ScalakitJson}
import play.api.libs.json.{JsObject, JsValue, Json => PlayJson}

class Track(val json: JsValue) {
  def withContentAuthorization(auth: ContentAuthorization): JsObject = {
    withoutContentAuthorization ++ sorry(Map(
      "policy" -> auth.getPolicy,
      "monetization_model" -> auth.getMonetizationModel
    ))
  }

  def withoutContentAuthorization: JsObject = {
    json.as[JsObject]
  }

  def sorry(m: Map[String, _]): JsObject = PlayJson.parse(ScalakitJson.asString(m)).as[JsObject]
}
