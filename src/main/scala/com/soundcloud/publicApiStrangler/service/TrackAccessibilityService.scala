package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.playlists.PlaylistsClient
import com.soundcloud.publicApiStrangler.client.trackmetadata.Track
import com.twitter.util.Future

class TrackAccessibilityService(playlistsClient: PlaylistsClient) {
  def areTracksAccessible(session: UserSession, tracks: List[Track]): Future[Map[Urn, Boolean]] = {
    Future.collect(tracks.map(track => isTrackAccessible(session, None, track).map((track.urn, _)))).map(_.toMap)
  }

  def isTrackAccessible(session: UserSession, secretTokenInRequest: Option[String], track: Track): Future[Boolean] = {
    lazy val isPrivacyAuthorized = {
      if (track.public || track.user_urn == session.getUser) Future.True
      else isAccessGrantedViaSecretToken
    }

    lazy val isAccessGrantedViaSecretToken = {
      secretTokenInRequest match {
        case None => Future.False
        case Some(secretToken) => {
          if (secretToken.equals(track.secret_token)) Future.True
          else
            playlistsClient
              .getPlaylistContainingTrackOwnedByUser(track.urn, track.user_urn)
              .map(_.exists(playlist => playlist.secretToken == secretToken && playlist.userUrn == track.user_urn))
        }
      }
    }

    val isDisabled = track.disabled_at.isDefined

    if (!isDisabled) isPrivacyAuthorized
    else Future.False
  }
}
