package com.soundcloud.apipublic.service.media

import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.util.Future
import proto.soundcloud.tracks.api.{GetDownloadRequest, MediaService}

trait DownloadOriginalResponse

case class DownloadOk(url: String) extends DownloadOriginalResponse

case object DownloadNotFound extends DownloadOriginalResponse

class DownloadService(mediaService: MediaService) {
  def download(
      session: UserSession,
      trackUrn: Urn,
      secretToken: Option[String],
      skipLogging: Boolean
  ): Future[DownloadOriginalResponse] = {
    val request = GetDownloadRequest(
      userSession = Some(session.asProtoSession),
      urn = trackUrn.toString,
      secretToken = secretToken,
      skipLogging = Some(skipLogging)
    )

    mediaService
      .getDownload(request)
      .map(res => DownloadOk(url = res.url))
      .handle {
        case _ => DownloadNotFound
      }
  }
}
