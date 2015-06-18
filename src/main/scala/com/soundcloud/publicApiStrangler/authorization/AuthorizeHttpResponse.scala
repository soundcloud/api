package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.finagle.{Request => BffRequest, ResponseBuilder}
import com.soundcloud.bff.media.WaveformUrlsRepository
import com.soundcloud.bff.web.UserAuthentication
import com.soundcloud.jvmkit.policies.{ContentAuthorization, ContentPolicy}
import com.soundcloud.publicApiStrangler.authorization.TrackWaveformActionStatus._
import com.soundcloud.scalakit.json.Json
import com.soundcloud.scalakit.{Urn, UserSession}
import com.twitter.util.Future

class AuthorizeHttpResponse(
                             contentAuthorization: ContentAuthorizationService,
                             userAuthentication: UserAuthentication,
                             waveformUrlsRepository: WaveformUrlsRepository) {

  def apply(request: BffRequest, status: Int, body: String): Future[ResponseBuilder] =
    authorize(request, status, body, BuilderResponse(body))

  private def authorize(request: BffRequest, status: Int, body: String, originalResponse: BuilderResponse): Future[ResponseBuilder] =
    CollectTrackUrns(originalResponse.content) match {
      case Some((visitor, urns)) =>
        authorize(request, status, visitor, urns, originalResponse)
      case None =>
        Future(originalResponse.render.status(status))
    }

  private def authorize(request: BffRequest, status: Int, visitor: TracksVisitor, urns: List[Urn], originalResponse: BuilderResponse): Future[ResponseBuilder] =
    userAuthentication.withUserSession(request) { session =>
      contentAuthorization.findRulesApplicableTo(session, urns).flatMap { rules =>
        retrieveWaveforms(session, urns, rules).map { waveforms =>
          ApplyTrackPolicies(session, visitor, rules, waveforms)
            .map(Json.stringify)
            .map(originalResponse.withBody)
            .map(_.status(status))
            .getOrElse(render.forbidden)   
        }
      }
    }

  private def render = new ResponseBuilder

  private def retrieveWaveforms(session: UserSession, urns: List[Urn], contentAuth: Seq[ContentAuthorization]): Future[List[TrackWaveformAction]] = {
    val snipContentAuth = contentAuth.filter(_.getPolicy.equals(ContentPolicy.SNIP)).toSet
    if (snipContentAuth.isEmpty)
      Future.value(getWaveformActionsForAllButSnip(urns, snipContentAuth))
    else {
      val actionsSnip = getWaveformActionsForSnip(session, snipContentAuth)
      val actionsAllButSnip = getWaveformActionsForAllButSnip(urns, snipContentAuth)
      actionsSnip.map(list => list ++ actionsAllButSnip)
    }
  }

  private def getWaveformActionsForSnip(session: UserSession, snipContentAuths: Set[ContentAuthorization]): Future[List[TrackWaveformAction]] = {
    val waveforms = waveformUrlsRepository.fetchWaveformUrls(session, snipContentAuths)
    waveforms.map(waveformsMap => waveformsMap.keys.map(urn => TrackWaveformAction(urn, NeedsModification, Some(waveformsMap(urn)))).toList)
  }


  private def getWaveformActionsForAllButSnip(allTrackIds: List[Urn], snipContentAuths: Set[ContentAuthorization]): List[TrackWaveformAction] = {
    val snipUrns = snipContentAuths.map(_.getUrn)
    allTrackIds.filter(!snipUrns.contains(_)).map(urn => TrackWaveformAction(urn, DoesNotNeedModification, None))
  }
}
