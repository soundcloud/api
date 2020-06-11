package com.soundcloud.publicApiStrangler.service.trackrepresentation

import com.soundcloud.publicApiStrangler.client.mothership.{DomainLocking, TrackAudioMetadata}
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.User
import com.soundcloud.publicApiStrangler.client.pubmese.Isrc
import com.soundcloud.publicApiStrangler.client.trackmetadata.Track
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

import scala.collection.immutable.HashSet

case class TrackRepresentation(
    track: Track,
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
    domainlockings: Option[Seq[DomainLocking]],
    waveformUrl: String
) {
  def id = track.urn.identifier.toLong
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
        "created_at" -> rep.track.created_at.toString(dateTimeFormat),
        "user_id" -> rep.user.urn.identifier.toLong,
        "duration" -> rep.track.duration,
        "commentable" -> rep.track.commentable,
        "comment_count" -> rep.commentCount,
        "state" -> rep.audioMetadata.state,
        "original_content_size" -> rep.audioMetadata.original_content_size,
        "last_modified" -> rep.track.last_modified.toString(dateTimeFormat),
        "sharing" -> (if (rep.track.public) "public" else "private"),
        "tag_list" -> mkTagList(rep),
        "permalink" -> rep.track.permalink,
        "streamable" -> rep.track.api_streamable,
        "embeddable_by" -> rep.track.embeddableBy,
        "purchase_url" -> rep.track.purchase_url,
        "purchase_title" -> rep.track.purchase_title,
        "label_id" -> rep.track.label_id,
        "genre" -> rep.track.genre,
        "title" -> rep.track.title,
        "description" -> rep.track.description,
        "label_name" -> rep.track.label_name,
        "release" -> rep.track.release,
        "track_type" -> rep.track.track_type,
        "key_signature" -> rep.track.key_signature,
        "isrc" -> rep.isrc.map(_.toString),
        "video_url" -> rep.track.video_url,
        "bpm" -> rep.track.bpm.map(roundBpm(_)),
        "release_year" -> rep.track.release_year,
        "release_month" -> rep.releaseMonth,
        "release_day" -> rep.releaseDay,
        "original_format" -> rep.audioMetadata.original_format,
        "license" -> rep.track.license,
        "uri" -> rep.uri,
        "user" -> rep.user,
        "permalink_url" -> rep.permalinkUrl,
        "artwork_url" -> rep.track.artwork.filename.map(imageUrl(_)),
        "stream_url" -> rep.streamUrl,
        "download_url" -> rep.downloadUrl,
        "waveform_url" -> rep.waveformUrl,
        "domain_lockings" -> rep.domainlockings.map(_.map(domainLocking => Json.obj("domain" -> domainLocking.domain))),
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
        "downloads_remaining" -> rep.downloadsRemaining
      )

    }

    private def roundBpm(f: Double): Double =
      (f * 10000.0).round.toDouble / 10000.0

    private def mkTagList(rep: TrackRepresentation): String =
      (rep.track.machine_tags ++ rep.track.user_tags).map(quoteTagIfNecessary _).mkString(" ")

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
