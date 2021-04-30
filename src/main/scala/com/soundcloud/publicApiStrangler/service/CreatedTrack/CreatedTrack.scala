package com.soundcloud.publicApiStrangler.service.CreatedTrack

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.UserRepresentation
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorTrack
import play.api.libs.json.{Json, Writes}

case class CreatedTrack(trackCoordinatorTrack: TrackCoordinatorTrack, user: UserRepresentation, agentUrn: Option[Urn]) {
  def location = trackCoordinatorTrack.uri

  private def secretToken =
    if (trackCoordinatorTrack.public) None else trackCoordinatorTrack.secret_token

  private def uri: Option[String] =
    if (trackCoordinatorTrack.public) Some(trackCoordinatorTrack.uri) else secretUri

  private def secretUri: Option[String] =
    if (trackCoordinatorTrack.public) None else Some(s"$location?$secretTokenParam")

  private def downloadUrl: String =
    if (trackCoordinatorTrack.public) trackCoordinatorTrack.download_url
    else s"${trackCoordinatorTrack.download_url}?$secretTokenParam"

  private def streamUrl: String =
    if (trackCoordinatorTrack.public) trackCoordinatorTrack.stream_url
    else s"${trackCoordinatorTrack.stream_url}?$secretTokenParam"

  private def permalinkUrl: String =
    if (trackCoordinatorTrack.public) {
      trackCoordinatorTrack.permalink_url
    } else if (agentUrn.contains(CreatedTrack.AbletonLiveApplication)) {
      // TODO: remove this workaround once better solution for Ableton integration is found: https://jira.soundcloud.org/browse/INT-279
      trackCoordinatorTrack.permalink_url
    } else {
      s"${trackCoordinatorTrack.permalink_url}/${secretToken.getOrElse("")}"
    }

  private def secretTokenParam = s"secret_token=${secretToken.getOrElse("")}"
}

object CreatedTrack {
  private val AbletonLiveApplication = Urn("soundcloud", "applications", "45176")

  implicit val writes: Writes[CreatedTrack] = Writes[CreatedTrack] { t =>
    Json.obj(
      "artwork_url" -> t.trackCoordinatorTrack.artwork_url,
      "bpm" -> t.trackCoordinatorTrack.bpm,
      "comment_count" -> t.trackCoordinatorTrack.comment_count,
      "commentable" -> t.trackCoordinatorTrack.commentable,
      "created_at" -> t.trackCoordinatorTrack.created_at,
      "description" -> t.trackCoordinatorTrack.description,
      "download_count" -> t.trackCoordinatorTrack.downloads_count,
      "download_url" -> t.downloadUrl,
      "downloadable" -> t.trackCoordinatorTrack.downloadable,
      "duration" -> t.trackCoordinatorTrack.duration,
      "embeddable_by" -> t.trackCoordinatorTrack.embeddable_by,
      "favoritings_count" -> t.trackCoordinatorTrack.favoritings_count,
      "genre" -> t.trackCoordinatorTrack.genre,
      "id" -> Urn.parse(t.trackCoordinatorTrack.urn).get.identifier.toLong,
      "isrc" -> t.trackCoordinatorTrack.isrc,
      "key_signature" -> t.trackCoordinatorTrack.key_signature,
      "kind" -> "track",
      "label_id" -> t.trackCoordinatorTrack.label_id,
      "label_name" -> t.trackCoordinatorTrack.label_name,
      "last_modified" -> t.trackCoordinatorTrack.last_modified,
      "license" -> t.trackCoordinatorTrack.license,
      "original_content_size" -> t.trackCoordinatorTrack.original_content_size,
      "original_format" -> t.trackCoordinatorTrack.original_format,
      "permalink_url" -> t.permalinkUrl,
      "permalink" -> t.trackCoordinatorTrack.permalink,
      "playback_count" -> t.trackCoordinatorTrack.playback_count,
      "purchase_title" -> t.trackCoordinatorTrack.purchase_title,
      "purchase_url" -> t.trackCoordinatorTrack.purchase_url,
      "release" -> t.trackCoordinatorTrack.release,
      "release_day" -> t.trackCoordinatorTrack.release_day,
      "release_month" -> t.trackCoordinatorTrack.release_month,
      "release_year" -> t.trackCoordinatorTrack.release_year,
      "secret_token" -> t.secretToken,
      "secret_uri" -> t.secretUri,
      "sharing" -> t.trackCoordinatorTrack.sharing,
      "state" -> t.trackCoordinatorTrack.state,
      "stream_url" -> t.streamUrl,
      "streamable" -> t.trackCoordinatorTrack.api_streamable,
      "tag_list" -> t.trackCoordinatorTrack.tag_list,
      "title" -> t.trackCoordinatorTrack.title,
      "track_type" -> t.trackCoordinatorTrack.track_type,
      "uri" -> t.uri,
      "user" -> t.user,
      "user_favorite" -> false,
      "user_id" -> Urn.parse(t.trackCoordinatorTrack.user_urn).get.identifier.toLong,
      "user_playback_count" -> t.trackCoordinatorTrack.user_playback_count,
      "video_url" -> t.trackCoordinatorTrack.video_url,
      "waveform_url" -> t.trackCoordinatorTrack.waveform_url
    )
  }
}
