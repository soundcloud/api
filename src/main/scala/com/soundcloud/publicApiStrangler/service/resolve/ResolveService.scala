package com.soundcloud.publicApiStrangler.service.resolve

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.client.mothership.MoshimoshiClient
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.handler.support.requestParser.AccessParams
import com.soundcloud.publicApiStrangler.service.TrackVisibilityService.TrackWithTranscodingsFieldMask
import com.soundcloud.publicApiStrangler.service.playlists.PlaylistRequest
import com.soundcloud.publicApiStrangler.service.resolve.ResourceURLs.PermalinkURL
import com.soundcloud.publicApiStrangler.service.{PlaylistsService, TrackVisibilityService}
import com.twitter.util.Future

class ResolveService(
    moshimoshiClient: MoshimoshiClient,
    trackVisibilityService: TrackVisibilityService,
    playlistsService: PlaylistsService,
    baseUrl: String
) {

  def resolveUrl(session: UserSession, url: String): Future[Option[String]] = {
    val maybePermalink = ResourceURLs.parsePermalinkUrl(url).toOption
    Future.value(maybePermalink).flatMap {
      case Some(permalink) =>
        moshimoshiClient.resolveToUrn(session, permalink.normalized).flatMap {
          case Some(urn) =>
            urn.collection match {
              case "users" => Future.value(Some(buildUserUrl(urn.identifier)))
              case "tracks" => fetchAndBuildTrackUrl(session, permalink, urn)
              case "playlists" => fetchAndBuildPlaylistUrl(session, permalink, urn)
              case _ => Future.value(None)
            }
          case _ => Future.value(None)
        }
      case _ => Future.value(None)
    }
  }

  private def fetchAndBuildTrackUrl(session: UserSession, permalink: PermalinkURL, urn: Urn): Future[Option[String]] = {
    val trackRequest = TrackRequest(urn, permalink.secretToken)
    trackVisibilityService
      .visibleTracks(session, List(trackRequest), TrackWithTranscodingsFieldMask, AccessParams.explicitAccess)
      .map { tracks =>
        if (tracks.nonEmpty) Some(buildTrackUrl(urn.identifier, permalink.secretToken)) else None
      }
  }

  private def fetchAndBuildPlaylistUrl(
      session: UserSession,
      permalink: PermalinkURL,
      urn: Urn
  ): Future[Option[String]] = {
    val playlistRequest = PlaylistRequest(urn, permalink.secretToken)
    playlistsService
      .fetchPlaylistsMetadataOnly(session, List(playlistRequest))
      .map { playlists =>
        if (playlists.nonEmpty) Some(buildPlaylistUrl(urn.identifier, permalink.secretToken)) else None
      }
  }

  private def buildUserUrl(urnIdentifier: String): String = {
    s"$baseUrl${Routing.userIdPath.replace(":id", urnIdentifier)}"
  }

  private def buildTrackUrl(urnIdentifier: String, secretToken: Option[String]): String = {
    val base = s"$baseUrl${Routing.trackIdPath.replace(":trackId", urnIdentifier)}"
    secretToken.map(token => s"$base?secret_token=$token").getOrElse(base)
  }

  private def buildPlaylistUrl(urnIdentifier: String, secretToken: Option[String]): String = {
    val base = s"$baseUrl${Routing.playlistIdPath.replace(":id", urnIdentifier)}"
    secretToken.map(token => s"$base?secret_token=$token").getOrElse(base)
  }
}
