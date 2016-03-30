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

  get("/tracks/:trackId/streams")(handleStreamRequest(_, trackStreamUrlToJsonResponseMapper))
  get("/tracks/:trackId/streams.json")(handleStreamRequest(_, trackStreamUrlToJsonResponseMapper))
  get("/i1/tracks/:trackId/streams")(handleStreamRequest(_, trackStreamUrlToJsonResponseMapper))
  get("/i1/tracks/:trackId/streams.json")(handleStreamRequest(_, trackStreamUrlToJsonResponseMapper))

  get("/tracks/:trackId/stream")(handleStreamRequest(_, trackStreamUrlToRedirectMapper))
  get("/tracks/:trackId/stream.json")(handleStreamRequest(_, trackStreamUrlToRedirectMapper))

//  head("/tracks/:trackId/stream")(handleStreamRequest(_, trackStreamUrlToRedirectMapper))
//  head("/tracks/:trackId/stream.json")(handleStreamRequest(_, trackStreamUrlToRedirectMapper))

  private def handleStreamRequest(request: Request, mapper: TrackStreamResponseMapper): Future[ResponseBuilder] = {

    userAuthentication.withUserSession(request) {
      (session: UserSession) =>
        trackStreamHandler.handle(request, session, mapper)
    }
  }
}
