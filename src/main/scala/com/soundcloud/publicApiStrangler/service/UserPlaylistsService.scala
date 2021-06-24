package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.chrono.ChronoItem
import com.soundcloud.publicApiStrangler.client.mothership.MoshimoshiClient
import com.soundcloud.publicApiStrangler.handler.support.requestParser.AccessParams
import com.soundcloud.publicApiStrangler.service.pagination.{CursorBasedPagination, OffsetBasedPagination}
import com.soundcloud.publicApiStrangler.service.playlists.PlaylistRequest
import com.soundcloud.publicApiStrangler.service.playlists.representation.Playlist
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.twitter.util.Future

class UserPlaylistsService(
    playlistsService: PlaylistsService,
    moshimoshiClient: MoshimoshiClient
) {

  def userPlaylists(
      session: UserSession,
      userUrn: Urn,
      access: AccessParams,
      pagination: CursorBasedPagination,
      showTracks: Option[Boolean]
  ): Future[Collection[Playlist]] = {
    for {
      userPlaylistsResponse <- moshimoshiClient.userPlaylists(session, userUrn, pagination)
      playlists <- playlistsService.fetchPlaylists(
        session,
        userPlaylistsResponse.items.map(item => PlaylistRequest(item.urn, None)),
        access,
        None,
        showTracks
      )
    } yield {
      Collection(playlists, nextHref(userPlaylistsResponse.items, pagination))
    }
  }

  def userPlaylist(
      session: UserSession,
      playlistUrn: Urn,
      secretToken: Option[String],
      pagination: Option[OffsetBasedPagination],
      userId: String,
      access: AccessParams,
      showTracks: Option[Boolean]
  ): Future[Outcome[Playlist]] = {
    playlistsService.fetchPlaylist(session, playlistUrn, secretToken, access, pagination, showTracks).map {
      case Good(playlist) if playlist.userId == userId.toLong => playlist.good
      case _ => NotFound("playlist not found").bad
    }
  }

  private def nextHref(items: List[ChronoItem], pagination: CursorBasedPagination): Option[String] = {
    if (items.nonEmpty) {
      Some(
        pagination
          .nextPage(items.last.cursor)
          .normalizedHref
      )
    } else {
      None
    }
  }
}
