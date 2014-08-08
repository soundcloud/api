package com.soudcloud.authorization

import com.soundcloud.bff.authorization.AuthorizationRules
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.UserSession

import play.api.libs.json.JsObject
import play.api.libs.json.JsValue

object ApplyTrackPolicies {

  def apply(session: UserSession, json: JsValue, rules: Seq[AuthorizationRules]) =
    policiesVisitor(session, policiesByUrn(rules)).apply(json)

  private def policiesVisitor(session: UserSession, policiesByUrn: Map[Urn, TrackPolicies]) =
    new TracksVisitor {
      def visit(urn: Urn, track: JsObject) =
        policiesByUrn(urn).apply(session, track)
    }

  private def policiesByUrn(rules: Seq[AuthorizationRules]) =
    rules.map(rule => rule.urn -> new TrackPolicies(rule.policies)).toMap
}
