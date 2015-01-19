package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.BffController
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{TrackStreamMappersComponent, TrackStreamResponseMapper}
import com.soundcloud.publicApiStrangler.support.{GateKeeperClientComponent, TrackStreamSnipHandlerComponent}
import com.twitter.util.Future


/**
 * Overrides the public api endpoints used to retrieve track streams.
 * Reason for overriding is to add support for SNIP content policy.
 */
trait TrackStreamsController extends BffController
    with TrackStreamSnipHandlerComponent
    with TrackStreamMappersComponent
    with GateKeeperClientComponent {

  get("/tracks/:trackId/streams")(handleStreamRequest(_, trackStreamUrlToJonResponseMapper))
  get("/tracks/:trackId/streams.json")(handleStreamRequest(_, trackStreamUrlToJonResponseMapper))
  get("/i1/tracks/:trackId/streams")(handleStreamRequest(_, trackStreamUrlToJonResponseMapper))
  get("/i1/tracks/:trackId/streams.json")(handleStreamRequest(_, trackStreamUrlToJonResponseMapper))
  get("/tracks/:trackId/stream")(handleStreamRequest(_, trackStreamUrlToRedirectMapper))
  get("/tracks/:trackId/stream.json")(handleStreamRequest(_, trackStreamUrlToRedirectMapper))

  private def handleStreamRequest(request: Request, mapper: TrackStreamResponseMapper) : Future[ResponseBuilder] = {
    withUserSession(request) {
      (session: UserSession) =>
        snipEnabled(session).flatMap(
            if (_)
              trackStreamSnipHandler.handle(request, session, mapper)
            else
              mothershipDispatcher.dispatch(request)
        )
    }
  }

  private def snipEnabled(session:UserSession) : Future[Boolean] =
    gatekeeperClient.isFeatureAccessible(session, "pub-api-snip-support")
      .handle
        {
          case ex :  Throwable =>
            logger.error("Exception when accessing gatekeeper", ex)
            false
        }
}
