package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.publicApiStrangler.service.media.{DownloadNotFound, DownloadOk, DownloadService}
import com.soundcloud.publicApiStrangler.support.TrackUrnUtil.getTrackUrn
import com.soundcloud.publicApiStrangler.support.{ErrorResponse, RangeHelper}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, Return, Throw, Try}

class TrackDownloadHandler(
    userAuthentication: UserAuthentication,
    downloadService: DownloadService
) {
  def handle(request: HandlerRequest): Future[Response] = {
    Try(getTrackUrn(request)) match {
      case Return(urn) =>
        userAuthentication.withUserSession(request) { session =>
          val secretToken = request.params.get("secret_token")
          downloadService.download(session, urn, secretToken, skipLogging(request)).map {
            case DownloadOk(url) => ResponseBuilder().header("Location", url).status(Status.Found).build
            case DownloadNotFound => ResponseBuilder.notFound()
          }
        }
      case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
    }
  }

  // If a client uses this endpoint with Range HTTP header download quota will be incremented
  // only if the first bytes of the request are being sent
  private def skipLogging(request: HandlerRequest): Boolean =
    !request.headerMap.get("Range").forall(RangeHelper.isRequestingFirstByte)
}
