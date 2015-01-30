package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.policies.ContentPolicy
import play.api.libs.json.JsValue
import play.api.libs.json.JsObject
import com.soundcloud.scalakit.json.Json

class Track(val json: JsValue) {

  def withPolicies(policies: ContentPolicy) =
    JsObject(json.as[JsObject].fields :+ policyField(policies))

  private def policyField(policies: ContentPolicy) =
    "policy" -> Json.toJsValue(policies)
}
