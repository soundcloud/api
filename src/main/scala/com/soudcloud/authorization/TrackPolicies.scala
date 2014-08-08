package com.soudcloud.authorization

import com.soundcloud.scalakit.UserSession
import com.soundcloud.scalakit.json.Json
import play.api.libs.json.JsObject
import com.soundcloud.jvmkit.policies.ContentPolicies

class TrackPolicies(raw: ContentPolicies) {

  private def policyField = "policy" -> Json.toJsValue(raw)

  def apply(session: UserSession, track: JsObject) =
    if (raw != ContentPolicies.BLOCK)
      Some(JsObject(track.fields :+ policyField))
    else
      None
}
