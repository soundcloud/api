package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.TrackDurationActionStatus._
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.Json

import scala.collection.mutable.ListBuffer

class AuthorizeHttpResponse(
    contentAuthorization: ContentAuthorizationRules,
    userAuthentication: UserAuthentication,
    trackPolicyApplicator: TrackPolicyApplicator
) {
  def apply(request: HandlerRequest, status: Status, body: String): Future[Response] =
    authorize(request, status, body, BuilderResponse(body))

  private def authorize(
      request: HandlerRequest,
      status: Status,
      body: String,
      originalResponse: BuilderResponse
  ): Future[Response] =
    CollectTrackUrns(originalResponse.content) match {
      case Some((visitor, urns)) =>
        authorize(request, status, visitor, urns, originalResponse)
      case None =>
        Future({
          val response = originalResponse.render
          response.status = status
          response
        })
    }

  private def authorize(
      request: HandlerRequest,
      status: Status,
      visitor: TracksVisitor,
      urns: Seq[Urn],
      originalResponse: BuilderResponse
  ): Future[Response] =
    userAuthentication.withUserSession(request) { session =>
      contentAuthorization.fetchRules(session, urns).map { rules =>
        val durationActions = extractDurations(session, urns, rules, extractFullTrackDurations(visitor))
        trackPolicyApplicator(session, visitor, rules, durationActions)
          .map(Json.stringify)
          .map(originalResponse.withBody)
          .map(response => {
            response.status = status
            response
          })
          .getOrElse(ResponseBuilder(status = Status.Forbidden).build)
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
