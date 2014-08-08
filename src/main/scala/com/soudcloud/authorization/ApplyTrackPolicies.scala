package com.soudcloud.authorization

import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.UserSession
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import com.soundcloud.jvmkit.policies.ContentAuthorization

object ApplyTrackPolicies {

  def apply(session: UserSession, json: JsValue, rules: Seq[ContentAuthorization]) =
    policiesVisitor(session, policiesByUrn(rules)).apply(json)

  private def policiesVisitor(session: UserSession, policiesByUrn: Map[Urn, TrackPolicies]) =
    new TracksVisitor {
      def visit(urn: Urn, track: JsObject) =
        policiesByUrn(urn).apply(session, track)
    }

  private def policiesByUrn(rules: Seq[ContentAuthorization]) =
    rules.map(rule => rule.getUrn -> new TrackPolicies(rule.getPolicy)).toMap
}
