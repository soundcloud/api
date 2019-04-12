package com.soundcloud.publicApiStrangler.service.media

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.media.TrackAccessRecorderClient
import com.soundcloud.publicApiStrangler.support.RangeHelper
import com.twitter.finagle.http.{Method, Response, Status}
import com.twitter.util.Future

class TrackAccessRecorderService(trackAccessRecorderClient: TrackAccessRecorderClient) {

  def recordStreamAccess(session: UserSession, request: HandlerRequest, trackUrn: Urn)(action: => Future[Response]): Future[Response] =
    recordAccess(session, request, trackUrn, "stream", action)

  def recordDownloadAccess(session: UserSession, request: HandlerRequest, trackUrn: Urn)(action: => Future[Response]): Future[Response] =
    recordAccess(session, request, trackUrn, "download", action)

  private def recordAccess(session: UserSession, request: HandlerRequest, trackUrn: Urn, accessFor: String, action: => Future[Response]): Future[Response] =
    trackAccessRecorderClient.recordAccess(session, trackUrn, accessFor, shouldLog(request), secretToken(request)).flatMap { response =>
      response.status match {
        case Status.Ok => action
        case _ => Future.value(response)
      }
    }

  private def shouldLog(request: HandlerRequest): Boolean =
    request.method == Method.Get &&
      request.headerMap.get("Range").forall(RangeHelper.isRequestingFirstByte)

  private def secretToken(request: HandlerRequest): Option[String] =
    Option(request.getParam("secret_token"))
}
