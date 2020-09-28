package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.client.chrono.{ChronoItem, ChronoMeta, ChronoMetaParams, ChronoResponse}
import com.soundcloud.publicApiStrangler.client.mothership.MoshimoshiClient
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.playlists.{PlaylistBuilder, PlaylistRequest}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.ParamMap
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when

class UserPlaylistsServiceSpec extends UnitSpecification {
  trait Context extends Scope {
    val playlist = new PlaylistBuilder().build
    val moshimoshiClient = mock[MoshimoshiClient]
    val playlistService = mock[PlaylistsService]
    val userPlaylistsService = new UserPlaylistsService(playlistService, moshimoshiClient)
    val session = loggedInSession(playlist.user.urn)
    val pagination = CursorBasedPagination(
      "https://api.soundcloud.com",
      "/users/1/playlists/",
      ParamMap(),
      Some("2"),
      2
    )
    val playlistUrn = Urn("soundcloud", "playlists", playlist.id.toString)
    val chronoResponse = ChronoResponse(
      items = List(ChronoItem("", "playlists", playlistUrn, "2")),
      meta = ChronoMeta(
        params = ChronoMetaParams(
          cursor = None,
          limit = 0,
          direction = "desc"
        ),
        validForCaching = false
      )
    )
  }

  "#userPlaylists" >> {
    "when all data is available" in new Context {
      when(moshimoshiClient.userPlaylists(session, playlist.user.urn, pagination))
        .thenReturn(Future.value(chronoResponse))
      when(playlistService.fetchPlaylists(session, List(PlaylistRequest(playlistUrn, None)), None))
        .thenReturn(Future.value(List(playlist)))

      val playlistsCollection = Await.result(userPlaylistsService.userPlaylists(session, playlist.user.urn, pagination))

      playlistsCollection.items ==== List(playlist)
      playlistsCollection.nextHref ==== Some("https://api.soundcloud.com/users/1/playlists/?cursor=2&page_size=2")
    }

    "when data is not available" in new Context {
      when(moshimoshiClient.userPlaylists(session, playlist.user.urn, pagination))
        .thenReturn(Future.value(ChronoResponse.emptyResponse))
      when(playlistService.fetchPlaylists(session, List.empty, None))
        .thenReturn(Future.value(List.empty))

      val playlistsCollection = Await.result(userPlaylistsService.userPlaylists(session, playlist.user.urn, pagination))

      playlistsCollection.items ==== List.empty
      playlistsCollection.nextHref ==== None
    }
  }

  "#userPlaylist" >> {
    "when all data is available" in new Context {
      when(playlistService.fetchPlaylist(session, playlistUrn, Some("secr3t-Token"), None))
        .thenReturn(Future.value(Good(playlist)))

      val result =
        Await.result(
          userPlaylistsService
            .userPlaylist(session, playlistUrn, Some("secr3t-Token"), None, playlist.userId.toString)
        )

      result match {
        case Good(playlist) =>
          playlist.id ==== playlistUrn.identifier.toLong
          playlist.userId ==== playlist.userId
        case _ => failure(s"returned ${result.toString} instead of Good(_)")
      }
    }

    "when data is not available" in new Context {
      when(playlistService.fetchPlaylist(session, playlistUrn, Some("secr3t-Token"), None))
        .thenReturn(Future.value(NotFound("playlist not found").bad))

      val result =
        Await.result(
          userPlaylistsService
            .userPlaylist(session, playlistUrn, Some("secr3t-Token"), None, session.getUser.identifier)
        )

      result ==== Bad(NotFound("playlist not found"))
    }
  }
}
