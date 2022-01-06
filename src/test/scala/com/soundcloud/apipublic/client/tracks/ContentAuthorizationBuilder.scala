package com.soundcloud.apipublic.client.tracks

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.authorization.policies._

import scala.util.Random

class ContentAuthorizationBuilder {
  private val random = Random

  private var urn: Urn = Urn("soundcloud", "tracks", s"${random.nextLong()}")
  private var policy: ContentPolicy = ContentPolicy.ALLOW
  private var reason: Reason = Reason.UNKNOWN
  private var restriction: ContentRestriction = null
  private var monetizationModel: MonetizationModel = MonetizationModel.NOT_APPLICABLE

  def setUrn(value: Urn): ContentAuthorizationBuilder = { urn = value; this }
  def setPolicy(value: ContentPolicy): ContentAuthorizationBuilder = { policy = value; this }
  def setReason(value: Reason): ContentAuthorizationBuilder = { reason = value; this }
  def setRestriction(value: ContentRestriction): ContentAuthorizationBuilder = { restriction = value; this }
  def setMonetizationModel(value: MonetizationModel): ContentAuthorizationBuilder = { monetizationModel = value; this }

  def build: ContentAuthorization = restriction match {
    case null => new ContentAuthorization(urn, policy, reason, monetizationModel)
    case _ => new ContentAuthorization(urn, policy, reason, restriction, monetizationModel)
  }
}
