package com.soundcloud.authorization

import com.soundcloud.jvmkit.policies.{ContentAuthorization, ContentPolicies}
import com.soundcloud.scalakit.{Urn, UserSession}

object ApplyTrackPolicies {

  def apply(session: UserSession, visitor: TracksVisitor, rules: Seq[ContentAuthorization]) =
    visit(session, visitor, policiesByUrn(rules))

  private def visit(session: UserSession, visitor: TracksVisitor, policiesByUrn: Map[Urn, ContentPolicies]) =
    visitor.apply {
      case (urn, track) =>
        val policies = policiesByUrn(urn)
        if (policies != ContentPolicies.BLOCK)
          Some(track.withPolicies(policies))
        else
          None
    }

  private def policiesByUrn(rules: Seq[ContentAuthorization]) =
    rules.map(rule => rule.getUrn -> rule.getPolicy).toMap
}
