package com.soundcloud.apipublic.service.playlists.representation

import com.soundcloud.apipublic.client.mothership.response.representation.UserRepresentation
import com.soundcloud.apipublic.service.trackrepresentation.TrackRepresentation
import com.soundcloud.apipublic.support.HtmlSanitizer
import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json._

import java.net.URLEncoder
import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneOffset}

case class Playlist(
    title: String,
    urn: Urn,
    userUrn: Urn,
    duration: Long,
    kind: String,
    releaseDay: Option[Int],
    permalinkUrl: String,
    genre: String,
    permalink: String,
    purchaseUrl: Option[String],
    releaseMonth: Option[Int],
    description: Option[String],
    uri: String,
    labelName: Option[String],
    label: Option[UserRepresentation],
    likesCount: Long,
    tagList: String,
    releaseYear: Option[Int],
    trackCount: Long,
    lastModified: Option[Instant],
    license: Option[String],
    playlistType: String,
    downloadable: Option[Boolean] = None,
    sharing: String,
    createdAt: Option[Instant],
    release: Option[String],
    purchaseTitle: Option[String],
    artworkUrl: Option[String],
    ean: Option[String],
    streamable: Option[Boolean],
    embeddableBy: String,
    labelId: Option[String],
    user: UserRepresentation,
    tracks: Option[List[TrackRepresentation]] = None,
    secretUri: Option[String],
    secretToken: Option[String]
) {
  def id: Long = urn.identifier.toLong
  def userId: Long = user.urn.identifier.toLong
}

object Playlist {
  val formatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("yyyy/MM/dd HH:mm:ss +0000")
    .withZone(ZoneOffset.UTC)

  implicit val playlistWrites = Writes[Playlist] { playlist =>
    val tracks = playlist.tracks.map(tracks => Json.obj("tracks" -> Json.toJson(tracks))).getOrElse(Json.obj())

    var playlistJson = Json.obj(
      "duration" -> playlist.duration,
      "genre" -> HtmlSanitizer.sanitize(playlist.genre),
      "release_day" -> playlist.releaseDay,
      "permalink" -> playlist.permalink,
      "permalink_url" -> playlist.permalinkUrl,
      "release_month" -> playlist.releaseMonth,
      "release_year" -> playlist.releaseYear,
      "description" -> playlist.description.map(HtmlSanitizer.sanitize),
      "uri" -> playlist.uri,
      "label_name" -> playlist.labelName.map(HtmlSanitizer.sanitize),
      "label_id" -> playlist.labelId.map(id => id.toInt),
      "label" -> playlist.label,
      "tag_list" -> playlist.tagList,
      "track_count" -> playlist.trackCount,
      "user_id" -> playlist.userId,
      "user_urn" -> playlist.userUrn.toString,
      "last_modified" -> playlist.lastModified.map(formatter.format(_)),
      "license" -> playlist.license,
      "user" -> Json.toJson(playlist.user),
      "playlist_type" -> playlist.playlistType,
      "type" -> playlist.playlistType,
      "id" -> playlist.id,
      "urn" -> playlist.urn.toString,
      "downloadable" -> playlist.downloadable,
      "likes_count" -> playlist.likesCount,
      "sharing" -> playlist.sharing,
      "created_at" -> playlist.createdAt.map(formatter.format(_)),
      "release" -> playlist.release.map(HtmlSanitizer.sanitize),
      "tags" -> playlist.tagList,
      "kind" -> playlist.kind,
      "title" -> HtmlSanitizer.sanitize(playlist.title),
      "purchase_title" -> playlist.purchaseTitle.map(HtmlSanitizer.sanitize),
      "ean" -> playlist.ean,
      "streamable" -> playlist.streamable,
      "embeddable_by" -> playlist.embeddableBy,
      "artwork_url" -> playlist.artworkUrl,
      "purchase_url" -> playlist.purchaseUrl,
      "tracks_uri" -> trackUri(playlist)
    )

    playlist.secretToken.foreach(token => playlistJson = playlistJson ++ Json.obj("secret_token" -> token))
    playlist.secretUri.foreach(uri => playlistJson = playlistJson ++ Json.obj("secret_uri" -> uri))
    playlistJson = playlistJson ++ tracks
    playlistJson
  }

