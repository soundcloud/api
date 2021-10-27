package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.authorization.Reasonator
import com.soundcloud.publicApiStrangler.service.UnavailableByPolicy
import com.soundcloud.publicApiStrangler.service.media._
import com.soundcloud.publicApiStrangler.support.ErrorResponse
import com.soundcloud.publicApiStrangler.support.TrackUrnUtil.getTrackUrn
import com.twitter.finagle.http.{MediaType, Method, Response, Status}
import com.twitter.util.{Future, Return, Try}
import play.api.libs.json.Json

case class StreamParams(trackUrn: Urn, secretToken: Option[String])

class TrackStreamsHandler(
    userAuthentication: UserAuthentication,
    streamService: StreamService,
    trackAccessRecorderService: TrackAccessRecorderService
) {

  def handleStreamRequest(request: HandlerRequest): Future[Response] =
    handleStreamRequest(request, singleStream = false)

  def redirectStreamRequest(request: HandlerRequest): Future[Response] =
    handleStreamRequest(request, singleStream = true)

  private def handleStreamRequest(
      request: HandlerRequest,
      singleStream: Boolean
  ): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      extractParams(request) match {
        case Some(streamParams) =>
          streamService.fetchUrls(session, streamParams.trackUrn, streamParams.secretToken, singleStream).flatMap {
            case Good(streamResponse) =>
              trackAccessRecorderService.recordStreamAccess(
                session,
                request,
                streamParams.trackUrn,
                loggingEnabled = singleStream
              )(Future.value(renderStreamResponse(request, streamResponse)))
            case Bad(CustomError(UnavailableByPolicy(_, reason), _)) =>
              Future.value(Reasonator.reasonToError(reason))
            case Bad(NotAuthorized(_)) => Future.value(ErrorResponse.forbidden())
            case Bad(_) => Future.value(ErrorResponse.notFound())
          }
        case None => Future.value(ErrorResponse.badRequest())
      }
    }
  }

  private def renderStreamResponse(
      request: HandlerRequest,
      streamResponse: MediaStreamResponse
  ): Response = {
    val builder = streamResponse match {
      case RedirectStreamResponse(url) =>
        ResponseBuilder().header("Location", url).status(Status.Found)
      case _ => ResponseBuilder().status(Status.Ok)
    }
    if (request.method != Method.Head)
      builder.header("Content-Type", MediaType.JsonUtf8).body(Json.stringify(Json.toJson(streamResponse))).build
    else
      builder.build
  }

  private def extractParams(request: HandlerRequest): Option[StreamParams] = {
    Try(getTrackUrn(request)) match {
      case Return(urn) => Some(StreamParams(urn, request.params.get("secret_token")))
      case _ => None
    }
  }
}
