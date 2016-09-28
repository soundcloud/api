package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.policies.ContentAuthorization
import com.soundcloud.scalakit.json.{UntypedJson}
import play.api.libs.json.{JsObject, JsValue, Json}

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

  // FIXME: Need a nicer way to convert MonetizationModel and ContentPolicy to JSON
  def sorry(m: Map[String, _]): JsObject = Json.parse(UntypedJson.asString(m)).as[JsObject]
}
