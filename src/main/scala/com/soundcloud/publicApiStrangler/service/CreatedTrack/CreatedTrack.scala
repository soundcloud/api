package com.soundcloud.publicApiStrangler.service.CreatedTrack

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorTrack
import play.api.libs.json.{Json, Writes}

case class CreatedTrack(trackCoordinatorTrack: TrackCoordinatorTrack) {
  def location = trackCoordinatorTrack.uri
}

object CreatedTrack {
  implicit val writes: Writes[CreatedTrack] = Writes[CreatedTrack] { t =>
    val secretToken =
      if (t.trackCoordinatorTrack.public) None else t.trackCoordinatorTrack.secret_token
    Json.obj(
      "artwork_url" -> t.trackCoordinatorTrack.artwork_url,
      "commentable" -> t.trackCoordinatorTrack.commentable,
      "created_at" -> t.trackCoordinatorTrack.created_at,
      "description" -> t.trackCoordinatorTrack.description,
      "download_count" -> t.trackCoordinatorTrack.downloads_count,
      "download_url" -> t.trackCoordinatorTrack.download_url,
      "downloadable" -> t.trackCoordinatorTrack.downloadable,
      "duration" -> t.trackCoordinatorTrack.duration,
      "embeddable_by" -> t.trackCoordinatorTrack.embeddable_by,
      "favoritings_count" -> t.trackCoordinatorTrack.favoritings_count,
      "genre" -> t.trackCoordinatorTrack.genre,
      "id" -> Urn.parse(t.trackCoordinatorTrack.urn).get.identifier.toLong,
      "isrc" -> t.trackCoordinatorTrack.isrc,
      "kind" -> "track",
      "label_id" -> t.trackCoordinatorTrack.label_id,
      "label_name" -> t.trackCoordinatorTrack.label_name,
      "last_modified" -> t.trackCoordinatorTrack.last_modified,
      "license" -> t.trackCoordinatorTrack.license,
      "original_content_size" -> t.trackCoordinatorTrack.original_content_size,
      "original_format" -> t.trackCoordinatorTrack.original_format,
      "permalink_url" -> t.trackCoordinatorTrack.permalink_url,
      "permalink" -> t.trackCoordinatorTrack.permalink,
      "playback_count" -> t.trackCoordinatorTrack.playback_count,
      "purchase_title" -> t.trackCoordinatorTrack.purchase_title,
      "purchase_url" -> t.trackCoordinatorTrack.purchase_url,
      "release_day" -> t.trackCoordinatorTrack.release_day,
      "release_month" -> t.trackCoordinatorTrack.release_month,
      "release_year" -> t.trackCoordinatorTrack.release_year,
      "secret_token" -> secretToken,
      "sharing" -> t.trackCoordinatorTrack.sharing,
      "state" -> t.trackCoordinatorTrack.state,
      "stream_url" -> t.trackCoordinatorTrack.stream_url,
      "streamable" -> t.trackCoordinatorTrack.streamable,
      "tag_list" -> t.trackCoordinatorTrack.tag_list,
      "title" -> t.trackCoordinatorTrack.title,
      "track_type" -> t.trackCoordinatorTrack.track_type,
      "uri" -> t.trackCoordinatorTrack.uri,
      "user_id" -> Urn.parse(t.trackCoordinatorTrack.user_urn).get.identifier.toLong,
      "waveform_url" -> t.trackCoordinatorTrack.waveform_url
    )
  }
}
