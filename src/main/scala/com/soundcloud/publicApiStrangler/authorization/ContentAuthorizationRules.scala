package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Urn, UserTier}
import com.soundcloud.jvmkit.policies.{ContentPolicy => BigJvmKitContentPolicy, ContentRestriction => BigJvmKitContentRestriction, MonetizationModel => BigJvmKitMonetizationModel, Reason => BigJvmKitReason, ContentAuthorization => BigJvmKitContentAuthorization}
import com.twitter.util.Future
import com.soundcloud.publicApiStrangler.client.BigJvmKitConversions._
import com.soundcloud.publicApiStrangler.policies._
import com.soundcloud.jvmkit.ModuleConversions._
import scala.collection.JavaConversions._

class ContentAuthorizationRules(contentAuth: ContentAuthorizationService, subscriptions: SubscriptionsService) {

  def fetchRules(session: UserSession, urns: Seq[Urn]): Future[Seq[ContentAuthorization]] =
    consumerSubsCountry(session).flatMap(country => contentAuth.findRulesApplicableTo(session, urns, country).map(toModulesContentAuthorizations))

  private def consumerSubsCountry(session: UserSession): Future[Option[String]] =
    if (session.getTier != UserTier.FREE) {
      subscriptions.getActiveSubscriptionCountry(session).map(Option(_))
    } else {
      Future.None
    }

  private def toModulesContentAuthorizations(ca: Seq[BigJvmKitContentAuthorization]): Seq[ContentAuthorization] = {
    ca.map(toModulesContentAuthorization)
  }

  private def toModulesContentAuthorization(ca: BigJvmKitContentAuthorization): ContentAuthorization = {
    new ContentAuthorization(toModuleUrn(ca.getUrn), toContentPolicy(ca.getPolicy), toReason(ca.getReason),
      ca.getContentRestrictions.map(toContentRestriction).toSet, toMonetizationModel(ca.getMonetizationModel))
  }

  def toContentPolicy(policy: BigJvmKitContentPolicy): ContentPolicy = {
    policy match {
      case BigJvmKitContentPolicy.ALLOW => ContentPolicy.ALLOW
      case BigJvmKitContentPolicy.BLOCK => ContentPolicy.BLOCK
      case BigJvmKitContentPolicy.MONETIZE => ContentPolicy.MONETIZE
      case BigJvmKitContentPolicy.SNIP => ContentPolicy.SNIP
    }
  }

  def toMonetizationModel(policy: BigJvmKitMonetizationModel): MonetizationModel = {
    policy match {
      case BigJvmKitMonetizationModel.NOT_APPLICABLE => MonetizationModel.NOT_APPLICABLE
      case BigJvmKitMonetizationModel.AD_SUPPORTED => MonetizationModel.AD_SUPPORTED
      case BigJvmKitMonetizationModel.SUB_MID_TIER => MonetizationModel.SUB_MID_TIER
      case BigJvmKitMonetizationModel.SUB_HIGH_TIER => MonetizationModel.SUB_HIGH_TIER
      case BigJvmKitMonetizationModel.BLACKBOX => MonetizationModel.BLACKBOX
    }
  }

  def toReason(policy: BigJvmKitReason): Reason = {
    policy match {
      case BigJvmKitReason.CLIENT_APPLICATION => Reason.CLIENT_APPLICATION
      case BigJvmKitReason.DEFAULT => Reason.DEFAULT
      case BigJvmKitReason.GEO => Reason.GEO
      case BigJvmKitReason.NOT_SUPPORTED => Reason.NOT_SUPPORTED
      case BigJvmKitReason.UNKNOWN => Reason.UNKNOWN
      case BigJvmKitReason.RIGHTSHOLDER_RESTRICTED => Reason.RIGHTSHOLDER_RESTRICTED
      case BigJvmKitReason.USER => Reason.USER
    }
  }

  def toContentRestriction(restriction: BigJvmKitContentRestriction): ContentRestriction = {
    restriction match {
      case BigJvmKitContentRestriction.ENCRYPTED_STREAM_ONLY => ContentRestriction.ENCRYPTED_STREAM_ONLY
      case BigJvmKitContentRestriction.NO_OFFLINE_SYNC => ContentRestriction.NO_OFFLINE_SYNC
    }
  }
}
