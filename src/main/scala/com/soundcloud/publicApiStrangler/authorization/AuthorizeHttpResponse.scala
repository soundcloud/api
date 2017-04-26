package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.media.WaveformUrlsRepository
import com.soundcloud.jvmkit.ModuleConversions._
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.TrackWaveformActionStatus._
import com.soundcloud.publicApiStrangler.client.BigJvmKitConversions._
import com.soundcloud.publicApiStrangler.policies
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.Json

class AuthorizeHttpResponse(
                             contentAuthorization: ContentAuthorizationRules,
                             userAuthentication: UserAuthentication,
                             waveformUrlsRepository: WaveformUrlsRepository,
                             trackPolicyApplicator: TrackPolicyApplicator) {

  def apply(request: HandlerRequest, status: Status, body: String): Future[Response] =
    authorize(request, status, body, BuilderResponse(body))

  private def authorize(request: HandlerRequest, status: Status, body: String, originalResponse: BuilderResponse): Future[Response] =
    CollectTrackUrns(originalResponse.content) match {
      case Some((visitor, urns)) =>
        authorize(request, status, visitor, urns, originalResponse)
      case None =>
        Future({
          val response = originalResponse.render
          response.setStatusCode(status.code)
          response
        })
    }

  private def authorize(request: HandlerRequest, status: Status, visitor: TracksVisitor, urns: Seq[Urn], originalResponse: BuilderResponse): Future[Response] =
    userAuthentication.withUserSession(request) { session =>
      contentAuthorization.fetchRules(session, urns).flatMap { rules =>
        retrieveWaveforms(session, urns, rules).map { waveforms =>
          trackPolicyApplicator(session, visitor, rules, waveforms.toList)
            .map(Json.stringify)
            .map(originalResponse.withBody)
            .map(response => {
              response.setStatusCode(status.code);
              response
            })
            .getOrElse(ResponseBuilder(status = Status.Forbidden).build)
        }
      }
    }

  private def retrieveWaveforms(session: UserSession, urns: Seq[Urn], contentAuth: Seq[policies.ContentAuthorization]): Future[Seq[TrackWaveformAction]] = {
    val snipContentAuth = contentAuth.filter(_.getPolicy == policies.ContentPolicy.SNIP).toSet
    if (snipContentAuth.isEmpty)
      Future.value(getWaveformActionsForAllButSnip(urns, snipContentAuth))
    else {
      val actionsSnip = getWaveformActionsForSnip(session, snipContentAuth)
      val actionsAllButSnip = getWaveformActionsForAllButSnip(urns, snipContentAuth)
      actionsSnip.map(list => list ++ actionsAllButSnip)
    }
  }

  private def getWaveformActionsForSnip(session: UserSession, snipContentAuths: Set[policies.ContentAuthorization]): Future[Seq[TrackWaveformAction]] = {
    val waveforms = waveformUrlsRepository.fetchWaveformUrls(session, snipContentAuths.map(toBigJvmKitContentAuthorization))
    waveforms.map(waveformsMap => waveformsMap.keys.map(urn => TrackWaveformAction(urn, NeedsModification, Some(waveformsMap(urn)))).toList)
  }

  private def getWaveformActionsForAllButSnip(allTrackIds: Seq[Urn], snipContentAuths: Set[policies.ContentAuthorization]): Seq[TrackWaveformAction] = {
    val snipUrns = snipContentAuths.map(_.getUrn)
    allTrackIds.filter(!snipUrns.contains(_)).map(urn => TrackWaveformAction(urn, DoesNotNeedModification, None))
  }
}
