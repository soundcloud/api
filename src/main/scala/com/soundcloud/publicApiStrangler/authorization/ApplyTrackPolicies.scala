package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.UserSession
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import com.soundcloud.jvmkit.policies.ContentAuthorization
import com.soundcloud.jvmkit.policies.ContentPolicy

object ApplyTrackPolicies {

  def apply(session: UserSession, visitor: TracksVisitor, rules: Seq[ContentAuthorization]) =
    visit(session, visitor, policiesByUrn(rules))

  private def visit(session: UserSession, visitor: TracksVisitor, policiesByUrn: Map[Urn, ContentPolicy]) =
    visitor.apply {
      case (urn, track) =>
        val policies = policiesByUrn(urn)
        if (policies != ContentPolicy.BLOCK)
          Some(track.withPolicies(policies))
        else
          None
    }

  private def policiesByUrn(rules: Seq[ContentAuthorization]) =
    rules.map(rule => rule.getUrn -> rule.getPolicy).toMap
}
