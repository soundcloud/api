package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.PublicApiSiloing
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{TrackStreamJsonResponseMapper, TrackStreamRedirectResponseMapper, TrackStreamResponseMapper}
import com.soundcloud.publicApiStrangler.service.media._
import com.twitter.finagle.http.{MediaType, Method, Response, Status}
import com.twitter.util.Future
import play.api.libs.json.Json

case class StreamParams(trackUrn: Urn, secretToken: Option[String])

/**
  * Overrides the public api endpoints used to retrieve track streams.
  * Reason for overriding is to migrate to the media-service service, and to add support for SNIP content policy.
  */
class TrackStreamsHandler(
                           userAuthentication: UserAuthentication,
                           trackStreamUrlToJsonResponseMapper: TrackStreamJsonResponseMapper,
                           trackStreamUrlToRedirectMapper: TrackStreamRedirectResponseMapper,
                           trackStreamHandler: TrackStreamHandler,
                           streamService: StreamService,
                           trackAccessRecorderService: TrackAccessRecorderService,
                           mediaServiceEnabled: () => Future[Boolean],
                           publicApiSiloing: PublicApiSiloing
                         ) {

  def handleStreamRequest(request: HandlerRequest): Future[Response] = handleStreamRequest(request, trackStreamUrlToJsonResponseMapper, singleStream = false)

  def redirectStreamRequest(request: HandlerRequest): Future[Response] = handleStreamRequest(request, trackStreamUrlToRedirectMapper, singleStream = true)

  private def handleStreamRequest(request: HandlerRequest, mapper: TrackStreamResponseMapper, singleStream: Boolean): Future[Response] = {
    mediaServiceEnabled().flatMap { enabled =>
      userAuthentication.withUserSession(request) { session =>
        publicApiSiloing.withSiloedSession(session) {
          if (enabled) {
            extractParams(request) match {
              case Some(streamParams) =>
                handleWithStreamService(session, streamParams, singleStream).flatMap {
                  case StreamNotFoundError => trackStreamHandler.handle(request, session, mapper)
                  case streamResponse if singleStream => trackAccessRecorderService.recordStreamAccess(session, request, streamParams.trackUrn)(Future.value(renderStreamResponse(request, streamResponse)))
                  case streamResponse => Future.value(renderStreamResponse(request, streamResponse))
                }
              case None => Future.value(ResponseBuilder().status(Status.BadRequest).build)
            }
          } else {
            trackStreamHandler.handle(request, session, mapper)
          }
        }
      }
    }
  }

  private def handleWithStreamService(session: UserSession, streamParams: StreamParams, singleStream: Boolean): Future[StreamResponse] = {
    if (singleStream) streamService.fetchSingle(session, streamParams.trackUrn, streamParams.secretToken)
    else streamService.fetchMultiple(session, streamParams.trackUrn, streamParams.secretToken)
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
      StreamParams(Urn("soundcloud", "tracks", trackId), request.params.get("secret_token"))
    }
  }
}
