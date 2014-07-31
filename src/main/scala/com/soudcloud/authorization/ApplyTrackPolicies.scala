package com.soudcloud.authorization

import com.soudcloud.data.{ParsedValue}
import com.soundcloud.bff.authorization.AuthorizationRules
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.UserSession

object ApplyTrackPolicies {

  def apply(session: UserSession, data: ParsedValue, rules: Seq[AuthorizationRules]) =
    policiesVisitor(session, policiesByUrn(rules)).apply(data)

  private def policiesVisitor(session: UserSession, policiesByUrn: Map[Urn, TrackPolicies]) =
    new TracksVisitor {
      def visit(urn: Urn, track: ParsedValue) =
        policiesByUrn(urn).apply(session, track)
    }

  private def policiesByUrn(rules: Seq[AuthorizationRules]) =
    rules.map(rule => rule.urn -> new TrackPolicies(rule.policies)).toMap
}
