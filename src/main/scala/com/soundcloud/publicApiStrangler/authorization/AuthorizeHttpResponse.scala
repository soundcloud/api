package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, HandlerRouter, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.TrackDurationActionStatus._
import com.twitter.finagle.http.Response
import com.twitter.util.Future
import play.api.libs.json.Json

import scala.collection.mutable.ListBuffer

class AuthorizeHttpResponse(
    contentAuthorization: ContentAuthorizationRules,
    userAuthentication: UserAuthentication,
    trackPolicyApplicator: TrackPolicyApplicator,
    telemetry: Telemetry,
    router: HandlerRouter
) {
  private val httpResponseAuthCounter = telemetry.counter(
    "http_response_auth_total",
    "Number of response track authorizations per application and endpoint",
    "appid",
    "path",
    "method"
  )

  def apply(request: HandlerRequest, originalResponse: Response): Future[Response] =
    CollectTrackUrns(originalResponse.contentString) match {
      case Some((visitor, urns)) =>
        authorize(request, visitor, urns)
      case None =>
        Future.value(originalResponse)
    }

  private def authorize(
      request: HandlerRequest,
      visitor: TracksVisitor,
      urns: Seq[Urn]
  ): Future[Response] =
    userAuthentication.withUserSession(request) { session =>
      val clientAppId = Option(session.getAgent).map(_.identifier).getOrElse("unknown")
      val method = request.method.toString
      val path = router.pathMatching(request).rawPattern
      httpResponseAuthCounter.labels(clientAppId, path, method).inc()

      contentAuthorization.fetchRules(session, urns).map { rules =>
        val durationActions = extractDurations(session, urns, rules, extractFullTrackDurations(visitor))
        trackPolicyApplicator(session, visitor, rules, durationActions)
          .map(json => JsonResponseBuilder.ok(Json.stringify(json)))
          .getOrElse(JsonResponseBuilder.forbidden())
      }
    }

  private def extractFullTrackDurations(visitor: TracksVisitor): Map[Urn, Int] = {
    val durations = ListBuffer[(Urn, Int)]()
    visitor.apply {
      case (urn, track) =>
        (track.json \ "duration").asOpt[Int].foreach { duration =>
          durations += urn -> duration
        }
        Some(track.json)
    }
    durations.toMap
  }

  private def extractDurations(
      session: UserSession,
      urns: Seq[Urn],
      contentAuth: Seq[policies.ContentAuthorization],
      fullTrackDurations: Map[Urn, Int]
  ): List[TrackDurationAction] = {
    val snipContentAuth = contentAuth.filter(_.getPolicy == policies.ContentPolicy.SNIP).toSet
    if (snipContentAuth.isEmpty)
      getDurationActionsForAllButSnip(urns, snipContentAuth)
    else {
      val actionsSnip = getDurationActionsForSnip(session, fullTrackDurations)
      val actionsAllButSnip = getDurationActionsForAllButSnip(urns, snipContentAuth)
      actionsSnip ++ actionsAllButSnip
    }
  }

  private def getDurationActionsForSnip(
      session: UserSession,
      fullTrackDurations: Map[Urn, Int]
  ): List[TrackDurationAction] = {
    fullTrackDurations.map {
      case (urn, duration) =>
        if (duration > TrackDurationAction.snipDuration)
          TrackDurationAction(urn, NeedsModification, Some(TrackDurationAction.snipDuration))
        else
          TrackDurationAction(urn, DoesNotNeedModification, None)
    }
  }.toList

  private def getDurationActionsForAllButSnip(
      allTrackIds: Seq[Urn],
      snipContentAuths: Set[policies.ContentAuthorization]
  ): List[TrackDurationAction] = {
    val snipUrns = snipContentAuths.map(_.getUrn)
    allTrackIds.filter(!snipUrns.contains(_)).map(urn => TrackDurationAction(urn, DoesNotNeedModification, None)).toList
  }
}
