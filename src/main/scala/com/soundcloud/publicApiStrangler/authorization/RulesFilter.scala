package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.HasUrn
import com.soundcloud.publicApiStrangler.authorization.policies.{
  ContentAuthorization,
  ContentPolicy,
  MonetizationModel,
  Reason
}
import com.twitter.util.Future
import play.api.libs.json.JsValue

import scala.collection.mutable.ArrayBuffer

case class RulesFilterResult(
    allowedContent: Seq[(JsValue, ContentAuthorization)],
    allowedUrns: Seq[Urn],
    filteredUrns: Seq[Urn]
)

/**
  * Filters contents accordingly to policies.
  *
  * @param allowedPolicies The whitelisted policies, anything but these will be rejected.
  */
class RulesFilter(allowedPolicies: ContentPolicy*) {
  val logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  def filter(
      rules: Future[Seq[ContentAuthorization]],
      content: Seq[JsValue]
  ): Future[Seq[(JsValue, ContentAuthorization)]] =
    rules.map(r => filter(r, content))

  /**
    * Inpects a sequence of [[JsValue]] and filters out those which don't follow the [[allowedPolicies]].
    *
    * This checks for both metadata and playback policies.
    *
    * @param rules   The rules to apply
    * @param content List of [[JsValue]] representing each content item (e.g. tracks)
    * @return The members of content matching the [[allowedPolicies]] and their respective rules.
    */
  def filter(rules: Seq[ContentAuthorization], content: Seq[JsValue]): Seq[(JsValue, ContentAuthorization)] = {
    val jsonAndRules = content.map(json => (json, contentAuthorizationFor(rules, json)))

    jsonAndRules.filter {
      case (json, rs) =>
        val urn = HasUrn.unapply(json)
        if (permits(rs.getPolicy)) {
          logger.debug(s"[${urn.map(_.toString)}] with rules [$rules] was kept")
          true
        } else {
          logger.info(s"[${urn.map(_.toString)}] with rules [$rules] was removed")
          false
        }
    }
  }

  /**
    * Inpects a sequence of [[JsValue]] and filters out those which don't follow the [[allowedPolicies]].
    * This checks for both metadata and playback policies.
    *
    * @param rules   The rules to apply
    * @param content List of [[JsValue]] representing each content item (e.g. tracks)
    * @return The members of content matching the [[allowedPolicies]], their rules AND the allowed and filtered urns.
    */
  def filterAndGetDetailedResult(rules: Seq[ContentAuthorization], content: Seq[JsValue]): RulesFilterResult = {
    val jsonAndRules = content.map(json => (json, contentAuthorizationFor(rules, json)))
    val keptUrns, removedUrns = new ArrayBuffer[Option[Urn]]
    val permitted = jsonAndRules.filter {
      case (json, rs) =>
        val urn = HasUrn.unapply(json)
        if (permits(rs.getPolicy)) {
          keptUrns += urn
          true
        } else {
          removedUrns += urn
          false
        }
    }
    RulesFilterResult(permitted, keptUrns.flatten.toSeq, removedUrns.flatten.toSeq)
  }

  /**
    * Indicates if a piece of content with the given [[ContentPolicy]] should be made available for this application.
    */
  def permits(policy: ContentPolicy) = allowedPolicies.contains(policy)

  /**
    * Finds the rule for this [[Urn]].
    *
    * @return The [[ContentAuthorization]], if found or the [[defaultRules( )]] if not.
    */
  def contentAuthorizationFor(rules: Seq[ContentAuthorization], content: JsValue): ContentAuthorization =
    content match {
      case json @ HasUrn(urn) => rules.find(_.getUrn == urn).getOrElse(defaultRules(urn))
      case jsonWithoutUrn =>
        logger.error(s"Could not authorize, no URN found in : [$jsonWithoutUrn], blocking")
        new ContentAuthorization(null, ContentPolicy.BLOCK, Reason.UNKNOWN, MonetizationModel.NOT_APPLICABLE)
    }

  private def defaultRules(urn: Urn) = {
    new ContentAuthorization(urn, ContentPolicy.ALLOW, Reason.UNKNOWN, MonetizationModel.NOT_APPLICABLE)
  }
}
