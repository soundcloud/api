package com.soundcloud.apipublic.handler

import com.soundcloud.apipublic.authorization.Reasonator
import com.soundcloud.apipublic.service.UnavailableByPolicy
import com.soundcloud.apipublic.service.media._
import com.soundcloud.apipublic.support.ErrorResponse
import com.soundcloud.apipublic.support.TrackUrnUtil.getTrackUrn
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.twitter.finagle.http.{MediaType, Method, Response, Status}
import com.twitter.util.{Future, Return, Try}
import play.api.libs.json.Json

case class StreamParams(trackUrn: Urn, secretToken: Option[String])
case class PlayParams(trackUrn: Urn, secretToken: Option[String], transcoding: String, protocol: String)

class TrackStreamsHandler(
    userAuthentication: UserAuthentication,
    streamService: StreamService,
    trackAccessRecorderService: TrackAccessRecorderService
) {

  def handlePlayRequest(request: HandlerRequest): Future[Response] =
    userAuthentication.withUserSession(request) { session =>
      extractPlayParams(request) match {
        case Some(params) =>
          streamService.fetchStreamUrl(session, params).flatMap {
            case Good(streamResponse) =>
              trackAccessRecorderService.recordStreamAccess(
                session,
                request,
                params.trackUrn
              )(
                Future.value(renderStreamResponse(request, streamResponse))
              )
            case Bad(CustomError(UnavailableByPolicy(_, reason), _)) => Future.value(Reasonator.reasonToError(reason))
            case Bad(NotAuthorized(_)) => Future.value(ErrorResponse.forbidden())
            case Bad(_) => Future.value(ErrorResponse.notFound())
          }
        case None => Future.value(ErrorResponse.badRequest())
      }
    }

  def handleLegacyPlayRequestAsPreview(
      request: HandlerRequest
  ): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      extractParams(request) match {
        case Some(params) =>
          streamService
            .fetchLegacyProgressivePreviewTranscodingUrl(
              session,
              params.trackUrn,
              params.secretToken
            )
            .flatMap {
              case Good(streamResponse) =>
                trackAccessRecorderService.recordStreamAccess(
                  session,
                  request,
                  params.trackUrn
                )(
                  Future.value(renderStreamResponse(request, streamResponse))
                )
              case Bad(CustomError(UnavailableByPolicy(_, reason), _)) => Future.value(Reasonator.reasonToError(reason))
              case Bad(NotAuthorized(_)) => Future.value(ErrorResponse.forbidden())
              case Bad(_) => Future.value(ErrorResponse.notFound())
            }
        case None => Future.value(ErrorResponse.badRequest())
      }
    }
  }

  def handleStreamsRequest(
      request: HandlerRequest
  ): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      extractParams(request) match {
        case Some(streamParams) =>
          streamService.fetchTranscodingUrls(session, streamParams.trackUrn, streamParams.secretToken).flatMap {
            case Good(streamResponse) =>
              Future.value(renderStreamResponse(request, streamResponse))
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
        ResponseBuilder().header("Location", url).header("Cache-Control", "private, max-age=0").status(Status.Found)
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

  private def extractPlayParams(request: HandlerRequest): Option[PlayParams] = {
    Try((getTrackUrn(request), getTranscoding(request), getProtocol(request))) match {
      case Return((urn, Some(transcoding), Some(protocol))) =>
        Some(
          PlayParams(
            trackUrn = urn,
            secretToken = request.params.get("secret_token"),
            transcoding = transcoding,
            protocol = protocol
          )
        )
      case _ => None
    }
  }

  private def getTranscoding(request: HandlerRequest) = request.routeParams.get("transcoding")

  private def getProtocol(request: HandlerRequest) = request.routeParams.get("protocol")
}
