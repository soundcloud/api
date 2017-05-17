package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.representation.Track
import play.api.libs.json.JsValue

object TrackMapper {
  def apply(json: JsValue): Track = {
    new Track(
      urn = Urn((json \ "self" \ "urn").as[String]),
      user_urn = Urn((json \ "user" \ "urn").as[String]),
      api_streamable = (json \ "api_streamable").asOpt[Boolean],
      artwork_url = (json \ "artwork_url").asOpt[String],
      bucket = (json \ "bucket").asOpt[String],
      comments_count = (json \ "comments_count").asOpt[Int],
      commentable = (json \ "commentable").as[Boolean],
      created_at = (json \ "created_at").as[String],
      description = (json \ "description").asOpt[String],
      disabled_at = (json \ "disabled_at").asOpt[String],
      disabled_reason = (json \ "disabled_reason").asOpt[String],
      downloads_count = (json \ "downloads_count").asOpt[Int],
      download_url = (json \ "download_url").asOpt[String],
      downloadable = (json \ "downloadable").asOpt[Boolean],
      duration = (json \ "duration").asOpt[Int].get,
      embeddable = (json \ "embeddable").asOpt[Boolean],
      embeddable_by = (json \ "embeddable_by").asOpt[String],
      feedable = (json \ "feedable").asOpt[Boolean],
      genre = (json \ "genre").asOpt[String],
      geo_blocking = (json \ "geo_blocking").asOpt[Boolean],
      isrc = (json \ "isrc").asOpt[String],
      label_id = (json \ "label_id").asOpt[Int],
      label_name = (json \ "label_name").asOpt[String],
      last_modified = (json \ "last_modified").as[String],
      license = (json \ "license").asOpt[String],
      favoritings_count = (json \ "favoritings_count").asOpt[Int],
      original_artwork_url = (json \ "original_artwork_url").asOpt[String],
      original_content_size = (json \ "original_content_size").asOpt[Int],
      original_format = (json \ "original_format").asOpt[String],
      permalink = (json \ "permalink").as[String],
      permalink_url = (json \ "permalink_url").as[String],
      playback_count = (json \ "playback_count").asOpt[Int],
      public = (json \ "public").as[Boolean],
      release_date = (json \ "release_date").asOpt[String],
      release_day = (json \ "release_day").asOpt[Int],
      release_month = (json \ "release_month").asOpt[Int],
      release_year = (json \ "release_year").asOpt[Int],
      reposts_count = (json \ "reposts_count").asOpt[Int],
      reveal_comments = (json \ "reveal_comments").asOpt[Boolean],
      reveal_stats = (json \ "reveal_stats").asOpt[Boolean],
      secret_token = (json \ "secret_token").asOpt[String],
      sharing = (json \ "sharing").as[String],
      state = (json \ "state").as[String],
      stream_url = (json \ "stream_url").asOpt[String],
      streamable = (json \ "streamable").as[Boolean],
      tag_list = (json \ "tag_list").asOpt[String],
      title = (json \ "title").as[String],
      track_type = (json \ "track_type").asOpt[String],
      uid = (json \ "uid").asOpt[String],
      updated_at = (json \ "updated_at").as[String],
      uri = (json \ "uri").as[String],
      waveform_url = (json \ "waveform_url").as[String],
      has_downloads_left = (json \ "has_downloads_left").asOpt[Boolean].getOrElse(false)
    )
  }
}
