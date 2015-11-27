package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.media.MediaUrlsRepository
import com.soundcloud.jvmkit.policies.{ContentAuthorization, ContentPolicy}
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.mapper.trackstreams.TrackStreamResponseMapper
import com.twitter.util.Future
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

/**
 * Forwards stream requests to public api but in case content policy for track is SNIP
 * we go out to MediaUrlsRepository.
 *
 * We still go through public-api because it has support for play restrictions which we
 * did not support yet in bff apps or authsy.
 */
class TrackStreamSnipHandler(mothershipDispatcher: DispatchToMothershipHandler,
                             contentAuthService: ContentAuthorizationService,
                             mediaUrlsRepository: MediaUrlsRepository) {

  def handle(request: Request, userSession: UserSession, mapper: TrackStreamResponseMapper): Future[ResponseBuilder] = {
    val trackUrn = new Urn("soundcloud", "tracks", request.routeParams("trackId"))

    if (urnWithNumericIdentifier(trackUrn)) {
      val responses = Future.join(mothershipDispatcher.dispatch(request), contentAuthFor(userSession, trackUrn))
      responses.flatMap {
        case (mothershipResponse: ResponseBuilder, contentAuth: ContentAuthorization) =>
          if ((mothershipResponse.build.getStatusCode < 400) && (contentAuth.getPolicy.equals(ContentPolicy.SNIP)))
            replaceStream(userSession, trackUrn, contentAuth, mapper)
          else
            Future.value(mothershipResponse)
      }
    } else {
      Future.value(mimicMothershipNotFoundResponse)
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

  private lazy val mimicMothershipNotFoundResponse = {
    new ResponseBuilder().
      notFound.
      header("Status", "404 Not Found").
      header("Content-Type", "application/json; charset=utf-8").
      header("Date", DateTime.now.toString(DateTimeFormat.forPattern("E, d MMM yyyy HH:mm:ss z"))).
      body(Json.stringify(JsObject(Seq("errors" -> JsArray(Seq(JsObject(Seq("error_message" -> JsString("404 - Not Found")))))))))
  }
}
