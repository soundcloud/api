package com.soundcloud.publicApiStrangler.service.playlists.representation

import java.net.URLEncoder
import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneOffset}

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.User
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackRepresentation
import play.api.libs.json._

case class Playlist(
    title: String,
    id: Long,
    duration: Long,
    userId: Long,
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
    label: Option[User],
    likesCount: Long,
    tagList: String,
    releaseYear: Option[Int],
    trackCount: Long,
    lastModified: Option[Instant],
    license: Option[String],
    playlistType: String,
    downloadable: Option[Boolean],
    sharing: String,
    createdAt: Option[Instant],
    release: Option[String],
    purchaseTitle: Option[String],
    artworkUrl: String,
    ean: Option[String],
    streamable: Option[Boolean],
    embeddableBy: String,
    labelId: Option[String],
    user: User,
    tracks: Option[List[TrackRepresentation]],
    secretUri: Option[String],
    secretToken: Option[String]
)

object Playlist {
  val formatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("yyyy/MM/dd HH:mm:ss +0000")
    .withZone(ZoneOffset.UTC)

  implicit val playlistWrites = Writes[Playlist] { playlist =>
    val tracks = playlist.tracks.map(tracks => Json.obj("tracks" -> Json.toJson(tracks))).getOrElse(Json.obj())

    var playlistJson = Json.obj(
      "duration" -> playlist.duration,
      "genre" -> playlist.genre,
      "release_day" -> playlist.releaseDay,
      "permalink" -> playlist.permalink,
      "permalink_url" -> playlist.permalinkUrl,
      "release_month" -> playlist.releaseMonth,
      "release_year" -> playlist.releaseYear,
      "description" -> playlist.description,
      "uri" -> playlist.uri,
      "label_name" -> playlist.labelName,
      "label_id" -> playlist.labelId.map(id => id.toInt),
      "label" -> playlist.label,
      "tag_list" -> playlist.tagList,
      "track_count" -> playlist.trackCount,
      "user_id" -> playlist.userId,
      "last_modified" -> playlist.lastModified.map(formatter.format(_)),
      "license" -> playlist.license,
      "user" -> Json.toJson(playlist.user),
      "playlist_type" -> playlist.playlistType,
      "type" -> playlist.playlistType,
      "id" -> playlist.id,
      "downloadable" -> playlist.downloadable,
      "likes_count" -> playlist.likesCount,
      "sharing" -> playlist.sharing,
      "created_at" -> playlist.createdAt.map(formatter.format(_)),
      "release" -> playlist.release,
      "tags" -> playlist.tagList,
      "kind" -> playlist.kind,
      "title" -> playlist.title,
      "purchase_title" -> playlist.purchaseTitle,
      "ean" -> playlist.ean,
      "streamable" -> playlist.streamable,
      "embeddable_by" -> playlist.embeddableBy,
      "artwork_url" -> playlist.artworkUrl,
      "purchase_url" -> playlist.purchaseUrl,
      "tracks_uri" -> s"${playlist.uri}/tracks"
    )

    playlist.secretToken.foreach(token => playlistJson = playlistJson ++ Json.obj("secret_token" -> token))
    playlist.secretUri.foreach(uri => playlistJson = playlistJson ++ Json.obj("secret_uri" -> uri))
    playlistJson = playlistJson ++ tracks
    playlistJson
  }

  def fromVisiblePlaylist(
      playlist: VisiblePlaylist,
      playlistTracks: List[TrackRepresentation],
      playlistOwner: User,
      maybeLabel: Option[User],
      requestingUserUrn: Option[Urn],
      showTracks: Boolean
  ): Playlist = {
    val releaseDay =
      playlist.releaseDate.map(date => LocalDateTime.ofInstant(date, ZoneOffset.UTC).getDayOfMonth)
    val releaseMonth =
      playlist.releaseDate.map(date => LocalDateTime.ofInstant(date, ZoneOffset.UTC).getMonth.getValue)
    val releaseYear = playlist.releaseDate.map(date => LocalDateTime.ofInstant(date, ZoneOffset.UTC).getYear)
    val tracks = if (showTracks) Some(playlistTracks) else None

    Playlist(
      title = playlist.title,
      id = Urn.parse(playlist.urn).get.identifier.toLong,
      duration = playlist.duration,
      userId = Urn.parse(playlist.userUrn).get.identifier.toLong,
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
      downloadable = if (playlistTracks.forall(track => track.downloadable)) Some(true) else None,
      sharing = playlist.sharing,
      createdAt = playlist.createdAt,
      release = None,
      purchaseTitle = playlist.purchaseTitle,
      artworkUrl = playlist.artworkUrl,
      ean = playlist.ean,
      streamable = playlist.streamable,
      likesCount = playlist.likesCount,
      embeddableBy = playlist.embeddableBy,
      labelId = playlist.labelId,
      labelName = playlist.labelName,
      label = maybeLabel,
      purchaseUrl = playlist.purchaseUrl,
      user = playlistOwner,
      tracks = tracks,
      secretToken = requestingUserUrn.flatMap(ownerUrn =>
        if (!playlist.public && playlist.userUrn == ownerUrn.toString) playlist.secretToken else None
      ),
      secretUri = requestingUserUrn.flatMap(ownerUrn =>
        if (!playlist.public && playlist.userUrn == ownerUrn.toString) Some(playlist.uri) else None
      )
    )
  }

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
}
