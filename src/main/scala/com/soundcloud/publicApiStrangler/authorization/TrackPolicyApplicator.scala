package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.TrackWaveformActionStatus._
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentAuthorization, ContentPolicy, MonetizationModel}
import play.api.libs.json.{JsObject, JsValue, Json}

case class TrackPolicyApplicator(clientWhitelist: Set[Urn]) {
  val durationJsonPropertyName = "duration"
  val waveformUrlPropertyName = "waveform_url"

  def apply(session: UserSession, visitor: TracksVisitor, rules: Seq[ContentAuthorization], waveforms: List[TrackWaveformAction]): Option[JsValue] =
    visit(session, visitor, policiesByUrn(rules), waveformsByUrn(waveforms))

  private def visit(session: UserSession, visitor: TracksVisitor, authorizations: Map[Urn, ContentAuthorization], waveformActions: Map[Urn, TrackWaveformAction]): Option[JsValue] =
    visitor.apply {
      case (urn, track) =>
        val contentAuth = authorizations(urn)
        if (allowTrack(contentAuth, session))
          potentiallyReplaceWaveform(urn, track, contentAuth, waveformActions(urn)).map(potentiallyAddContentAuthorization(_, contentAuth, session))
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

  private def waveformsByUrn(waveforms: List[TrackWaveformAction]): Map[Urn, TrackWaveformAction] =
    waveforms.map(waveform => waveform.urn -> waveform).toMap

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

  private def potentiallyReplaceWaveform(urn: Urn, track: Track, contentAuth: ContentAuthorization, waveformAction: TrackWaveformAction): Option[Track] = {
    waveformAction.status match {
      case NeedsModification => replaceWaveformAndDuration(track, waveformAction)
      case DoesNotNeedModification => Some(track)
    }
  }

  private def replaceWaveformAndDuration(track: Track, waveformAction: TrackWaveformAction): Option[Track] = {
    val originalTrack = track.json.as[JsObject]
    waveformAction.url.flatMap(_.durationMs) match {
      case None => waveformAction.url.map(url => new Track(replaceWaveform(originalTrack, url.pngUrl.s)))
      case Some(duration) => waveformAction.url.map(url => new Track(replaceWaveformAndDuration(originalTrack, url.pngUrl.s, duration)))
    }
  }

  private def replaceWaveformAndDuration(originalTrack: JsObject, waveformUrl: String, duration: Int): JsObject =
    replaceWaveform(originalTrack, waveformUrl) ++ Json.obj(durationJsonPropertyName -> duration)

  private def replaceWaveform(originalTrack: JsObject, waveformUrl: String): JsObject =
    originalTrack ++ Json.obj(waveformUrlPropertyName -> waveformUrl)
}
