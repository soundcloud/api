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
    tracks: List[TrackRepresentation],
    secretUri: Option[String],
    secretToken: Option[String]
)

object Playlist {
  val formatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("yyyy/MM/dd HH:mm:ss +0000")
    .withZone(ZoneOffset.UTC)

  implicit val userWrites = Writes[User] { user =>
    Json.obj(
      "id" -> user.urn.identifier.toLong,
      "kind" -> "user",
      "permalink" -> user.permalink,
      "username" -> user.username,
      "last_modified" -> user.updated_at,
      "uri" -> s"https://api.soundcloud.com/users/${user.urn.identifier}",
      "permalink_url" -> user.permalink_url,
      "avatar_url" -> user.avatar_url.replaceAll("\\?[0-9]+$", "").replaceAll("^http:", "https:")
    )
  }

  implicit val playlistWrites = Writes[Playlist] { playlist =>
    val playlistJson = Json.obj(
      "duration" -> playlist.duration,
      "genre" -> playlist.genre,
      "release_day" -> playlist.releaseDay,
      "permalink" -> playlist.permalink,
      "permalink_url" -> playlist.permalinkUrl,
      "release_day" -> playlist.releaseDay,
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
      "tracks" -> Json.toJson(playlist.tracks),
      "user" -> Json.toJson(playlist.user),
      "playlist_type" -> playlist.playlistType,
      "type" -> playlist.playlistType,
      "id" -> playlist.id,
      "downloadable" -> playlist.downloadable,
      "sharing" -> playlist.sharing,
      "created_at" -> playlist.createdAt.map(formatter.format(_)),
      "release" -> playlist.release,
      "kind" -> playlist.kind,
      "title" -> playlist.title,
      "purchase_title" -> playlist.purchaseTitle,
      "ean" -> playlist.ean,
      "streamable" -> playlist.streamable,
      "embeddable_by" -> playlist.embeddableBy,
      "artwork_url" -> playlist.artworkUrl,
      "purchase_url" -> playlist.purchaseUrl
    )

    playlist.secretToken.map(token => playlistJson ++ Json.obj("secret_token" -> token))
    playlist.secretUri.map(uri => playlistJson ++ Json.obj("secret_uri" -> uri))
    playlistJson
  }

  def fromVisiblePlaylist(
      playlist: VisiblePlaylist,
      playlistTracks: List[TrackRepresentation],
      playlistOwner: User,
      maybeLabel: Option[User],
      requestingUserUrn: Option[Urn]
  ): Playlist = {
    val releaseDay =
      playlist.releaseDate.map(date => LocalDateTime.ofInstant(date, ZoneOffset.UTC).getDayOfMonth)
    val releaseMonth =
      playlist.releaseDate.map(date => LocalDateTime.ofInstant(date, ZoneOffset.UTC).getMonth.getValue)
    val releaseYear = playlist.releaseDate.map(date => LocalDateTime.ofInstant(date, ZoneOffset.UTC).getYear)

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
      embeddableBy = playlist.embeddableBy,
      labelId = playlist.labelId,
      labelName = playlist.labelName,
      label = maybeLabel,
      purchaseUrl = playlist.purchaseUrl,
      user = playlistOwner,
      tracks = playlistTracks,
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
