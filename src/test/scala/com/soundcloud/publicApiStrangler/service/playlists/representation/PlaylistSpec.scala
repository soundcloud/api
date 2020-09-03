package com.soundcloud.publicApiStrangler.service.playlists.representation

import java.time.{LocalDateTime, ZoneOffset}

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackRepresentationSpecContext
import org.specs2.mutable.Specification

class PlaylistSpec extends Specification with TrackRepresentationSpecContext {
  val playlistUrn = Urn("soundcloud", "playlists", "42703821")
  val playlistTitle = "my playlist"
  val playlistReleaseInstant = LocalDateTime.of(2013, 8, 19, 2, 29, 15).toInstant(ZoneOffset.UTC)
  val visiblePlaylist =
    VisiblePlaylist(
      urn = playlistUrn.toString,
      title = "playlist mix",
      description = Some("cool playlist"),
      createdAt = Some(playlistReleaseInstant),
      duration = 120,
      genre = "metal",
      permalinkUrl = "http://soundcloud.com/some-random-link",
      permalink = "some-random-link",
      artworkUrl = "http://some.url/link",
      trackCount = 7,
      userTags = List("retro", "vintage"),
      releaseDate = Some(playlistReleaseInstant),
      public = false,
      sharing = "",
      secretToken = Some("secret"),
      updatedAt = None,
      userUrn = userUrn.toString,
      likesCount = 1,
      isAlbum = false,
      setType = "mix",
      managedByFeeds = false,
      repostsCount = 0,
      publishedAt = None,
      embeddableBy = "",
      license = None,
      labelName = None,
      labelId = None,
      purchaseTitle = None,
      purchaseUrl = None,
      cursor = None,
      ean = Some("7641825109894"),
      streamable = Some(false),
      uri = "https://api.soundcloud.com/playlists/42703821?secret_token=secret",
      trackRequests = PlaylistTrackRequests(requests = List.empty, pagination = None)
    )

  "#fromVisiblePlaylist" >> {
    "fields are mapped correctly" in {
      val trackRepresentation = createTrackRepresentation()
      val playlist =
        Playlist.fromVisiblePlaylist(
          visiblePlaylist,
          List(trackRepresentation),
          defaultUser,
          Some(defaultLabel),
          Some(Urn("soundcloud", "users", "1"))
        )

      playlist.title ==== "playlist mix"
      playlist.id ==== 42703821
      playlist.userId ==== 3456
      playlist.description ==== Some("cool playlist")
      playlist.createdAt ==== Some(playlistReleaseInstant)
      playlist.duration ==== 120
      playlist.genre ==== "metal"
      playlist.permalinkUrl ==== "http://soundcloud.com/some-random-link/secret"
      playlist.permalink ==== "some-random-link"
      playlist.artworkUrl ==== "http://some.url/link"
      playlist.trackCount ==== 7
      playlist.tagList ==== "\"retro\" \"vintage\""
      playlist.releaseDay ==== Some(19)
      playlist.releaseMonth ==== Some(8)
      playlist.releaseYear ==== Some(2013)
      playlist.embeddableBy ==== ""
      playlist.downloadable === None
      playlist.ean ==== Some("7641825109894")
      playlist.streamable ==== Some(false)
      playlist.uri ==== "https://api.soundcloud.com/playlists/42703821?secret_token=secret"
      playlist.user ==== defaultUser
      playlist.tracks ==== List(trackRepresentation)
      playlist.secretUri ==== None
      playlist.secretToken ==== None
    }

    "downloadable is set to true if all tracks are downloadable" in {
      val downloadableTrack = createTrackRepresentation().copy(downloadable = true)
      val playlist =
        Playlist.fromVisiblePlaylist(
          visiblePlaylist,
          List(downloadableTrack),
          defaultUser,
          Some(defaultLabel),
          Some(Urn("soundcloud", "users", "1"))
        )

      playlist.downloadable ==== Some(true)
    }

    "owner only fields are added when requesting user owns playlist" in {
      val downloadableTrack = createTrackRepresentation().copy(downloadable = true)
      val playlist =
        Playlist.fromVisiblePlaylist(
          visiblePlaylist,
          List(downloadableTrack),
          defaultUser,
          Some(defaultLabel),
          Some(Urn.parse(visiblePlaylist.userUrn).get)
        )

      playlist.secretToken ==== Some("secret")
      playlist.secretUri ==== Some("https://api.soundcloud.com/playlists/42703821?secret_token=secret")
    }
  }
}
