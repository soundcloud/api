package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.PublicApiSiloing
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{TrackStreamJsonResponseMapper, TrackStreamRedirectResponseMapper, TrackStreamResponseMapper}
import com.soundcloud.publicApiStrangler.service.media.{StreamNotFoundError, StreamResponse, StreamService, StreamUrl}
import com.twitter.finagle.http.{MediaType, Method, Response, Status}
import com.twitter.util.Future
import play.api.libs.json.Json

/**
  * Overrides the public api endpoints used to retrieve track streams.
  * Reason for overriding is to add support for SNIP content policy.
  */

case class StreamParams(trackUrn: Urn, secretToken: Option[String])

class TrackStreamsHandler(
                           userAuthentication: UserAuthentication,
                           trackStreamUrlToJsonResponseMapper: TrackStreamJsonResponseMapper,
                           trackStreamUrlToRedirectMapper: TrackStreamRedirectResponseMapper,
                           trackStreamHandler: TrackStreamHandler,
                           streamService: StreamService,
                           mediaServiceEnabled: () => Future[Boolean],
                           publicApiSiloing: PublicApiSiloing
                         ) {

  def handleStreamRequest(request: HandlerRequest): Future[Response] = handleStreamRequest(request, trackStreamUrlToJsonResponseMapper, singleStream = false)

  def redirectStreamRequest(request: HandlerRequest): Future[Response] = handleStreamRequest(request, trackStreamUrlToRedirectMapper, singleStream = true)

  private def handleStreamRequest(request: HandlerRequest, mapper: TrackStreamResponseMapper, singleStream: Boolean): Future[Response] = {
    mediaServiceEnabled().flatMap{ enabled =>
      userAuthentication.withUserSession(request) { session =>
        publicApiSiloing.withSiloedSession(session) {
          if (enabled) {
            handleWithStreamService(session, request, singleStream).flatMap {
              case StreamNotFoundError => trackStreamHandler.handle(request, session, mapper)
              case response => Future.value(renderStreamResponse(request, response))
            }
          } else {
            trackStreamHandler.handle(request, session, mapper)
          }
        }
      }
    }
  }

  private def handleWithStreamService(session: UserSession, request: HandlerRequest, singleStream: Boolean): Future[StreamResponse] = {
    extractParams(request) match {
      case Some(params) =>
        if (singleStream) streamService.fetchSingle(session, params.trackUrn, params.secretToken)
        else streamService.fetchMultiple(session, params.trackUrn, params.secretToken)
    }
  }

  private def renderStreamResponse(request: HandlerRequest, streamResponse: StreamResponse): Response = {
    val builder = streamResponse match {
      case StreamUrl(url) =>
        ResponseBuilder().header("Location", url).status(Status.Found)
      case StreamNotFoundError =>
        ResponseBuilder().status(Status.NotFound)
      case _ => ResponseBuilder().status(Status.Ok)
    }
    if (request.method != Method.Head)
      builder.mediaType(MediaType.Json).body(Json.stringify(Json.toJson(streamResponse))).build
    else
      builder.build
  }

  private def extractParams(request: HandlerRequest): Option[StreamParams] = {
    request.routeParams.get("trackId").map { trackId =>
      StreamParams(Urn("soundcloud", "tracks", trackId), request.params.get("secretToken"))
    }
  }
}
