package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.chrono.ChronoItem
import com.soundcloud.publicApiStrangler.client.mothership.MoshimoshiClient
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
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
      pagination: CursorBasedPagination
  ): Future[Collection[Playlist]] = {
    for {
      userPlaylistsResponse <- moshimoshiClient.userPlaylists(session, userUrn, pagination)
      playlists <- playlistsService.fetchPlaylists(
        session,
        userPlaylistsResponse.items.map(item => PlaylistRequest(item.urn, None)),
        None
      )
    } yield {
      Collection(playlists, nextHref(userPlaylistsResponse.items, pagination))
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
