package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.publicApiStrangler.authorization.PublicApiSiloing
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{TrackStreamJsonResponseMapper, TrackStreamRedirectResponseMapper, TrackStreamResponseMapper}
import com.soundcloud.publicApiStrangler.support.TrackStreamHandler
import com.twitter.finagle.http.Response
import com.twitter.util.Future

/**
  * Overrides the public api endpoints used to retrieve track streams.
  * Reason for overriding is to add support for SNIP content policy.
  */
class TrackStreamsHandler(
                           userAuthentication: UserAuthentication,
                           trackStreamUrlToJsonResponseMapper: TrackStreamJsonResponseMapper,
                           trackStreamUrlToRedirectMapper: TrackStreamRedirectResponseMapper,
                           trackStreamHandler: TrackStreamHandler,
                           publicApiSiloing: PublicApiSiloing
                         ) {

  def handleStreamRequest(request: HandlerRequest): Future[Response] = handleStreamRequest(request, trackStreamUrlToJsonResponseMapper)

  def redirectStreamRequest(request: HandlerRequest): Future[Response] = handleStreamRequest(request, trackStreamUrlToRedirectMapper)

  private def handleStreamRequest(request: HandlerRequest, mapper: TrackStreamResponseMapper): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      publicApiSiloing.withSiloedSession(session) {
        trackStreamHandler.handle(request, session, mapper)
      }
    }
  }
}
