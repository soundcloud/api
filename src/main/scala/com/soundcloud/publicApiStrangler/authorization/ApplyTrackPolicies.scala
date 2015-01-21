package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.UserSession
import play.api.libs.json.{JsNumber, JsString, JsObject, JsValue}
import com.soundcloud.jvmkit.policies.ContentAuthorization
import com.soundcloud.jvmkit.policies.ContentPolicy
import com.soundcloud.publicApiStrangler.authorization.TrackWaveformActionStatus._

object ApplyTrackPolicies {

  val durationJsonPropertyName = "duration"
  val waveformUrlPropertyName = "waveform_url"

  def apply(session: UserSession, visitor: TracksVisitor, rules: Seq[ContentAuthorization], waveforms: List[TrackWaveformAction]) =
    visit(session, visitor, policiesByUrn(rules), waveformsByUrn(waveforms))

  private def visit(session: UserSession, visitor: TracksVisitor, policies: Map[Urn, ContentPolicy], waveformActions: Map[Urn, TrackWaveformAction]) =
    visitor.apply {
      case (urn, track) =>
        val policy = policies(urn)
        if (policy == ContentPolicy.BLOCK)
          None
        else
          potentiallyReplaceWaveform(urn, track, policy, waveformActions(urn))
    }

  private def policiesByUrn(rules: Seq[ContentAuthorization]) =
    rules.map(rule => rule.getUrn -> rule.getPolicy).toMap

  private def waveformsByUrn(waveforms: List[TrackWaveformAction]) =
    waveforms.map(waveform => waveform.urn -> waveform).toMap

  private def potentiallyReplaceWaveform(urn:Urn, track:Track, policy:ContentPolicy, waveformAction: TrackWaveformAction) : Option[JsValue] = {
    waveformAction.status match {
      case NeedsModification => replaceWaveformAndDuration(track, waveformAction, policy)
      case DoesNotNeedModification => Some(track.withPolicies(policy))
    }
  }

  private def replaceWaveformAndDuration(track:Track, waveformAction:TrackWaveformAction, policy:ContentPolicy) : Option[JsValue] = {
    val originalTrack = track.json.as[JsObject]
    waveformAction.url.flatMap(_.durationMs) match {
      case None => waveformAction.url.map(url => new Track(replaceWaveform(originalTrack, url.pngUrl.s)).withPolicies(policy))
      case Some(duration) => waveformAction.url.map(url => new Track(replaceWaveformAndDuration(originalTrack, url.pngUrl.s, duration)).withPolicies(policy))
    }
  }

  private def replaceWaveformAndDuration(originalTrack:JsObject, waveformUrl:String, duration:Int) =
    replaceWaveform(originalTrack, waveformUrl) ++ (JsObject(Seq(durationJsonPropertyName -> JsNumber(duration))))

  private def replaceWaveform(originalTrack:JsObject, waveformUrl:String) =
    originalTrack ++ (JsObject(Seq(waveformUrlPropertyName -> JsString(waveformUrl))))

}
