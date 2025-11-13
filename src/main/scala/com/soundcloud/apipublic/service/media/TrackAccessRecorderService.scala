package com.soundcloud.apipublic.service.media

import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.client.media.TrackAccessRecorderClient
import com.soundcloud.apipublic.support.RangeHelper
import com.twitter.finagle.http.{Method, Response, Status}
import com.twitter.util.Future

class TrackAccessRecorderService(trackAccessRecorderClient: TrackAccessRecorderClient) {
  def recordStreamAccess(session: UserSession, request: HandlerRequest, trackUrn: Urn)(
      action: => Future[Response]
  ): Future[Response] =
    recordAccess(session, request, trackUrn, "stream", request.method == Method.Get, action)

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
          case Status.Unauthorized | Status.Forbidden =>
            Future.value(ResponseBuilder().status(Status.NotFound).build)
          case _ => Future.value(response)
        }
      }

  private def shouldLog(request: HandlerRequest): Boolean =
    request.headerMap.get("Range").forall(RangeHelper.isRequestingFirstByte)

  private def secretToken(request: HandlerRequest): Option[String] =
    Option(request.getParam("secret_token"))
}
