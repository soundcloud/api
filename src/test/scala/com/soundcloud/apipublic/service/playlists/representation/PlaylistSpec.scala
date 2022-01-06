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
        likesCount = 0,
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

    val playlist =
      Playlist.fromVisiblePlaylist(
        visiblePlaylist,
        defaultUser,
        Some(defaultLabel),
        Some(Urn.parse(visiblePlaylist.userUrn).get),
        1L
      )
  }

  "fields are mapped correctly from a visible playlist" in new Context {
    override val playlist =
      Playlist.fromVisiblePlaylist(
        visiblePlaylist,
        defaultUser,
        Some(defaultLabel),
        Some(Urn("soundcloud", "users", "1")),
        1L
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
    playlist.uri ==== "https://api.soundcloud.com/playlists/42703821?secret_token=secret"
    playlist.user ==== defaultUser
    playlist.tracks ==== None
    playlist.secretUri ==== None
    playlist.secretToken ==== None
    playlist.likesCount ==== 1L
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
    playlist.secretUri ==== Some("https://api.soundcloud.com/playlists/42703821?secret_token=secret")
  }

  "Json representation has correct track_uri with secret_token" in new Context {
    val trackRepresentation = createTrackRepresentationFromVisibleTrack()
    val enrichedPlaylist = Playlist.enrichPlaylistWithTracks(playlist, List(trackRepresentation))

    val json = Json.toJson(enrichedPlaylist)
    (json \ "tracks_uri").as[String] ==== "https://api.soundcloud.com/playlists/42703821/tracks?secret_token=secret"
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
        1L
      )
    val enrichedPlaylist = Playlist.enrichPlaylistWithTracks(playlist, List(trackRepresentation))

    val json = Json.toJson(enrichedPlaylist)
    (json \ "tracks_uri").as[String] ==== "https://api.soundcloud.com/playlists/42703821/tracks"
  }
}
