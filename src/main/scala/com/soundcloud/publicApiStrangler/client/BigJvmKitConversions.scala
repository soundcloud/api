package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.module.http.client.{IntParam, ListParam, LongParam, Param, Params, StringParam, UrnParam, UrnsParam}
import com.soundcloud.jvmkit.module.util.session.{AnonymousUserSession, LoggedInUserSession, UserSession}
import com.soundcloud.jvmkit.module.util.{Geo, Path, Url, Urn}
import com.soundcloud.jvmkit.policies.{ContentAuthorization => BigJvmKitContentAuthorization, ContentPolicy => BigJvmKitContentPolicy, ContentRestriction => BigJvmKitContentRestriction, MonetizationModel => BigJvmKitMonetizationModel, Reason => BigJvmKitReason}
import com.soundcloud.jvmkit.{Country => BigJvmKitCountry, Geo => BigJvmKitGeo, Urn => BigJvmKitUrn, UserSession => BigJvmKitUserSession, UserSessionBuilder => BigJvmKitUserSessionBuilder}
import com.soundcloud.publicApiStrangler.policies._
import com.soundcloud.publicApiStrangler.representation.Country
import com.soundcloud.scalakit.finagle.jsonservice.{IntParam => BigJvmKitIntParam, ListParam => BigJvmKitListParam, LongParam => BigJvmKitLongParam, Param => BigJvmKitParam, Params => BigJvmKitParams, StringParam => BigJvmKitStringParam, UrnParam => BigJvmKitUrnParam, UrnsParam => BigJvmKitUrnsParam}
import com.soundcloud.scalakit.{Path => BigJvmKitPath, Url => BigJvmKitUrl}

import scala.collection.JavaConversions._

object BigJvmKitConversions {
  implicit def toBigJvmKitUserSession(userSession: UserSession): BigJvmKitUserSession = {
    builderWithPotentialAgent(builderWithPotentialUser(new BigJvmKitUserSessionBuilder(), userSession), userSession)
      .setScopes(userSession.getScopes)
      .setFeatures(userSession.getFeatures)
      .setGeo(userSession.getGeo)
      .build
  }

  private def builderWithPotentialUser(builder: BigJvmKitUserSessionBuilder, userSession: UserSession): BigJvmKitUserSessionBuilder = {
    // Use the type of the session to decide whether a user is present.
    userSession match {
      case x: LoggedInUserSession => builder.setUser(userSession.getUser)
      case x: AnonymousUserSession => builder
    }
  }

  private def builderWithPotentialAgent(builder: BigJvmKitUserSessionBuilder, userSession: UserSession): BigJvmKitUserSessionBuilder = {
    // Extract the agent to decide whether it is present.
    if (userSession.getAgent == null) {
      builder
    } else {
      builder.setAgent(userSession.getAgent)
    }
  }

  implicit def toBigJvmKitUrl(url: Url): BigJvmKitUrl = {
    BigJvmKitUrl(url.toString())
  }

  implicit def toBigJvmKitUrn(urn: Urn): BigJvmKitUrn = {
    BigJvmKitUrn(urn.toString())
  }

  implicit def toBigJvmKitUrnList(urns: List[Urn]): List[BigJvmKitUrn] = {
    urns.map(toBigJvmKitUrn)
  }

  implicit def toBigJvmKitUrnSet(urns: Set[Urn]): Set[BigJvmKitUrn] = {
    urns.map(toBigJvmKitUrn)
  }

  implicit def toBigJvmKitUrnSeq(urns: Seq[Urn]): Seq[BigJvmKitUrn] = {
    urns.map(toBigJvmKitUrn)
  }

  implicit def toBigJvmKitGeo(geo: Geo): BigJvmKitGeo = {
    new BigJvmKitGeo(geo.getCountryCode, geo.getCity, geo.getRegion)
  }


