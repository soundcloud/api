package com.soundcloud.publicApiStrangler.mapper.timeline.representation

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.jvmkit.module.json.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import play.api.libs.json.JsValue

class Track(
    jsonValue: JsValue,
    likesByUrn: Map[Urn, Long],
    repostCountsByUrn: Map[Urn, Long],
    baseUrl: String,
    entitySummaryMapper: EntitySummaryMapper
)(implicit context: MappingContext)
    extends TrackSummary(jsonValue, baseUrl, entitySummaryMapper) {
  val artwork_url = (json \ "artwork_url").asOpt[String]
  val comment_count = (json \ "comments_count").asOpt[Int]
  val commentable = (json \ "commentable").asOpt[Boolean]
  val description = (json \ "description").asOpt[String]
  val download_count = (json \ "downloads_count").asOpt[Int]
  val downloadable = for {
    downloadable <- (json \ "downloadable").asOpt[Boolean]
    has_downloads_left <- (json \ "has_downloads_left").asOpt[Boolean]
  } yield (downloadable && has_downloads_left)

  val embeddable_by = (json \ "embeddable_by").asOpt[String]
  val favoritings_count = (json \ "favoritings_count").asOpt[Int]
  val genre = (json \ "genre").asOpt[String]
  val isrc = (json \ "isrc").asOpt[String]
  val label_id = (json \ "label_id").asOpt[Long]
  val label_name = (json \ "label_name").asOpt[String]
  val license = (json \ "license").asOpt[String]
  val original_content_size = (json \ "original_content_size").asOpt[Int]
  val original_format = (json \ "original_format").asOpt[String]
  val playback_count = (json \ "playback_count").asOpt[Int]
  val purchase_title = (json \ "purchase_title").asOpt[String]
  // Option[Any] so we can override with an embedded attribute when calling okidoki
  val purchase_url: Option[Any] = (json \ "purchase_url").asOpt[String]
  val release = (json \ "release").asOpt[String]
  val release_day = (json \ "release_day").asOpt[Int]
  val release_month = (json \ "release_month").asOpt[Int]
  val release_year = (json \ "release_year").asOpt[Int]
  val reposts_count = repostCountsByUrn.get(urn).orElse((json \ "reposts_count").asOpt[Long])
  val state = (json \ "state").asOpt[String]
  val streamable = (json \ "streamable").asOpt[Boolean]
  val tag_list = (json \ "tag_list").asOpt[String]
  val track_type = (json \ "track_type").asOpt[String]
  val user = entitySummaryMapper.embed((json \ "user" \ "urn").as[Urn])
  // Option[Any] so we can override with an embedded attribute when calling Liebling
  val likes_count: Option[Any] = likesByUrn.get(urn)

  // deprecated fields, kept for structure only
  val bpm = None
  val key_signature: Option[String] = None
  // Option[Any] so we can override with an embedded attribute when calling Liebling
  val user_favorite: Option[Any] = None
  val user_playback_count: Option[Int] = None
  val video_url: Option[String] = None
}
