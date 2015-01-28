package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.media.MediaUrlsRepository
import com.soundcloud.jvmkit.policies.{ContentAuthorization, ContentPolicy}
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.controller.DefaultResponseHeaders
import com.soundcloud.publicApiStrangler.mapper.trackstreams.TrackStreamResponseMapper
import com.twitter.util.Future

/**
 * Forwards stream requests to public api but in case content policy for track is SNIP
 * we go out to MediaUrlsRepository.
 *
 * We still go through public-api because it has support for play restrictions which we
 * did not support yet in bff apps or authsy.
 */
class TrackStreamSnipHandler(mothershipDispatcher:DispatchToMothershipHandler,
                                     contentAuthService: ContentAuthorizationService,
                                     mediaUrlsRepository: MediaUrlsRepository) {

  def handle(request: Request, userSession: UserSession, mapper: TrackStreamResponseMapper): Future[ResponseBuilder] = {
    val trackUrn = new Urn("soundcloud", "tracks", request.routeParams("trackId"))
    val original = mothershipDispatcher.dispatch(request)
    val replaced = for {
      responseBuilder <- original if responseBuilder.build.getStatusCode < 400
      contentAuth <- contentAuthFor(userSession, trackUrn) if contentAuth.getPolicy.equals(ContentPolicy.SNIP)
      replaced <- replaceStream(userSession, trackUrn, contentAuth, mapper)
    } yield replaced
    replaced.liftToTry.join(original).map { case (snip, full) => snip.getOrElse(full)}
  }


  private def contentAuthFor(session:UserSession, trackUrn:Urn): Future[ContentAuthorization] = {
    val contentAuthorizations = contentAuthService.findRulesApplicableTo(session, Seq(trackUrn))
    contentAuthorizations.map(ca => ca.head)
  }

  private def replaceStream(session:UserSession, trackUrn:Urn, contentAuth:ContentAuthorization, mapper: TrackStreamResponseMapper) : Future[ResponseBuilder] = {
    val mediaUrls = mediaUrlsRepository.byUrn(session, trackUrn, contentAuth, false)
    mapper.map(mediaUrls).map(_.headers(DefaultResponseHeaders.defaultHeaders))
  }


}