  implicit def toBigJvmKitPath(path: Path): BigJvmKitPath = {
    new BigJvmKitPath(path.s)
  }

  implicit def toBigJvmKitParam(param: Param): BigJvmKitParam = {
    param match {
      case StringParam(string: String) => BigJvmKitStringParam(string)
      case ListParam(value: List[String]) => BigJvmKitListParam(value)
      case IntParam(int: Int) => BigJvmKitIntParam(int)
      case LongParam(long: Long) => BigJvmKitLongParam(long)
      case UrnParam(urn: Urn) => BigJvmKitUrnParam(urn)
      case UrnsParam(urns: Iterable[Urn]) => BigJvmKitUrnsParam(urns.map(toBigJvmKitUrn))
    }
  }

  implicit def toBigJvmKitParams(params: Params): BigJvmKitParams = {
    BigJvmKitParams(params.mapValues(toBigJvmKitParam).toSeq: _*)
  }

  implicit def toBigJvmKitContentAuthorization(ca: ContentAuthorization): BigJvmKitContentAuthorization = {
    new BigJvmKitContentAuthorization(ca.urn, ca.policy, ca.reason, ca.contentRestrictions.map(toBigJvmKitContentRestriction), ca.monetizationModel)
  }

  implicit def toBigJvmKitContentPolicy(policy: ContentPolicy): BigJvmKitContentPolicy = {
    policy match {
      case ContentPolicy.ALLOW => BigJvmKitContentPolicy.ALLOW
      case ContentPolicy.BLOCK => BigJvmKitContentPolicy.BLOCK
      case ContentPolicy.MONETIZE => BigJvmKitContentPolicy.MONETIZE
      case ContentPolicy.SNIP => BigJvmKitContentPolicy.SNIP
    }
  }

  implicit def toBigJvmKitMonetizationModel(policy: MonetizationModel): BigJvmKitMonetizationModel = {
    policy match {
      case MonetizationModel.NOT_APPLICABLE => BigJvmKitMonetizationModel.NOT_APPLICABLE
      case MonetizationModel.AD_SUPPORTED => BigJvmKitMonetizationModel.AD_SUPPORTED
      case MonetizationModel.SUB_MID_TIER => BigJvmKitMonetizationModel.SUB_MID_TIER
      case MonetizationModel.SUB_HIGH_TIER => BigJvmKitMonetizationModel.SUB_HIGH_TIER
      case MonetizationModel.BLACKBOX => BigJvmKitMonetizationModel.BLACKBOX
    }
  }

  implicit def toBigJvmKitReason(policy: Reason): BigJvmKitReason = {
    policy match {
      case Reason.CLIENT_APPLICATION => BigJvmKitReason.CLIENT_APPLICATION
      case Reason.DEFAULT => BigJvmKitReason.DEFAULT
      case Reason.GEO => BigJvmKitReason.GEO
      case Reason.NOT_SUPPORTED => BigJvmKitReason.NOT_SUPPORTED
      case Reason.UNKNOWN => BigJvmKitReason.UNKNOWN
      case Reason.RIGHTSHOLDER_RESTRICTED => BigJvmKitReason.RIGHTSHOLDER_RESTRICTED
      case Reason.USER => BigJvmKitReason.USER
    }
  }

  implicit def toBigJvmKitContentRestriction(restriction: ContentRestriction): BigJvmKitContentRestriction = {
    restriction match {
      case ContentRestriction.ENCRYPTED_STREAM_ONLY => BigJvmKitContentRestriction.ENCRYPTED_STREAM_ONLY
      case ContentRestriction.NO_OFFLINE_SYNC => BigJvmKitContentRestriction.NO_OFFLINE_SYNC
    }
  }

  implicit def toBigJvmKitCountry(country: Country): BigJvmKitCountry = {
    new BigJvmKitCountry(country.alpha2Code, country.isOfficiallyAssigned, country.isTransitionallyReserved)
  }
}
