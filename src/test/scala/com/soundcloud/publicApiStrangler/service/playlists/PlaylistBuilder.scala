package com.soundcloud.publicApiStrangler.service.playlists

import java.time.Instant

import com.soundcloud.publicApiStrangler.client.mothership.response.representation.User
import com.soundcloud.publicApiStrangler.service.playlists.representation.Playlist
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackRepresentation
import com.soundcloud.publicApiStrangler.service.users.UserBuilder

class PlaylistBuilder {
  private val defaultUser = new UserBuilder().build

  private var title: String = "my playlist"
  private var id: Long = 123L
  private var duration: Long = 5000L
  private var userId: Long = 1212L
  private var kind: String = "playlist"
  private var releaseDay: Option[Int] = Some(1)
  private var permalinkUrl: String = "https://soundcloud.com/user1212/my-playlist"
  private var genre: String = "techno"
  private var permalink: String = "my-playlist"
  private var purchaseUrl: Option[String] = Some("http://test.api/purchase")
  private var releaseMonth: Option[Int] = Some(1)
  private var description: Option[String] = Some("best playlist ever")
  private var uri: String = "https://soundcloud.com/playlists/123"
  private var labelName: Option[String] = Some("red eye records")
  private var label: Option[User] = None
  private var tagList: String = "beats techno slapper"
  private var releaseYear: Option[Int] = Some(1)
  private var trackCount: Long = 12L
  private var lastModified: Option[Instant] = Some(Instant.now())
  private var license: Option[String] = Some("li-ce")
  private var playlistType: String = "set"
  private var downloadable: Option[Boolean] = Some(true)
  private var sharing: String = "public"
  private var createdAt: Option[Instant] = Some(Instant.now())
  private var release: Option[String] = Some("")
  private var purchaseTitle: Option[String] = Some("my playlist")
  private var artworkUrl: Option[String] = Some("artwork.jpeg")
  private var ean: Option[String] = Some("1ae")
  private var streamable: Option[Boolean] = Some(true)
  private var embeddableBy: String = "all"
  private var labelId: Option[String] = None
  private var user: User = defaultUser
  private var tracks: Option[List[TrackRepresentation]] = None
  private var secretUri: Option[String] = Some("https://soundcloud.com/user1212/my-playlist?secret_token=s3creT")
  private var secretToken: Option[String] = Some("s3creT")
  private var likesCount: Long = 808

  def setTitle(value: String) = { title = value; this }
  def setId(value: Long) = { id = value; this }
  def setDuration(value: Long) = { duration = value; this }
  def setUserid(value: Long) = { userId = value; this }
  def setKind(value: String) = { kind = value; this }
  def setReleaseDay(value: Option[Int]) = { releaseDay = value; this }
  def setPermalinkUrl(value: String) = { permalinkUrl = value; this }
  def setGenre(value: String) = { genre = value; this }
  def setPermalink(value: String) = { permalink = value; this }
  def setReleaseMonth(value: Option[Int]) = { releaseMonth = value; this }
  def setDescription(value: Option[String]) = { description = value; this }
  def setUri(value: String) = { uri = value; this }
  def setLabelName(value: Option[String]) = { labelName = value; this }
  def setLabel(value: Option[User]) = { label = value; this }
  def setTagList(value: String) = { tagList = value; this }
  def setReleaseYear(value: Option[Int]) = { releaseYear = value; this }
  def setTrackCount(value: Long) = { trackCount = value; this }
  def setLastModified(value: Option[Instant]) = { lastModified = value; this }
  def setLicense(value: Option[String]) = { license = value; this }
  def setPlaylistType(value: String) = { playlistType = value; this }
  def setDownloadable(value: Option[Boolean]) = { downloadable = value; this }
  def setSharing(value: String) = { sharing = value; this }
  def setCreatedAt(value: Option[Instant]) = { createdAt = value; this }
  def setRelease(value: Option[String]) = { release = value; this }
  def setPurchaseTitle(value: Option[String]) = { purchaseTitle = value; this }
  def setArtworkUrl(value: Option[String]) = { artworkUrl = value; this }
  def setEan(value: Option[String]) = { ean = value; this }
  def setStreamable(value: Option[Boolean]) = { streamable = value; this }
  def setEmbeddableBy(value: String) = { embeddableBy = value; this }
  def setLabelId(value: Option[String]) = { labelId = value; this }
  def setUser(value: User) = { user = value; this }
  def setTracks(value: Option[List[TrackRepresentation]]) = { tracks = value; this }
  def setSecretUri(value: Option[String]) = { secretUri = value; this }
  def setSecretToken(value: Option[String]) = { secretToken = value; this }
  def setPurchaseUrl(value: Option[String]) = { purchaseUrl = value; this }
  def setLikesCount(value: Long) = { likesCount = value; this }

  def build: Playlist = {
    Playlist(
      title = this.title,
      id = this.id,
      duration = this.duration,
      userId = this.userId,
      kind = this.kind,
      releaseDay = this.releaseDay,
      permalinkUrl = this.permalinkUrl,
      genre = this.genre,
      permalink = this.permalink,
      purchaseUrl = this.purchaseUrl,
      releaseMonth = this.releaseMonth,
      description = this.description,
      uri = this.uri,
      labelName = this.labelName,
      label = this.label,
      tagList = this.tagList,
      releaseYear = this.releaseYear,
      trackCount = this.trackCount,
      lastModified = this.lastModified,
      license = this.license,
      playlistType = this.playlistType,
      downloadable = this.downloadable,
      sharing = this.sharing,
      createdAt = this.createdAt,
      release = this.release,
      purchaseTitle = this.purchaseTitle,
      artworkUrl = this.artworkUrl,
      ean = this.ean,
      streamable = this.streamable,
      embeddableBy = this.embeddableBy,
      labelId = this.labelId,
      user = this.user,
      tracks = this.tracks,
      secretUri = this.secretUri,
      secretToken = this.secretToken,
      likesCount = this.likesCount
    )
  }
}
