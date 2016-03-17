package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.media.TrackWaveformUrl
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.policies.{ContentAuthorization, ContentPolicy, MonetizationModel, Reason}
import com.soundcloud.publicApiStrangler.authorization.TrackWaveformActionStatus._
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.{Url, Urn, UserSession}
import play.api.libs.json.{JsObject, JsValue}

class TrackPolicyApplicatorSpec extends UnitSpecification with Fixtures {

  trait Context extends Scope {
    val session = mock[UserSession]
    val urns =
      tracksArray.as[List[JsObject]]
        .map(track => (track \ "id").as[Int])
        .map(id => Urn(s"soundcloud:tracks:$id"))

    val whitelistedClientUrn = Urn("soundcloud:applications:1000")
    val nonWhitelistedClientUrn = Urn("soundcloud:applications:2000")
    val clientWhitelist = Set(whitelistedClientUrn)

    def rules: List[ContentAuthorization]

    def waveformActions: List[TrackWaveformAction]

    lazy val trackPolicy = TrackPolicyApplicator(clientWhitelist)
    lazy val tracksWithPoliciesApplied = trackPolicy(session, new TracksVisitor(tracksArray), rules, waveformActions).get

    lazy val authorizedTrackIds = extractIds(tracksWithPoliciesApplied)

    lazy val waveformsAndDurations = extractWaveformAndDuration(tracksWithPoliciesApplied)

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
        ("https://w1.sndcdn.com/DWpqP6aFqglm_m.png", 68127), ("https://w1.sndcdn.com/sDWnMpZaIQ9Z_m.png", 326183))
    }

    "all tracks has 'policy' and 'monetization_model' for whitelisted user agent" in new EverythingAuthorized {
      session.getAgent returns whitelistedClientUrn

      tracksWithPoliciesApplied.as[List[JsObject]].filter(e => !e.keys.contains("policy") || !e.keys.contains("monetization_model")) must beEmpty
    }

    "no tracks has 'policy' and 'monetization_model' for non-whitelisted user agent" in new EverythingAuthorized {
      session.getAgent returns nonWhitelistedClientUrn

      tracksWithPoliciesApplied.as[List[JsObject]].filter(e => e.keys.contains("policy") || e.keys.contains("monetization_model")) must beEmpty
    }

    trait PartiallyAuthorized extends Context {
      val allowedTrackUrn = Urn("soundcloud:tracks:49438146")
      val monetizedTrackUrn = Urn("soundcloud:tracks:49437906")
      val blockedTrackUrn = Urn("soundcloud:tracks:48031525")

      def rules = List(
        new ContentAuthorization(allowedTrackUrn, ContentPolicy.ALLOW, Reason.GEO, MonetizationModel.NOT_APPLICABLE),
        new ContentAuthorization(monetizedTrackUrn, ContentPolicy.MONETIZE, Reason.GEO, MonetizationModel.SUB_HIGH_TIER),
        new ContentAuthorization(blockedTrackUrn, ContentPolicy.BLOCK, Reason.GEO, MonetizationModel.NOT_APPLICABLE)
      )

      def waveformActions =
        for (urn <- urns) yield {
          TrackWaveformAction(urn, DoesNotNeedModification, None)
        }
    }

    "returns allowed and monietized tracks for whitelisted clients" in new PartiallyAuthorized {
      session.getAgent returns whitelistedClientUrn

      authorizedTrackIds mustEqual List(allowedTrackUrn, monetizedTrackUrn).map(_.getIdentifier.toInt)
      waveformsAndDurations ==== List(
        ("https://w1.sndcdn.com/RhJ436DPf2Vx_m.png", 370348),
        ("https://w1.sndcdn.com/DWpqP6aFqglm_m.png", 68127))
    }

    "returns only allowed tracks for non-whitelisted clients" in new PartiallyAuthorized {
      session.getAgent returns nonWhitelistedClientUrn

      authorizedTrackIds mustEqual List(allowedTrackUrn).map(_.getIdentifier.toInt)
      waveformsAndDurations ==== List(("https://w1.sndcdn.com/RhJ436DPf2Vx_m.png", 370348))
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
      waveformsAndDurations ==== List(
        ("http://preview/pngnurl/noDuration", 370348),
        ("https://w1.sndcdn.com/DWpqP6aFqglm_m.png", 68127),
        ("http://preview/pngnurl/withDuration", 90000))
    }
  }

  "stream tests" >> {
    trait StreamContext extends Context {
      override val urns = List(165855069, 168419205).map(id => Urn(s"soundcloud:tracks:$id"))

      override lazy val authorizedTrackIds =
        extractIds(
          TrackPolicyApplicator(clientWhitelist)(session, new TracksVisitor(stream), rules, waveformActions).get
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
