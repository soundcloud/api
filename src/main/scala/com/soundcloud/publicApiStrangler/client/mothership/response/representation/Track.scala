package com.soundcloud.publicApiStrangler.client.mothership.response.representation

import com.soundcloud.jvmkit.module.util.Urn

case class TracksWithPagination(tracks: List[Track], meta: TrackMeta)

case class TrackMeta(cursor: Option[TrackCursor])

case class TrackCursor(nextHref: String, after: Int, limit: Int)

class Track(
    val urn: Urn,
    val user_urn: Urn,
    val api_streamable: Option[Boolean],
    val artwork_url: Option[String],
    val bucket: Option[String],
    val commentable: Boolean,
    val comments_count: Option[Int],
    val created_at: String,
    val description: Option[String],
    val disabled_at: Option[String],
    val disabled_reason: Option[String],
    val download_url: Option[String],
    val downloadable: Option[Boolean],
    val downloads_count: Option[Int],
    val duration: Int,
    val embeddable: Option[Boolean],
    val embeddable_by: Option[String],
    val favoritings_count: Option[Int],
    val feedable: Option[Boolean],
    val genre: Option[String],
    val geo_blocking: Option[Boolean],
    val isrc: Option[String],
    val label_id: Option[Int],
    val label_name: Option[String],
    val last_modified: String,
    val license: Option[String],
    val original_artwork_url: Option[String],
    val original_content_size: Option[Int],
    val original_format: Option[String],
    val permalink: String,
    val permalink_url: String,
    val playback_count: Option[Int],
    val public: Boolean,
    val release_date: Option[String],
    val release_day: Option[Int],
    val release_month: Option[Int],
    val release_year: Option[Int],
    val reposts_count: Option[Int],
    val reveal_comments: Option[Boolean],
    val reveal_stats: Option[Boolean],
    val secret_token: Option[String],
    val sharing: String,
    val state: String,
    val stream_url: Option[String],
    val streamable: Boolean,
    val tag_list: Option[String],
    val title: String,
    val track_type: Option[String],
    val uid: Option[String],
    val updated_at: String,
    val uri: String,
    val waveform_url: String,
    val has_downloads_left: Boolean
)
