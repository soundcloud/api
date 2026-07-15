package com.soundcloud.apipublic.client.fanmonetization

import com.soundcloud.apipublic.service.storefront.{Storefront, StorefrontNotEligible, StorefrontType, StorefrontUpsert}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.util.Future
import proto.soundcloud.fan_monetization.api.{
  BuyModule,
  FanMonetizationClientProtobuf,
  GetBuyModulesRequest,
  SaveBuyModuleRequest
}

trait FanMonetizationClient {

  /** Returns the buy modules (storefronts) attached to a track. */
  def getBuyModules(session: UserSession, trackUrn: Urn): Future[Outcome[Seq[BuyModule]]]

  /**
    * Creates or updates the buy module (storefront) of a track. When `existing` is present its id is
    * forwarded so fan-monetization performs an edit; its image_url is preserved because images are
    * not manageable through this API and fan-monetization overwrites absent fields on save.
    */
  def saveBuyModule(
      session: UserSession,
      trackUrn: Urn,
      upsert: StorefrontUpsert,
      existing: Option[BuyModule]
  ): Future[Outcome[Storefront]]
}

class FanMonetizationTwirpClient(client: FanMonetizationClientProtobuf) extends FanMonetizationClient {

  override def getBuyModules(session: UserSession, trackUrn: Urn): Future[Outcome[Seq[BuyModule]]] = {
    client
      .getBuyModules(
        GetBuyModulesRequest(
          userSession = Some(session.asProtoSession),
          urn = Some(trackUrn.toString)
        )
      )
      .map(response => response.modules.good)
      .handle {
        case TwinagleException(ErrorCode.NotFound, _, _, _) => Seq.empty[BuyModule].good
        case TwinagleException(ErrorCode.PermissionDenied, _, _, _) => CustomError(StorefrontNotEligible).bad
        case TwinagleException(ErrorCode.InvalidArgument, msg, _, _) => NotValid(msg).bad
      }
  }

  override def saveBuyModule(
      session: UserSession,
      trackUrn: Urn,
      upsert: StorefrontUpsert,
      existing: Option[BuyModule]
  ): Future[Outcome[Storefront]] = {
    client
      .saveBuyModule(
        SaveBuyModuleRequest(
          userSession = Some(session.asProtoSession),
          urn = Some(trackUrn.toString),
          title = upsert.title,
          `type` = upsert.storefrontType.proto,
          link = upsert.link,
          linkTitle = upsert.linkTitle,
          description = upsert.description,
          imageUrl = existing.flatMap(_.imageUrl),
          // price is a plain proto3 string, so "" is its only "absent" encoding; fan-monetization
          // stores it verbatim and unwraps absent link_title/description to "" as well, making
          // omit-to-clear behave identically for all three optional fields
          price = upsert.price.getOrElse(""),
          id = existing.map(_.id).filter(_.nonEmpty)
        )
      )
      .map { response =>
        Storefront(
          trackUrn = trackUrn,
          title = response.title,
          storefrontType = StorefrontType.fromProto(response.`type`),
          link = response.link,
          linkTitle = response.linkTitle.filter(_.nonEmpty),
          description = response.description.filter(_.nonEmpty),
          imageUrl = response.imageUrl.filter(_.nonEmpty),
          price = Option(response.price).filter(_.nonEmpty)
        ).good
      }
      .handle {
        case TwinagleException(ErrorCode.NotFound, msg, _, _) => NotFound(msg).bad
        case TwinagleException(ErrorCode.PermissionDenied, _, _, _) => CustomError(StorefrontNotEligible).bad
        case TwinagleException(ErrorCode.InvalidArgument, msg, _, _) => NotValid(msg).bad
      }
  }
}
