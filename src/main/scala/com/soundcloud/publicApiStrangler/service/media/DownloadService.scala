package com.soundcloud.publicApiStrangler.service.media

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.tracks.{DownloadRequest, DownloadUrlResponse, TracksClient}
import com.twitter.util.Future

trait DownloadOriginalResponse

case class DownloadOk(url: String) extends DownloadOriginalResponse

case object DownloadNotFound extends DownloadOriginalResponse

class DownloadService(tracksClient: TracksClient) {
  def download(
      session: UserSession,
      trackUrn: Urn,
      secretToken: Option[String],
      skipLogging: Boolean
  ): Future[DownloadOriginalResponse] = {
    val downloadRequest = DownloadRequest(trackUrn, secretToken, skipLogging)

    tracksClient.downloadUrl(session, downloadRequest).map {
      case DownloadUrlResponse(url) => DownloadOk(url)
      case _ => DownloadNotFound
    }
  }
}
