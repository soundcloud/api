package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.service.media.{DownloadNotFound, DownloadOk, DownloadService, TrackAccessRecorderService}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future

class TrackDownloadHandler(userAuthentication: UserAuthentication,
                           mothershipDispatcher: DispatchToMothershipHandler,
                           trackAccessRecorderService: TrackAccessRecorderService,
                           downloadService: DownloadService,
                           mediaServiceEnabled: () => Future[Boolean]) {

  def handle(request: HandlerRequest): Future[Response] = {
    mediaServiceEnabled().flatMap {
      case false => mothershipDispatcher.dispatch(request)
      case true => handleWithMediaService(request)
    }
  }

  private def handleWithMediaService(request: HandlerRequest): Future[Response] = {
    request.routeParams.get("trackId").map { trackId =>
      userAuthentication.withUserSession(request) { session =>
        val trackUrn = Urn("soundcloud", "tracks", trackId)
        val secretToken = request.params.get("secret_token")
        trackAccessRecorderService.recordDownloadAccess(session, request, trackUrn) {
          downloadService.download(session, trackUrn, secretToken).map {
            case DownloadOk(url) => ResponseBuilder().header("Location", url).status(Status.Found).build
            case DownloadNotFound => ResponseBuilder.notFound()
          }
        }
      }
    }.getOrElse(Future.value(ResponseBuilder.badRequest()))
  }
}
