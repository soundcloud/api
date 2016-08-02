package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.authorization.PublicApiSiloing
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{TrackStreamJsonResponseMapper, TrackStreamRedirectResponseMapper, TrackStreamResponseMapper}
import com.soundcloud.publicApiStrangler.support.TrackStreamHandler
import com.twitter.util.Future

/**
 * Overrides the public api endpoints used to retrieve track streams.
 * Reason for overriding is to add support for SNIP content policy.
 */
class TrackStreamsController(
                              userAuthentication: UserAuthentication,
                              trackStreamUrlToJsonResponseMapper: TrackStreamJsonResponseMapper,
                              trackStreamUrlToRedirectMapper: TrackStreamRedirectResponseMapper,
                              trackStreamHandler: TrackStreamHandler,
                              publicApiSiloing: PublicApiSiloing
                              )
  extends BffInjectionBasedController {

  Seq("", "/", ".json").foreach { end: String => {
    Seq("", "/v1").foreach { start: String => {
        get(s"$start/tracks/:trackId/streams$end")(handleStreamRequest(_, trackStreamUrlToJsonResponseMapper))
        head(s"$start/tracks/:trackId/streams$end")(handleStreamRequest(_, trackStreamUrlToJsonResponseMapper))

        get(s"$start/tracks/:trackId/stream$end")(handleStreamRequest(_, trackStreamUrlToRedirectMapper))
        head(s"$start/tracks/:trackId/stream$end")(handleStreamRequest(_, trackStreamUrlToRedirectMapper))
      }
    }

    get(s"/i1/tracks/:trackId/streams$end")(handleStreamRequest(_, trackStreamUrlToJsonResponseMapper))
    head(s"/i1/tracks/:trackId/streams$end")(handleStreamRequest(_, trackStreamUrlToJsonResponseMapper))
    }
  }

  private def handleStreamRequest(request: Request, mapper: TrackStreamResponseMapper): Future[ResponseBuilder] = {
    userAuthentication.withUserSession(request) { session =>
      publicApiSiloing.withSiloedSession(session) {
        trackStreamHandler.handle(request, session, mapper)
      }
    }
  }
}
