package com.soundcloud.apipublic.service.playlists.representation

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.service.trackrepresentation.TrackRepresentationSpecContext
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.libs.json.Json

import java.time.{LocalDateTime, ZoneOffset}

class PlaylistSpec extends Specification with TrackRepresentationSpecContext {

  trait Context extends Scope {
    val playlistUrn = Urn("soundcloud", "playlists", "42703821")
    val application = Urn("soundcloud", "applications", "2")
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
        artworkUrl = Some("http://some.url/link"),
        trackCount = 7,
        userTags = List("retro", "vintage"),
        releaseDate = Some(playlistReleaseInstant),
        public = false,
        sharing = "",
        secretToken = Some("secret"),
        updatedAt = None,
        userUrn = userUrn.toString,
        isAlbum = false,
        setType = "mix",
        managedByFeeds = false,
        publishedAt = None,
        embeddableBy = "",
        license = None,
        labelName = None,
        labelId = None,
        purchaseTitle = None,
        purchaseUrl = None,
        ean = Some("7641825109894"),
        streamable = Some(false),
        uri = "https://api.soundcloud.com/playlists/soundcloud:playlists:42703821?secret_token=secret",
        trackRequests = PlaylistTrackRequests(requests = List.empty, pagination = None),
        counts = Some(PlaylistCounts(Some(2), Some(3)))
      )

    val playlist =
      Playlist.fromVisiblePlaylist(
        visiblePlaylist,
        defaultUser,
        Some(defaultLabel),
        Some(Urn.parse(visiblePlaylist.userUrn).get),
        Some(application)
      )
  }

  "fields are mapped correctly from a visible playlist" in new Context {
    override val playlist =
      Playlist.fromVisiblePlaylist(
        visiblePlaylist,
        defaultUser,
        Some(defaultLabel),
        Some(Urn("soundcloud", "users", "1")),
        Some(application)
      )

    playlist.title ==== "playlist mix"
    playlist.id ==== 42703821
    playlist.urn ==== Urn("soundcloud", "playlists", "42703821")
    playlist.userId ==== 3456
    playlist.userUrn ==== Urn("soundcloud", "users", "3456")
    playlist.description ==== Some("cool playlist")
    playlist.createdAt ==== Some(playlistReleaseInstant)
    playlist.duration ==== 120
    playlist.genre ==== "metal"
    playlist.permalinkUrl ==== "http://soundcloud.com/some-random-link/secret?utm_medium=api&utm_campaign=social_sharing&utm_source=id_2"
    playlist.permalink ==== "some-random-link"
    playlist.artworkUrl ==== Some("http://some.url/link")
    playlist.trackCount ==== 7
    playlist.tagList ==== "\"retro\" \"vintage\""
    playlist.releaseDay ==== Some(19)
    playlist.releaseMonth ==== Some(8)
    playlist.releaseYear ==== Some(2013)
    playlist.embeddableBy ==== ""
    playlist.downloadable === None
    playlist.ean ==== Some("7641825109894")
    playlist.streamable ==== Some(false)
    playlist.uri ==== "https://api.soundcloud.com/playlists/soundcloud:playlists:42703821?secret_token=secret"
    playlist.user ==== defaultUser
    playlist.tracks ==== None
    playlist.secretUri ==== None
    playlist.secretToken ==== None
    playlist.likesCount ==== 2L
  }

  "downloadable is set to true if all tracks are downloadable" in new Context {
    val downloadableTrack = createTrackRepresentationFromVisibleTrack().copy(downloadable = true)
    val enrichedPlaylist = Playlist.enrichPlaylistWithTracks(playlist, List(downloadableTrack))

    enrichedPlaylist.downloadable ==== Some(true)
  }

  "tracks are present when playlist is enriched" in new Context {
    val downloadableTrack = createTrackRepresentationFromVisibleTrack()
    val enrichedPlaylist = Playlist.enrichPlaylistWithTracks(playlist, List(downloadableTrack))

    enrichedPlaylist.tracks ==== Some(List(downloadableTrack))
  }

  "tracks are present when playlist is not enriched" in new Context {
    playlist.tracks must beEmpty
  }

  "tracks are empty when playlist is enriched but no tracks are provided" in new Context {
    val enrichedPlaylist = Playlist.enrichPlaylistWithTracks(playlist, List.empty)

    enrichedPlaylist.tracks ==== Some(List.empty)
  }

  "owner only fields are added when requesting user owns playlist" in new Context {

    playlist.secretToken ==== Some("secret")
    playlist.secretUri ==== Some(
      "https://api.soundcloud.com/playlists/soundcloud:playlists:42703821?secret_token=secret"
    )
  }

  "Json representation has correct track_uri with secret_token" in new Context {
    val trackRepresentation = createTrackRepresentationFromVisibleTrack()
    val enrichedPlaylist = Playlist.enrichPlaylistWithTracks(playlist, List(trackRepresentation))

    val json = Json.toJson(enrichedPlaylist)
    (json \ "tracks_uri")
      .as[String] ==== "https://api.soundcloud.com/playlists/soundcloud:playlists:42703821/tracks?secret_token=secret"
  }

  "Json representation has correct track_uri without secret_token" in new Context {
    val trackRepresentation = createTrackRepresentationFromVisibleTrack()
    val visiblePlaylistNoSecret = visiblePlaylist.copy(secretToken = None)

    override val playlist =
      Playlist.fromVisiblePlaylist(
        visiblePlaylistNoSecret,
        defaultUser,
        Some(defaultLabel),
        Some(Urn.parse(visiblePlaylist.userUrn).get),
        Some(application)
      )
    val enrichedPlaylist = Playlist.enrichPlaylistWithTracks(playlist, List(trackRepresentation))

    val json = Json.toJson(enrichedPlaylist)
    (json \ "tracks_uri").as[String] ==== "https://api.soundcloud.com/playlists/soundcloud:playlists:42703821/tracks"
  }

  "returns 0 likes_count if not returned from VisiblePlaylist" in new Context {
    val visiblePlaylistWithoutCounts = visiblePlaylist.copy(counts = None)

    override val playlist =
      Playlist.fromVisiblePlaylist(
        visiblePlaylistWithoutCounts,
        defaultUser,
        Some(defaultLabel),
        Some(Urn.parse(visiblePlaylist.userUrn).get),
        Some(application)
      )

    playlist.likesCount ==== 0
  }
}
