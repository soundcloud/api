package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.media.TrackWaveformUrl
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.policies.{MonetizationModel, ContentAuthorization, ContentPolicy, Reason}
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.{Url, Urn, UserSession}
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import com.soundcloud.publicApiStrangler.authorization.TrackWaveformActionStatus._

class ApplyTrackPoliciesSpec extends UnitSpecification with Fixtures {

  trait Context extends Scope {
    val session = mock[UserSession]
    val urns =
      tracksArray.as[List[JsObject]]
        .map(track => (track \ "id").as[Int])
        .map(id => Urn(s"soundcloud:tracks:$id"))
    def rules: List[ContentAuthorization]
    def waveformActions: List[TrackWaveformAction]

    lazy val authorizedTrackIds =
      extractIds(
        ApplyTrackPolicies(session, new TracksVisitor(tracksArray), rules, waveformActions).get
      )

    lazy val waveformsAndDurations =
      extractWaveformAndDuration(
        ApplyTrackPolicies(session, new TracksVisitor(tracksArray), rules, waveformActions).get
      )

    def extractIds(json: JsValue) =
      json.as[List[JsObject]].map(e => (e \ "id").as[Int])

    def extractWaveformAndDuration(json: JsValue) =
      json.as[List[JsObject]].map(e => ((e \ "waveform_url").as[String], (e \ "duration").as[Int]))
  }

  "applies the policies to all track objects" >> {

    trait EverythingAuthorized extends Context {
      def rules =
        for (urn <- urns) yield {
          new ContentAuthorization(urn, ContentPolicy.ALLOW, Reason.GEO, MonetizationModel.NOT_APPLICABLE)
        }

      def waveformActions =
        for (urn <- urns) yield {
          TrackWaveformAction(urn, DoesNotNeedModification, None)
        }

    }

    "all tracks authorized" in new EverythingAuthorized {
      authorizedTrackIds mustEqual extractIds(tracksArray)
      waveformsAndDurations ==== List(("https://w1.sndcdn.com/RhJ436DPf2Vx_m.png", 370348),
        ("https://w1.sndcdn.com/DWpqP6aFqglm_m.png",68127),("https://w1.sndcdn.com/sDWnMpZaIQ9Z_m.png", 326183))
    }

    trait PartiallyAuthorized extends Context {
      val authorized = urns.take(2)
      def rules =
        for (urn <- urns) yield {
          if (authorized.contains(urn))
            new ContentAuthorization(urn, ContentPolicy.ALLOW, Reason.GEO, MonetizationModel.NOT_APPLICABLE)
          else
            new ContentAuthorization(urn, ContentPolicy.BLOCK, Reason.GEO, MonetizationModel.NOT_APPLICABLE)
        }

      def waveformActions =
        for (urn <- urns) yield {
          TrackWaveformAction(urn, DoesNotNeedModification, None)
        }
    }

    "some authorized tracks" in new PartiallyAuthorized {
      authorizedTrackIds mustEqual authorized.map(_.getIdentifier.toInt)
      waveformsAndDurations ==== List(("https://w1.sndcdn.com/RhJ436DPf2Vx_m.png", 370348),
        ("https://w1.sndcdn.com/DWpqP6aFqglm_m.png",68127))
    }

    trait SomeAreSnip extends Context {
      val snip = List(urns(0), urns(2))

      def rules =
        for (urn <- urns) yield {
          if (snip.contains(urn))
            new ContentAuthorization(urn, ContentPolicy.SNIP, Reason.GEO, MonetizationModel.NOT_APPLICABLE)
          else
            new ContentAuthorization(urn, ContentPolicy.ALLOW, Reason.GEO, MonetizationModel.NOT_APPLICABLE)
        }

      val urlWithoutDuration = TrackWaveformUrl("uid1", Url("http://preview/jsonurl/noDuration"), Url("http://preview/pngnurl/noDuration"), "stream", None)
      val urlWithDuration = TrackWaveformUrl("uid3", Url("http://preview/jsonurl/withDuration"), Url("http://preview/pngnurl/withDuration"), "stream", Some(90000))

      def waveformActions = List(TrackWaveformAction(urns(0), NeedsModification, Some(urlWithoutDuration)),
        TrackWaveformAction(urns(1), DoesNotNeedModification, None),
        TrackWaveformAction(urns(2), NeedsModification, Some(urlWithDuration))
      )
    }


    "some tracks have content policy SNIP" in new SomeAreSnip {
      authorizedTrackIds ==== extractIds(tracksArray)
      waveformsAndDurations ==== List(("http://preview/pngnurl/noDuration", 370348),
        ("https://w1.sndcdn.com/DWpqP6aFqglm_m.png",68127),
        ("http://preview/pngnurl/withDuration", 90000))
    }

  }

  "stream tests" >> {
    trait StreamContext extends Context {

      override val urns = List(165855069, 168419205).map(id => Urn(s"soundcloud:tracks:$id"))

      override lazy val authorizedTrackIds =
        extractIds(
          ApplyTrackPolicies(session, new TracksVisitor(stream), rules, waveformActions).get
        )

      override def extractIds(json: JsValue) =
        (json \ "collection" \\ "track").toList.map(e => (e \ "id").asOpt[Int].getOrElse(-999))

    }

    trait PartiallyAuthorized extends StreamContext {
      val authorized = urns.take(1)
      override def rules =
        for (urn <- urns) yield {
          if (authorized.contains(urn))
            new ContentAuthorization(urn, ContentPolicy.ALLOW, Reason.GEO, MonetizationModel.NOT_APPLICABLE)
          else
            new ContentAuthorization(urn, ContentPolicy.BLOCK, Reason.GEO, MonetizationModel.NOT_APPLICABLE)
        }

      def waveformActions =
        for (urn <- urns) yield {
          TrackWaveformAction(urn, DoesNotNeedModification, None)
        }
    }

    "some authorized tracks" in new PartiallyAuthorized {
      authorizedTrackIds mustEqual Seq(165855069)
    }
  }



}
