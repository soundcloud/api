package com.soundcloud.publicApiStrangler.service.media

import com.soundcloud.jvmkit.module.rollout.{Rollout, RolloutFeature}
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.tracks.{DownloadRequest, DownloadUrlResponse, TracksClient}
import com.twitter.util.Future
import proto.soundcloud.tracks.api.{GetDownloadRequest, MediaService}

trait DownloadOriginalResponse

case class DownloadOk(url: String) extends DownloadOriginalResponse

case object DownloadNotFound extends DownloadOriginalResponse

class DownloadService(tracksClient: TracksClient, mediaService: MediaService, rollout: Rollout) {
  val trackDownloadRollout = RolloutFeature("twirp-download-url")

  def download(
      session: UserSession,
      trackUrn: Urn,
      secretToken: Option[String],
      skipLogging: Boolean
  ): Future[DownloadOriginalResponse] = {

    rollout
      .isActive(trackDownloadRollout)
      .flatMap(isActive => {
        if (isActive) {
          downloadTwirp(session, trackUrn, secretToken, skipLogging)
        } else {
          val downloadRequest = DownloadRequest(trackUrn, secretToken, skipLogging)

          tracksClient.downloadUrl(session, downloadRequest).map {
            case DownloadUrlResponse(url) => DownloadOk(url)
            case _ => DownloadNotFound
          }
        }
      })
  }

  def downloadTwirp(
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
