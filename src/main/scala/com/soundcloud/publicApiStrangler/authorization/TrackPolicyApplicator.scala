package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.TrackDurationActionStatus._
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentAuthorization, ContentPolicy, MonetizationModel}
import play.api.libs.json.{JsObject, JsValue, Json}

case class TrackPolicyApplicator(clientWhitelist: Set[Urn]) {
  val durationJsonPropertyName = "duration"

  def apply(session: UserSession, visitor: TracksVisitor, rules: Seq[ContentAuthorization], durationActions: List[TrackDurationAction]): Option[JsValue] =
    visit(session, visitor, policiesByUrn(rules), durationsByUrn(durationActions))

  private def visit(session: UserSession, visitor: TracksVisitor, authorizations: Map[Urn, ContentAuthorization], durationActions: Map[Urn, TrackDurationAction]): Option[JsValue] =
    visitor.apply {
      case (urn, track) =>
        val contentAuth = authorizations(urn)
        if (allowTrack(contentAuth, session))
          Some(
            potentiallyAddContentAuthorization(
              potentiallyReplaceDurations(track, durationActions(urn)),
              contentAuth,
              session
            )
          )
        else
          None
    }

  private def allowTrack(contentAuth: ContentAuthorization, userSession: UserSession): Boolean = {
    contentAuth.getPolicy match {
      case ContentPolicy.BLOCK => false
      case ContentPolicy.MONETIZE => contentAuth.getMonetizationModel match {
        case MonetizationModel.SUB_HIGH_TIER | MonetizationModel.SUB_MID_TIER => userAgentIsWhitelisted(userSession)
        case _ => true
      }
      case _ => true
    }
  }

  private def policiesByUrn(rules: Seq[ContentAuthorization]): Map[Urn, ContentAuthorization] =
    rules.map(rule => rule.getUrn -> rule).toMap

  private def durationsByUrn(durationActions: List[TrackDurationAction]): Map[Urn, TrackDurationAction] =
    durationActions.map(durationAction => durationAction.urn -> durationAction).toMap

  private def potentiallyAddContentAuthorization(track: Track, contentAuthorization: ContentAuthorization, userSession: UserSession): JsObject = {
    if (userAgentIsWhitelisted(userSession)) {
      track.withContentAuthorization(contentAuthorization)
    } else {
      track.withoutContentAuthorization
    }
  }

  private def userAgentIsWhitelisted(userSession: UserSession) = {
    clientWhitelist.contains(userSession.getAgent)
  }

  private def potentiallyReplaceDurations(track: Track, durationAction: TrackDurationAction): Track = {
    durationAction match {
      case TrackDurationAction(_, NeedsModification, Some(duration)) =>
        new Track(track.json.as[JsObject] ++ Json.obj(durationJsonPropertyName -> duration))
      case _ => track
    }
  }
}
