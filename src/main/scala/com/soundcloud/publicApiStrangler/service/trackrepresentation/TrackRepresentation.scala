package com.soundcloud.publicApiStrangler.service.trackrepresentation

import com.soundcloud.publicApiStrangler.client.mothership.TrackAudioMetadata
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.User
import com.soundcloud.publicApiStrangler.client.pubmese.Isrc
import com.soundcloud.publicApiStrangler.client.tracks.VisibleTrack
import com.soundcloud.publicApiStrangler.support.HtmlSanitizer
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

import scala.collection.immutable.HashSet

case class TrackRepresentation(
    visibleTrack: VisibleTrack,
    user: User,
    isrc: Option[Isrc],
    label: Option[User],
    geoblockings: Option[HashSet[String]],
    playbackCount: Option[Int],
    downloadable: Boolean,
    downloadCount: Option[Int],
    downloadsRemaining: Option[Int],
    favoritingsCount: Option[Int],
    repostsCount: Option[Int],
    secretToken: Option[String],
    releaseDay: Option[Int],
    releaseMonth: Option[Int],
    uri: Option[String],
    streamUrl: Option[String],
    downloadUrl: Option[String],
    permalinkUrl: Option[String],
    secretUri: Option[String],
    commentCount: Option[Int],
    userFavourite: Option[Boolean],
    userPlaybackCount: Option[Int],
    audioMetadata: TrackAudioMetadata,
    waveformUrl: String
) {
  def id = visibleTrack.urn.identifier.toLong
}

object TrackRepresentation {
  private val dateTimeFormat = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss +0000")
  private val cdnRoot = "https://i1.sndcdn.com"

  implicit val writes = new Writes[TrackRepresentation] {

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

    override def writes(rep: TrackRepresentation): JsValue = {
      Json.obj(
        "kind" -> "track",
        "id" -> rep.id,
        "created_at" -> rep.visibleTrack.createdAt.toString(dateTimeFormat),
        "user_id" -> rep.user.urn.identifier.toLong,
        "duration" -> rep.visibleTrack.duration,
        "commentable" -> rep.visibleTrack.commentable,
        "comment_count" -> rep.commentCount,
        "state" -> rep.audioMetadata.state,
        "original_content_size" -> rep.audioMetadata.original_content_size,
        "last_modified" -> rep.visibleTrack.lastModified.toString(dateTimeFormat),
        "sharing" -> (if (rep.visibleTrack.public) "public" else "private"),
        "tag_list" -> mkTagList(rep),
        "permalink" -> rep.visibleTrack.permalink,
        "streamable" -> rep.visibleTrack.apiStreamable,
        "embeddable_by" -> rep.visibleTrack.embeddableBy,
        "purchase_url" -> rep.visibleTrack.purchaseUrl,
        "purchase_title" -> rep.visibleTrack.purchaseTitle.map(HtmlSanitizer.sanitize),
        "label_id" -> rep.visibleTrack.labelId,
        "genre" -> rep.visibleTrack.genre.map(HtmlSanitizer.sanitize),
        "title" -> HtmlSanitizer.sanitize(rep.visibleTrack.title),
        "description" -> rep.visibleTrack.description.map(HtmlSanitizer.sanitize),
        "label_name" -> rep.visibleTrack.labelName.map(HtmlSanitizer.sanitize),
        "release" -> rep.visibleTrack.release.map(HtmlSanitizer.sanitize),
        "track_type" -> rep.visibleTrack.trackType.map(HtmlSanitizer.sanitize),
        "key_signature" -> rep.visibleTrack.keySignature.map(HtmlSanitizer.sanitize),
        "isrc" -> rep.isrc.map(_.toString),
        "video_url" -> rep.visibleTrack.videoUrl,
        "bpm" -> rep.visibleTrack.bpm.map(roundBpm),
        "release_year" -> rep.visibleTrack.releaseYear,
        "release_month" -> rep.releaseMonth,
        "release_day" -> rep.releaseDay,
        "original_format" -> rep.audioMetadata.original_format,
        "license" -> rep.visibleTrack.license,
        "uri" -> rep.uri,
        "user" -> rep.user,
        "user_uri" -> s"https://api.soundcloud.com/users/${rep.user.urn.identifier}",
        "permalink_url" -> rep.permalinkUrl,
        "artwork_url" -> rep.visibleTrack.artwork.filename.map(imageUrl),
        "stream_url" -> rep.streamUrl,
        "download_url" -> rep.downloadUrl,
        "waveform_url" -> rep.waveformUrl,
        "domain_lockings" -> None,
        "available_country_codes" -> rep.geoblockings,
        "label" -> rep.label,
        "secret_token" -> rep.secretToken,
        "secret_uri" -> rep.secretUri,
        "user_favorite" -> rep.userFavourite,
        "user_playback_count" -> rep.userPlaybackCount,
        "playback_count" -> rep.playbackCount,
        "download_count" -> rep.downloadCount,
        "favoritings_count" -> rep.favoritingsCount,
        "reposts_count" -> rep.repostsCount,
        "downloadable" -> rep.downloadable,
        "downloads_remaining" -> rep.downloadsRemaining,
        "access" -> rep.visibleTrack.access.map(_.name)
      )

    }

    private def roundBpm(f: Double): Double =
      (f * 10000.0).round.toDouble / 10000.0

    private def mkTagList(rep: TrackRepresentation): String =
      (rep.visibleTrack.machineTags ++ rep.visibleTrack.userTags).map(quoteTagIfNecessary _).mkString(" ")

    private def quoteTagIfNecessary(tag: String): String =
      if (tag.exists(_.isSpaceChar))
        "\"" + tag + "\""
      else
        tag

    private def imageUrl(imageFile: String): String = {
      val s3FilenamePattern = """(.*)-original\.\w*""".r

      imageFile match {
        case s3FilenamePattern(s3filename) => cdnRoot + s"/$s3filename-large.jpg"
        case _ => cdnRoot + "/" + imageFile
      }
    }
  }
}
