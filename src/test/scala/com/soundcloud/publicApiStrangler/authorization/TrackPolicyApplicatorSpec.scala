package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.TrackDurationActionStatus._
import com.soundcloud.publicApiStrangler.authorization.policies._
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import play.api.libs.json.{JsObject, JsValue}

class TrackPolicyApplicatorSpec extends UnitSpecification {

  trait Context extends Scope {
    val session = mock[UserSession]
    val urns =
      tracksArray.as[List[JsObject]]
        .map(track => (track \ "id").as[Int])
        .map(id => Urn("soundcloud", "tracks", id.toString))

    val whitelistedClientUrn = Urn("soundcloud", "applications", "1000")
    val nonWhitelistedClientUrn = Urn("soundcloud", "applications", "2000")
    val clientWhitelist = Set(whitelistedClientUrn)

    def rules: List[ContentAuthorization]

    def durationActions: List[TrackDurationAction]

    lazy val trackPolicy = TrackPolicyApplicator(clientWhitelist)
    lazy val tracksWithPoliciesApplied = trackPolicy(session, new TracksVisitor(tracksArray), rules, durationActions).get

    lazy val authorizedTrackIds = extractIds(tracksWithPoliciesApplied)

    lazy val durations = extractDuration(tracksWithPoliciesApplied)

    def extractIds(json: JsValue) =
      json.as[List[JsObject]].map(e => (e \ "id").as[Int])

    def extractDuration(json: JsValue) =
      json.as[List[JsObject]].map(e => (e \ "duration").as[Int])
  }

  "applies the policies to all track objects" >> {
    trait EverythingAuthorized extends Context {
      def rules =
        for (urn <- urns) yield {
          new ContentAuthorization(urn, ContentPolicy.ALLOW, Reason.GEO, MonetizationModel.NOT_APPLICABLE)
        }

      def durationActions =
        for (urn <- urns) yield {
          TrackDurationAction(urn, DoesNotNeedModification, None)
        }
    }

    "all tracks authorized" in new EverythingAuthorized {
      authorizedTrackIds mustEqual extractIds(tracksArray)
      durations ==== List(370348, 2000, 326183)
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
      val allowedTrackUrn = Urn("soundcloud", "tracks", "49438146")
      val monetizedTrackUrn = Urn("soundcloud", "tracks", "49437906")
      val blockedTrackUrn = Urn("soundcloud", "tracks", "48031525")

      def rules = List(
        new ContentAuthorization(allowedTrackUrn, ContentPolicy.ALLOW, Reason.GEO, MonetizationModel.NOT_APPLICABLE),
        new ContentAuthorization(monetizedTrackUrn, ContentPolicy.MONETIZE, Reason.GEO, MonetizationModel.SUB_HIGH_TIER),
        new ContentAuthorization(blockedTrackUrn, ContentPolicy.BLOCK, Reason.GEO, MonetizationModel.NOT_APPLICABLE)
      )

      def durationActions =
        for (urn <- urns) yield {
          TrackDurationAction(urn, DoesNotNeedModification, None)
        }
    }

    "returns allowed and monetized tracks for whitelisted clients" in new PartiallyAuthorized {
      session.getAgent returns whitelistedClientUrn

      authorizedTrackIds mustEqual List(allowedTrackUrn, monetizedTrackUrn).map(_.getIdentifier.toInt)
      durations ==== List(370348, 2000)
    }

    "returns only allowed tracks for non-whitelisted clients" in new PartiallyAuthorized {
      session.getAgent returns nonWhitelistedClientUrn

      authorizedTrackIds mustEqual List(allowedTrackUrn).map(_.getIdentifier.toInt)
      durations ==== List(370348)
    }

    trait AdSupported extends Context {
      val allowedTrackUrn = Urn("soundcloud", "tracks", "49438146")
      val monetizedHighTierTrackUrn = Urn("soundcloud", "tracks", "49437906")
      val monetizedAdSupportedTrackUrn = Urn("soundcloud", "tracks", "48031525")

      def rules = List(
        new ContentAuthorization(allowedTrackUrn, ContentPolicy.ALLOW, Reason.GEO, MonetizationModel.NOT_APPLICABLE),
        new ContentAuthorization(monetizedHighTierTrackUrn, ContentPolicy.MONETIZE, Reason.GEO, MonetizationModel.SUB_HIGH_TIER),
        new ContentAuthorization(monetizedAdSupportedTrackUrn, ContentPolicy.MONETIZE, Reason.GEO, MonetizationModel.AD_SUPPORTED)
      )

      def durationActions =
        for (urn <- urns) yield {
          TrackDurationAction(urn, DoesNotNeedModification, None)
        }
    }

    "returns allowed, tiered and ad-supported tracks for whitelisted clients" in new AdSupported {
      session.getAgent returns whitelistedClientUrn

      authorizedTrackIds mustEqual List(allowedTrackUrn, monetizedHighTierTrackUrn, monetizedAdSupportedTrackUrn).map(_.getIdentifier.toInt)
    }

    "returns only allowed and ad-supported tracks for non-whitelisted clients" in new AdSupported {
      session.getAgent returns nonWhitelistedClientUrn

      authorizedTrackIds mustEqual List(allowedTrackUrn, monetizedAdSupportedTrackUrn).map(_.getIdentifier.toInt)
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

      def durationActions = List(TrackDurationAction(urns(0), NeedsModification, None),
        TrackDurationAction(urns(1), DoesNotNeedModification, None),
        TrackDurationAction(urns(2), NeedsModification, Some(90000))
      )
    }

    "some tracks have content policy SNIP" in new SomeAreSnip {
      authorizedTrackIds ==== extractIds(tracksArray)
      durations ==== List(370348, 2000, 90000)
    }
  }

  "stream tests" >> {
    trait StreamContext extends Context {
      override val urns = List(165855069, 168419205).map(id => new Urn(s"soundcloud:tracks:$id"))

      override lazy val authorizedTrackIds =
        extractIds(
          TrackPolicyApplicator(clientWhitelist)(session, new TracksVisitor(stream), rules, durationActions).get
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

      def durationActions =
        for (urn <- urns) yield {
          TrackDurationAction(urn, DoesNotNeedModification, None)
        }
    }

    "some authorized tracks" in new PartiallyAuthorized {
      authorizedTrackIds mustEqual Seq(165855069)
    }
  }
}
