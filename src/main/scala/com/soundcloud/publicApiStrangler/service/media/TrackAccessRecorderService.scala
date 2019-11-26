package com.soundcloud.publicApiStrangler.service.media

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.media.TrackAccessRecorderClient
import com.soundcloud.publicApiStrangler.support.RangeHelper
import com.twitter.finagle.http.{Method, Response, Status}
import com.twitter.util.Future

class TrackAccessRecorderService(trackAccessRecorderClient: TrackAccessRecorderClient, telemetry: Telemetry) {
  private val downloadsWithRangeCounter = telemetry.counter(
    "downloads_with_range_total",
    "Number of downloads using Range as header by client application",
    "appid"
  )

  def recordStreamAccess(session: UserSession, request: HandlerRequest, trackUrn: Urn, loggingEnabled: Boolean = true)(
      action: => Future[Response]
  ): Future[Response] =
    recordAccess(session, request, trackUrn, "stream", loggingEnabled && request.method == Method.Get, action)

  def recordDownloadAccess(session: UserSession, request: HandlerRequest, trackUrn: Urn)(
      action: => Future[Response]
  ): Future[Response] = {
    if (request.headerMap.get("Range").nonEmpty) {
      val clientAppId = Option(session.getAgent).map(_.identifier).getOrElse("unknown")
      downloadsWithRangeCounter.labels(clientAppId).inc()
    }
    recordAccess(session, request, trackUrn, "download", loggingEnabled = true, action)
  }

  private def recordAccess(
      session: UserSession,
      request: HandlerRequest,
      trackUrn: Urn,
      accessFor: String,
      loggingEnabled: Boolean,
      action: => Future[Response]
  ): Future[Response] =
    trackAccessRecorderClient
      .recordAccess(session, trackUrn, accessFor, loggingEnabled && shouldLog(request), secretToken(request))
      .flatMap { response =>
        response.status match {
          case Status.Ok => action
          case _ => Future.value(response)
        }
      }

  private def shouldLog(request: HandlerRequest): Boolean =
    request.headerMap.get("Range").forall(RangeHelper.isRequestingFirstByte)

  private def secretToken(request: HandlerRequest): Option[String] =
    Option(request.getParam("secret_token"))
}
