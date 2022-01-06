package com.soundcloud.apipublic.service.resolve

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.Routing
import com.soundcloud.apipublic.client.mothership.MoshimoshiClient
import com.soundcloud.apipublic.client.tracks.TrackRequest
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.TrackVisibilityService.TrackWithTranscodingsFieldMask
import com.soundcloud.apipublic.service.playlists.PlaylistRequest
import com.soundcloud.apipublic.service.resolve.ResourceURLs.PermalinkURL
import com.soundcloud.apipublic.service.{PlaylistsService, TrackVisibilityService}
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
            val preservedQueryParams = ResourceURLs.queryParams(url, permalink.secretToken)
            urn.collection match {
              case "users" => Future.value(Some(buildUserUrl(urn.identifier, preservedQueryParams)))
              case "tracks" => fetchAndBuildTrackUrl(session, permalink, urn, preservedQueryParams)
              case "playlists" => fetchAndBuildPlaylistUrl(session, permalink, urn, preservedQueryParams)
              case _ => Future.value(None)
            }
          case _ => Future.value(None)
        }
      case _ => Future.value(None)
    }
  }

  private def fetchAndBuildTrackUrl(
      session: UserSession,
      permalink: PermalinkURL,
      urn: Urn,
      preservedQueryParams: String
  ): Future[Option[String]] = {
    val trackRequest = TrackRequest(urn, permalink.secretToken)
    trackVisibilityService
      .visibleTracks(session, List(trackRequest), TrackWithTranscodingsFieldMask, AccessParams.explicitAccess)
      .map { tracks =>
        if (tracks.nonEmpty) Some(buildTrackUrl(urn.identifier, preservedQueryParams)) else None
      }
  }

  private def fetchAndBuildPlaylistUrl(
      session: UserSession,
      permalink: PermalinkURL,
      urn: Urn,
      preservedQueryParams: String
  ): Future[Option[String]] = {
    val playlistRequest = PlaylistRequest(urn, permalink.secretToken)
    playlistsService
      .fetchPlaylistsMetadataOnly(session, List(playlistRequest))
      .map { playlists =>
        if (playlists.nonEmpty) Some(buildPlaylistUrl(urn.identifier, preservedQueryParams))
        else None
      }
  }

  private def buildUserUrl(urnIdentifier: String, queryParams: String): String = {
    s"$baseUrl${Routing.userIdPath.replace(":id", urnIdentifier)}" + queryParams
  }

  private def buildTrackUrl(urnIdentifier: String, queryParams: String): String = {
    val base = s"$baseUrl${Routing.trackIdPath.replace(":trackId", urnIdentifier)}"
    base + queryParams
  }

  private def buildPlaylistUrl(urnIdentifier: String, queryParams: String): String = {
    val base = s"$baseUrl${Routing.playlistIdPath.replace(":id", urnIdentifier)}"
    base + queryParams
  }
}
