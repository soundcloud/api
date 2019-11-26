package com.soundcloud.publicApiStrangler.service.media

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.policies.ContentPolicy
import com.soundcloud.publicApiStrangler.client.media.MediaServiceClient
import com.soundcloud.publicApiStrangler.client.tracks.{TracksClient, VisibleTrack}
import com.twitter.util.Future

trait DownloadResponse

case class DownloadOk(url: String) extends DownloadResponse

case object DownloadNotFound extends DownloadResponse

class DownloadService(tracksClient: TracksClient, mediaServiceClient: MediaServiceClient) {
  def download(session: UserSession, trackUrn: Urn, secretToken: Option[String]): Future[DownloadResponse] = {
    tracksClient.visibleTrack(session, trackUrn, secretToken).flatMap { maybeTrack =>
      (for {
        track <- maybeTrack
        uid <- track.uid
      } yield {
        if (downloadingAllowed(track, session.getUser)) {
          mediaServiceClient.fetchDownloadOriginalUrl(session, uid).map {
            case Some(url) => DownloadOk(url)
            case _ => DownloadNotFound
          }
        } else Future.value(DownloadNotFound)
      }).getOrElse(Future.value(DownloadNotFound))
    }
  }

  private def downloadingAllowed(track: VisibleTrack, userUrn: Urn): Boolean =
    track.disabledAt.isEmpty &&
      (track.userUrn == userUrn || (track.downloadable && (track.authorization.policy == ContentPolicy.ALLOW || track.authorization.policy == ContentPolicy.MONETIZE)))
}
