package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.media.MediaUrlsRepository
import com.soundcloud.jvmkit.policies.{ContentAuthorization, ContentPolicy}
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.mapper.trackstreams.TrackStreamResponseMapper
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Forwards stream requests to public api but intervene in case content policy for track is SNIP
 * or BLOCK.
 *
 * We still forward request to public-api because it has support for play restrictions which we
 * do not support yet in bff apps or authsy.
 */
class TrackStreamHandler(mothershipDispatcher: DispatchToMothershipHandler,
                             contentAuthService: ContentAuthorizationService,
                             mediaUrlsRepository: MediaUrlsRepository) {

  def handle(request: Request, userSession: UserSession, mapper: TrackStreamResponseMapper): Future[ResponseBuilder] = {
    val trackUrn = new Urn("soundcloud", "tracks", request.routeParams("trackId"))

    if (urnWithNumericIdentifier(trackUrn)) {
      val responses = Future.join(mothershipDispatcher.dispatch(request), contentAuthFor(userSession, trackUrn))
      responses.flatMap {
        case (mothershipResponse: ResponseBuilder, contentAuth: ContentAuthorization) =>
          if (mothershipResponse.build.getStatusCode < 400) {
            contentAuth.getPolicy match {
              case ContentPolicy.ALLOW | ContentPolicy.MONETIZE => Future.value(mothershipResponse)
              case ContentPolicy.SNIP => replaceStream(userSession, trackUrn, contentAuth, mapper)
              case ContentPolicy.BLOCK if (userSession.isAnonymous) => Future.value(generateResponseFor(HttpResponseStatus.UNAUTHORIZED))
              case ContentPolicy.BLOCK => Future.value(generateResponseFor(HttpResponseStatus.FORBIDDEN))
            }
          } else
            Future.value(mothershipResponse)
      }
    } else {
      Future.value(generateResponseFor(HttpResponseStatus.NOT_FOUND))
    }
  }


  private def contentAuthFor(session: UserSession, trackUrn: Urn): Future[ContentAuthorization] = {
    val contentAuthorizations = contentAuthService.findRulesApplicableTo(session, Seq(trackUrn))
    contentAuthorizations.map(ca => ca.head)
  }

  private def replaceStream(session: UserSession, trackUrn: Urn, contentAuth: ContentAuthorization, mapper: TrackStreamResponseMapper): Future[ResponseBuilder] = {
    val mediaUrls = mediaUrlsRepository.byUrn(session, trackUrn, contentAuth, false)
    mapper.map(mediaUrls)
  }

  private def urnWithNumericIdentifier(urn: Urn) = urn.getIdentifier.matches("\\d+")

  private def generateResponseFor(status: HttpResponseStatus) =
    new ResponseBuilder().status(status.getCode).
      header("Status", status.getCode + " " + status.getReasonPhrase).
      header("Content-Type", "application/json; charset=utf-8").
      header("Date", DateTime.now.toString(DateTimeFormat.forPattern("E, d MMM yyyy HH:mm:ss z"))).
      body("{\"errors\":[{\"error_message\":\"" + status.getCode + " - " + status.getReasonPhrase + "\"}]}")


}
