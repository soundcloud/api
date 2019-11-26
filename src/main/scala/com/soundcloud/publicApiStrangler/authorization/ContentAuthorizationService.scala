package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.json.UrnFormat._
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.authorization.policies._
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json.{JsValue, Json}

/**
  * Finds the [[ContentAuthorization]] applicable for a given piece of content for a particular [[UserSession]]
  *
  * @param authsy    An client to [[https://github.com/soundcloud/authsy Authsy]]
  * @param batchSize number of urns to put into one batch fetch.
  *                  If the service is asked for more than that, there will be several requests performed.
  *                  The reason we're doing this -- is to prevent request urls to become too long (which causes an exception).
  *                  Default value is chosen empirically.
  */
class ContentAuthorizationService(authsy: JsonClient, batchSize: Int = ContentAuthorizationService.defaultBatchSize) {
  val logger = SoundCloudLoggerFactory.getLogger(getClass)

  val tracksFetchpath: Path = Path("/tracks") / "authorization"

  /**
    * Returns a list of authorization rules applied to the URNs.
    *
    * @param session             The [[UserSession]] for which authorization rules should be checked.
    * @param resources           The [[Urn]]s of the resources we want to check.
    * @param subscriptionCountry the subscription country of a high/mid-tier subscriber, or geo-country of a free-tier subscriber.
    * @return A list containing all [[ContentAuthorization]] applicable to the content.
    */
  def findRulesApplicableTo(
      session: UserSession,
      resources: Seq[Urn],
      subscriptionCountry: Option[String]
  ): Future[Seq[ContentAuthorization]] =
    resources match {
      case Seq() => Future(Seq())
      case _ => fetch(session, resources, subscriptionCountry)
    }

  private def fetch(session: UserSession, resources: Seq[Urn], subscriptionCountry: Option[String]) = {
    val authsySession =
      subscriptionCountry.map(session.addHeader(UserSession.CONSUMER_SUBSCRIPTION_COUNTRY, _)).getOrElse(session)
    Future
      .collect(resources.grouped(batchSize).toSeq.map { urns =>
        authsy.getWithSession(authsySession, tracksFetchpath, Params("urns" -> urns), Headers.empty).map {
          case resp if resp.status == Status.Ok => Json.parse(resp.contentString).as[Seq[JsValue]].map(jsonToRules)
        }
      })
      .map(_.flatten)
  }

  private def jsonToRules(value: JsValue): ContentAuthorization = {
    val resource = (value \ "resource").as[Urn]
    val playback = ContentPolicy.from((value \ "playback" \ "policy").as[String])
    val metadata = ContentPolicy.from((value \ "metadata" \ "policy").as[String])

    val reason = (value \ "reason")
      .asOpt[String]
      .map(_.toUpperCase)
      .map(Reason.from)
      .getOrElse(Reason.UNKNOWN)

    val monetizationModel = (value \ "monetization_model")
      .asOpt[String]
      .map(_.toUpperCase)
      .map(MonetizationModel.from)
      .getOrElse(MonetizationModel.NOT_APPLICABLE)

    val restrictions: Set[ContentRestriction] = (value \ "restrictions").asOpt[List[String]] match {
      case Some(r) => ContentRestriction.from(r).toSet
      case None => Set.empty
    }

    if (playback != metadata) {
      logger.warn(
        s"[${resource}}] has diverging metadata [$metadata] and playback [$playback] policies, " +
          s"given metadata is deprecated will use [$playback]"
      )
    }

    new ContentAuthorization(resource, playback, reason, restrictions, monetizationModel)
  }
}

object ContentAuthorizationService {
  private val defaultBatchSize = 65
}
