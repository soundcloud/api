package com.soudcloud.authorization

import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.UserSession
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import com.soundcloud.jvmkit.policies.ContentAuthorization

object ApplyTrackPolicies {

  def apply(session: UserSession, visitor: TracksVisitor, rules: Seq[ContentAuthorization]) =
    visit(session, visitor, policiesByUrn(rules))

  private def visit(session: UserSession, visitor: TracksVisitor, policiesByUrn: Map[Urn, TrackPolicies]) =
    visitor.apply {
      case (urn, track) =>
        policiesByUrn(urn).apply(session, track)
    }

  private def policiesByUrn(rules: Seq[ContentAuthorization]) =
    rules.map(rule => rule.getUrn -> new TrackPolicies(rule.getPolicy)).toMap
}
