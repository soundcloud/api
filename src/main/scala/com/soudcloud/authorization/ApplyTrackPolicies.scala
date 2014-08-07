package com.soudcloud.authorization

import com.soudcloud.data.ParsedValue
import com.soundcloud.jvmkit.policies.ContentAuthorization
import com.soundcloud.scalakit.{Urn, UserSession}

object ApplyTrackPolicies {

  def apply(session: UserSession, data: ParsedValue, rules: Seq[ContentAuthorization]) =
    policiesVisitor(session, policiesByUrn(rules)).apply(data)

  private def policiesVisitor(session: UserSession, policiesByUrn: Map[Urn, TrackPolicies]) =
    new TracksVisitor {
      def visit(urn: Urn, track: ParsedValue) =
        policiesByUrn(urn).apply(session, track)
    }

  private def policiesByUrn(authorizations: Seq[ContentAuthorization]) =
    authorizations.map(authorization => authorization.getUrn -> new TrackPolicies(authorization.getPolicy)).toMap
}
