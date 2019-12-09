package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.publicApiStrangler.TrackUrnUtil.trackUrn
import com.soundcloud.publicApiStrangler.service.media.{DownloadNotFound, DownloadOk, DownloadService}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, Return, Try}

class TrackDownloadHandler(
    userAuthentication: UserAuthentication,
    downloadService: DownloadService
) {
  def handle(request: HandlerRequest): Future[Response] = {
    Try(trackUrn(request)) match {
      case Return(urn) =>
        userAuthentication.withUserSession(request) { session =>
          val secretToken = request.params.get("secret_token")
          downloadService.download(session, urn, secretToken).map {
            case DownloadOk(url) => ResponseBuilder().header("Location", url).status(Status.Found).build
            case DownloadNotFound => ResponseBuilder.notFound()
          }
        }
      case _ => Future.value(ResponseBuilder.badRequest())
    }
  }
}
