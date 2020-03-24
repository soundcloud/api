package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.TrackUrnUtil.trackUrn
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{
  TrackStreamJsonResponseMapper,
  TrackStreamRedirectResponseMapper,
  TrackStreamResponseMapper
}
import com.soundcloud.publicApiStrangler.service.media._
import com.twitter.finagle.http.{MediaType, Method, Response, Status}
import com.twitter.util.{Future, Return, Try}
import play.api.libs.json.Json

import scala.util.control.NonFatal

case class StreamParams(trackUrn: Urn, secretToken: Option[String])

/**
  * Overrides the public api endpoints used to retrieve track streams.
  * Reason for overriding is to migrate to the media-service service, and to add support for SNIP content policy.
  */
class TrackStreamsHandler(
    userAuthentication: UserAuthentication,
    trackStreamUrlToJsonResponseMapper: TrackStreamJsonResponseMapper,
    trackStreamUrlToRedirectMapper: TrackStreamRedirectResponseMapper,
    streamService: StreamService,
    legacyStreamService: LegacyStreamService,
    trackAccessRecorderService: TrackAccessRecorderService,
    telemetry: Telemetry
) {

  private val logger = SoundCloudLoggerFactory.getLogger(getClass)

  private val inconsistentStreamResponsesCounter =
    telemetry.counter(
      "inconsistent_stream_response_total",
      "Count of inconsistent (not matching) responses from legacy and new stream services",
      "fetch_type"
    )

  def handleStreamRequest(request: HandlerRequest): Future[Response] =
    handleStreamRequest(request, trackStreamUrlToJsonResponseMapper, singleStream = false)

  def redirectStreamRequest(request: HandlerRequest): Future[Response] =
    handleStreamRequest(request, trackStreamUrlToRedirectMapper, singleStream = true)

  private def handleStreamRequest(
      request: HandlerRequest,
      mapper: TrackStreamResponseMapper,
      singleStream: Boolean
  ): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      extractParams(request) match {
        case Some(streamParams) =>
          handleWithStreamService(session, streamParams, singleStream).flatMap {
            case MediaStreamNotFoundError =>
              Future.value(renderStreamResponse(request, session, MediaStreamNotFoundError))
            case MediaStreamNotAllowed =>
              Future.value(renderStreamResponse(request, session, MediaStreamNotAllowed))
            case streamResponse if singleStream =>
              trackAccessRecorderService.recordStreamAccess(session, request, streamParams.trackUrn)(
                Future.value(renderStreamResponse(request, session, streamResponse))
              )
            case streamResponse =>
              trackAccessRecorderService.recordStreamAccess(
                session,
                request,
                streamParams.trackUrn,
                loggingEnabled = false
              )(Future.value(renderStreamResponse(request, session, streamResponse)))
          }
        case None => Future.value(ResponseBuilder().status(Status.BadRequest).build)
      }
    }
  }

  private def handleWithStreamService(
      session: UserSession,
      streamParams: StreamParams,
      singleStream: Boolean
  ): Future[MediaStreamResponse] = {
    if (singleStream) {
      Future
        .join(
          streamService.fetchSingle(session, streamParams.trackUrn, streamParams.secretToken).rescue {
            case NonFatal(_) => Future.value(MediaStreamNotFoundError)
          },
          legacyStreamService.fetchSingle(session, streamParams.trackUrn, streamParams.secretToken)
        )
        .map {
          case (newResult, legacyResult) =>
            if (!validateResponses(newResult, legacyResult)) logInconsistency(newResult, legacyResult, "single_fetch")
            legacyResult
        }
    } else {
      Future
        .join(
          streamService.fetchMultiple(session, streamParams.trackUrn, streamParams.secretToken).rescue {
            case NonFatal(_) => Future.value(MediaStreamNotFoundError)
          },
          legacyStreamService.fetchMultiple(session, streamParams.trackUrn, streamParams.secretToken)
        )
        .map {
          case (newResult, legacyResult) =>
            if (!validateResponses(newResult, legacyResult)) logInconsistency(newResult, legacyResult, "multiple_fetch")
            legacyResult
        }
    }
  }

  /**
    * Compares stream service responses coming from two different component: new stream service and legacy stream
    *
    * @param newServiceResult    Media Response from new stream service
    * @param legacyServiceResult Media Response from old service
    * @return true if response the same, otherwise false
    */
  private def validateResponses(
      newServiceResult: MediaStreamResponse,
      legacyServiceResult: MediaStreamResponse
  ): Boolean = {
    var equal: Boolean = true
    legacyServiceResult match {
      case legacyUrl: MediaStreamUrl =>
        equal = newServiceResult match {
          case url: MediaStreamUrl => compareLinks(url.httpMp3, legacyUrl.httpMp3)
          case _ => false
        }
      case legacyUrl: MediaStreamUrls =>
        equal = newServiceResult match {
          case urls: MediaStreamUrls =>
            equal &= compareLinks(urls.httpMp3, legacyUrl.httpMp3)
            equal &= compareLinks(urls.hlsMp3, legacyUrl.hlsMp3)
            equal &= compareLinks(urls.httpPreviewMp3, legacyUrl.httpPreviewMp3)
            equal &= compareLinks(urls.hlsOpus.getOrElse(""), legacyUrl.hlsOpus.getOrElse(""))
            equal
          case _ => false
        }
      case legacyUrl: PreviewUrls =>
        equal = newServiceResult match {
          case previewUrls: PreviewUrls =>
            equal &= compareLinks(previewUrls.httpMp3, legacyUrl.httpMp3)
            equal &= compareLinks(previewUrls.hlsMp3, legacyUrl.hlsMp3)
            equal
          case _ => false
        }
      case _: Any => equal = (newServiceResult == legacyServiceResult)
    }
    equal
  }

  private def logInconsistency(
      newResult: MediaStreamResponse,
      legacyResult: MediaStreamResponse,
      label: String
  ): Unit = {
    inconsistentStreamResponsesCounter.labels(label).inc()
    logger.warn(
      s"Inconsistent ${label} responses from new and legacy services:" +
        s"new -> ${newResult}; legacy -> ${legacyResult}"
    )
  }

  private def compareLinks(newServiceLink: String, legacyServiceLink: String): Boolean = {
    newServiceLink.split("\\?")(0) == legacyServiceLink.split("\\?")(0)
  }

  private def renderStreamResponse(
      request: HandlerRequest,
      session: UserSession,
      streamResponse: MediaStreamResponse
  ): Response = {
    val builder = streamResponse match {
      case MediaStreamUrl(url) =>
        ResponseBuilder().header("Location", url).status(Status.Found)
      case MediaStreamNotFoundError | MediaStreamNotAllowed =>
        ResponseBuilder().status(Status.NotFound)
      case _ => ResponseBuilder().status(Status.Ok)
    }
    if (request.method != Method.Head)
      builder.mediaType(MediaType.Json).body(Json.stringify(Json.toJson(streamResponse))).build
    else
      builder.build
  }

  private def extractParams(request: HandlerRequest): Option[StreamParams] = {
    Try(trackUrn(request)) match {
      case Return(urn) => Some(StreamParams(urn, request.params.get("secret_token")))
      case _ => None
    }
  }
}
