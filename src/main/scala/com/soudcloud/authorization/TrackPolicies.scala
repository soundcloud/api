package com.soudcloud.authorization

import com.soundcloud.bff.authorization.Policies
import com.soundcloud.bff.authorization.Policies.blocked
import com.soundcloud.scalakit.UserSession
import com.soundcloud.scalakit.json.Json

import play.api.libs.json.JsObject

class TrackPolicies(raw: Policies) {

  private def policiesField = "policies" -> Json.toJsValue(raw)

  def apply(session: UserSession, track: JsObject) =
    if (raw.playback != blocked && raw.metadata != blocked)
      Some(JsObject(track.fields :+ policiesField))
    else
      None
}