  def fromVisiblePlaylist(
      playlist: VisiblePlaylist,
      playlistOwner: UserRepresentation,
      maybeLabel: Option[UserRepresentation],
      requestingUserUrn: Option[Urn]
  ): Playlist = {
    val releaseDay =
      playlist.releaseDate.map(date => LocalDateTime.ofInstant(date, ZoneOffset.UTC).getDayOfMonth)
    val releaseMonth =
      playlist.releaseDate.map(date => LocalDateTime.ofInstant(date, ZoneOffset.UTC).getMonth.getValue)
    val releaseYear = playlist.releaseDate.map(date => LocalDateTime.ofInstant(date, ZoneOffset.UTC).getYear)

    Playlist(
      title = playlist.title,
      urn = Urn.parse(playlist.urn).get,
      duration = playlist.duration,
      userUrn = Urn.parse(playlist.userUrn).get,
      kind = "playlist",
      genre = playlist.genre,
      releaseDay = releaseDay,
      releaseMonth = releaseMonth,
      releaseYear = releaseYear,
      permalink = playlist.permalink,
      permalinkUrl = createFullPermalinkUrl(playlist),
      description = playlist.description,
      uri = playlist.uri,
      tagList = getTagList(playlist.userTags),
      trackCount = playlist.trackCount,
      lastModified = playlist.updatedAt,
      license = playlist.license,
      playlistType = playlist.setType,
      sharing = playlist.sharing,
      createdAt = playlist.createdAt,
      release = None,
      purchaseTitle = playlist.purchaseTitle,
      artworkUrl = playlist.artworkUrl,
      ean = playlist.ean,
      streamable = playlist.streamable,
      likesCount = playlist.counts.flatMap(_.likes).getOrElse(0),
      embeddableBy = playlist.embeddableBy,
      labelId = playlist.labelId,
      labelName = playlist.labelName,
      label = maybeLabel,
      purchaseUrl = playlist.purchaseUrl,
      user = playlistOwner,
      secretToken = requestingUserUrn.flatMap(ownerUrn =>
        if (!playlist.public && playlist.userUrn == ownerUrn.toString) playlist.secretToken else None
      ),
      secretUri = requestingUserUrn.flatMap(ownerUrn =>
        if (!playlist.public && playlist.userUrn == ownerUrn.toString) Some(playlist.uri) else None
      )
    )
  }

  def enrichPlaylistWithTracks(playlist: Playlist, playlistTracks: List[TrackRepresentation]): Playlist =
    playlist.copy(
      downloadable = if (playlistTracks.forall(track => track.downloadable)) Some(true) else None,
      tracks = Some(playlistTracks)
    )

  /*
   This method converts a list of the format [tag_a, tag_b] to a string like '"tag_a" "tag_b"'
   */
  private def getTagList(userTags: List[String]): String = {
    if (userTags.nonEmpty) userTags.mkString("\"", "\" \"", "\"") else ""
  }

  private def createFullPermalinkUrl(
      visiblePlaylist: VisiblePlaylist
  ): String = {
    if (!visiblePlaylist.public && visiblePlaylist.secretToken.isDefined) {
      val secret = URLEncoder.encode(visiblePlaylist.secretToken.get, "UTF-8")
      s"${visiblePlaylist.permalinkUrl}/$secret"
    } else {
      visiblePlaylist.permalinkUrl
    }
  }

  private val baseUrl = "https://api.soundcloud.com/playlists"

  private def trackUri(playlist: Playlist) = {
    secretUrl(s"$baseUrl/${playlist.urn}/tracks", playlist)
  }

  private def secretUrl(url: String, playlist: Playlist): String = {
    playlist.secretToken.map(token => s"$url?secret_token=$token").getOrElse(url)
  }
}
