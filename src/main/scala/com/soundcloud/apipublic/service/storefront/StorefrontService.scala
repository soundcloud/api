package com.soundcloud.apipublic.service.storefront

import com.google.protobuf.field_mask.FieldMask
import com.soundcloud.apipublic.client.GatekeeperClient
import com.soundcloud.apipublic.client.fanmonetization.FanMonetizationClient
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.util.Future
import proto.soundcloud.tracks.api.{
  GetVisibleTracksRequest,
  Track,
  TrackMetadataService,
  TrackRequest => TwirpTrackRequest
}
import scalapb.FieldMaskUtil

class StorefrontService(
    fanMonetizationClient: FanMonetizationClient,
    gatekeeperClient: GatekeeperClient,
    trackMetadataClient: TrackMetadataService
) {
  import StorefrontService._

  /**
    * Creates or updates the storefront (buy module) of a track.
    *
    * Mirrors the checks web performs before saving: the creator must hold the
    * `external_purchase_options` gatekeeper feature (paid creator plans), and — unlike web, which
    * relies on its UI only opening the editor on own tracks — the track must be owned by the
    * authenticated user, because fan-monetization does not enforce ownership on SaveBuyModule.
    */
  def upsertStorefront(
      session: UserSession,
      trackUrn: Urn,
      upsert: StorefrontUpsert
  ): Future[Outcome[Storefront]] = {
    Future
      .join(
        verifyEligibility(session),
        verifyTrackOwnership(session, trackUrn)
      )
      .flatMap {
        case (Good(_), Good(_)) => saveStorefront(session, trackUrn, upsert)
        // when both checks fail, the track outcome wins: a missing track is a 404,
        // not a hint about the caller's subscription
        case (_, Bad(outcome)) => Future.value(outcome.bad)
        case (Bad(outcome), _) => Future.value(outcome.bad)
      }
  }

  private def saveStorefront(
      session: UserSession,
      trackUrn: Urn,
      upsert: StorefrontUpsert
  ): Future[Outcome[Storefront]] = {
    fanMonetizationClient.getBuyModules(session, trackUrn).flatMap {
      // fan-monetization stores the buy module keyed by track urn, so GetBuyModules returns at
      // most one module and headOption is the whole list
      case Good(modules) => fanMonetizationClient.saveBuyModule(session, trackUrn, upsert, modules.headOption)
      case Bad(outcome) => Future.value(outcome.bad)
    }
  }

  private def verifyEligibility(session: UserSession): Future[Outcome[Unit]] = {
    gatekeeperClient.isFeatureAccessible(session, ExternalPurchaseOptionsFeature).map {
      case true => ().good
      case false => CustomError(StorefrontNotEligible).bad
    }
  }

  private def verifyTrackOwnership(session: UserSession, trackUrn: Urn): Future[Outcome[Unit]] = {
    val request = GetVisibleTracksRequest(
      trackRequests = List(TwirpTrackRequest(trackUrn.toString)),
      trackFieldMask = Some(MetadataFieldMask),
      userSession = Some(session.asProtoSession)
    )

    trackMetadataClient.getVisibleTracks(request).map { response =>
      response.tracks.headOption.flatMap(_.metadata) match {
        case None => NotFound("Track not found").bad
        case Some(metadata) if Urn.parse(metadata.userUrn).toOption.contains(session.getUser) => ().good
        case Some(_) => NotAllowed("Track is not owned by the authenticated user").bad
      }
    }
  }
}

object StorefrontService {
  val ExternalPurchaseOptionsFeature = "external_purchase_options"

  private val MetadataFieldMask: FieldMask =
    FieldMaskUtil.selectFieldNumbers[Track](Set(Track.METADATA_FIELD_NUMBER))
}
