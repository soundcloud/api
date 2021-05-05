package com.soundcloud.publicApiStrangler.service.trackrepresentation

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.authorization.policies.Access
import com.soundcloud.publicApiStrangler.client.mothership.TrackAudioMetadata
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.UserRepresentation
import com.soundcloud.publicApiStrangler.client.pubmese.Isrc
import com.soundcloud.publicApiStrangler.client.tracks.EmbeddingPermission
import com.soundcloud.publicApiStrangler.support.HtmlSanitizer
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

import scala.collection.immutable.HashSet

case class TrackRepresentation(
    urn: Urn,
    createdAt: LocalDateTime,
    duration: Int,
    lastModified: LocalDateTime,
    public: Boolean,
    userTags: Option[String],
    machineTags: Option[String],
    permalink: String,
    apiStreamable: Option[Boolean],
    embeddableBy: EmbeddingPermission,
    purchaseUrl: Option[String],
    purchaseTitle: Option[String],
    labelId: Option[Long],
    genre: Option[String],
    title: String,
    description: Option[String],
    labelName: Option[String],
    release: Option[String],
    trackType: Option[String],
    keySignature: Option[String],
    isrc: Option[Isrc],
    videoUrl: Option[String],
    bpm: Option[Double],
    releaseYear: Option[Int],
    releaseDay: Option[Int],
    releaseMonth: Option[Int],
    license: String,
    uri: Option[String],
    user: UserRepresentation,
    permalinkUrl: Option[String],
    artworkUrl: Option[String],
    streamUrl: Option[String],
    downloadUrl: Option[String],
    waveformUrl: String,
    availableCountries: Option[HashSet[String]],
    label: Option[UserRepresentation],
    secretToken: Option[String],
    secretUri: Option[String],
    userFavourite: Option[Boolean],
    userPlaybackCount: Option[Int],
    playbackCount: Option[Int],
    downloadCount: Option[Int],
    commentCount: Option[Int],
    favoritingsCount: Option[Int],
    repostsCount: Option[Int],
    commentable: Boolean,
    downloadable: Boolean,
    downloadsRemaining: Option[Int],
    access: Option[Access],
    policy: Option[String],
    monetizationModel: Option[String],
    audioMetadata: TrackAudioMetadata
) {
  def id = urn.identifier.toLong
}

object TrackRepresentation {
  val dateTimeFormat = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss +0000")

  implicit val writes = new Writes[TrackRepresentation] {

    implicit val userWrites = Writes[UserRepresentation] { user =>
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
        "created_at" -> rep.createdAt.toString(dateTimeFormat),
        "user_id" -> rep.user.urn.identifier.toLong,
        "duration" -> rep.duration,
        "commentable" -> rep.commentable,
        "comment_count" -> rep.commentCount,
        "state" -> rep.audioMetadata.state,
        "original_content_size" -> rep.audioMetadata.original_content_size,
        "last_modified" -> rep.lastModified.toString(dateTimeFormat),
        "sharing" -> (if (rep.public) "public" else "private"),
        "tag_list" -> (rep.machineTags ++ rep.userTags).mkString(" "),
        "permalink" -> rep.permalink,
        "streamable" -> rep.apiStreamable,
        "embeddable_by" -> rep.embeddableBy,
        "purchase_url" -> rep.purchaseUrl,
        "purchase_title" -> rep.purchaseTitle.map(HtmlSanitizer.sanitize),
        "label_id" -> rep.labelId,
        "genre" -> rep.genre.map(HtmlSanitizer.sanitize),
        "title" -> HtmlSanitizer.sanitize(rep.title),
        "description" -> rep.description.map(HtmlSanitizer.sanitize),
        "label_name" -> rep.labelName.map(HtmlSanitizer.sanitize),
        "release" -> rep.release.map(HtmlSanitizer.sanitize),
        "track_type" -> rep.trackType.map(HtmlSanitizer.sanitize),
        "key_signature" -> rep.keySignature.map(HtmlSanitizer.sanitize),
        "isrc" -> rep.isrc.map(_.toString),
        "video_url" -> rep.videoUrl,
        "bpm" -> rep.bpm.map(roundBpm),
        "release_year" -> rep.releaseYear,
        "release_month" -> rep.releaseMonth,
        "release_day" -> rep.releaseDay,
        "original_format" -> rep.audioMetadata.original_format,
        "license" -> rep.license,
        "uri" -> rep.uri,
        "user" -> rep.user,
        "user_uri" -> s"https://api.soundcloud.com/users/${rep.user.urn.identifier}",
        "permalink_url" -> rep.permalinkUrl,
        "artwork_url" -> rep.artworkUrl,
        "stream_url" -> rep.streamUrl,
        "download_url" -> rep.downloadUrl,
        "waveform_url" -> rep.waveformUrl,
        "domain_lockings" -> None,
        "available_country_codes" -> rep.availableCountries,
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
        "access" -> rep.access.map(_.name),
        "policy" -> rep.policy, // empty unless requested by allowlisted client
        "monetization_model" -> rep.monetizationModel // empty unless requested by allowlisted client
      )
    }

    private def roundBpm(f: Double): Double = (f * 10000.0).round.toDouble / 10000.0
  }
}
