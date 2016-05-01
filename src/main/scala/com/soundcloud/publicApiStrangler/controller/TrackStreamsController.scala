package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.security.RequestForAuthenticator
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{TrackStreamJsonResponseMapper, TrackStreamRedirectResponseMapper, TrackStreamResponseMapper}
import com.soundcloud.publicApiStrangler.support.{DispatchToMothershipHandler, TrackStreamHandler}
import com.twitter.util.Future


/**
 * Overrides the public api endpoints used to retrieve track streams.
 * Reason for overriding is to add support for SNIP content policy.
 */
class TrackStreamsController(
                              userAuthentication: UserAuthentication,
                              trackStreamUrlToJsonResponseMapper: TrackStreamJsonResponseMapper,
                              trackStreamUrlToRedirectMapper: TrackStreamRedirectResponseMapper,
                              trackStreamHandler: TrackStreamHandler
                              )
  extends BffInjectionBasedController {

  Seq("", "/", ".json").foreach { end: String => {
      get(s"/tracks/:trackId/streams${end}")(handleStreamRequest(_, trackStreamUrlToJsonResponseMapper))
      head(s"/tracks/:trackId/streams${end}")(handleStreamRequest(_, trackStreamUrlToJsonResponseMapper))

      get(s"/tracks/:trackId/stream${end}")(handleStreamRequest(_, trackStreamUrlToRedirectMapper))
      head(s"/tracks/:trackId/stream${end}")(handleStreamRequest(_, trackStreamUrlToRedirectMapper))

      get(s"/i1/tracks/:trackId/streams${end}")(handleStreamRequest(_, trackStreamUrlToJsonResponseMapper))
      head(s"/i1/tracks/:trackId/streams${end}")(handleStreamRequest(_, trackStreamUrlToJsonResponseMapper))
    }
  }

  private def handleStreamRequest(request: Request, mapper: TrackStreamResponseMapper): Future[ResponseBuilder] = {
    userAuthentication.withUserSession(request) { session =>
      trackStreamHandler.handle(request, session, mapper)
    }
  }
}
