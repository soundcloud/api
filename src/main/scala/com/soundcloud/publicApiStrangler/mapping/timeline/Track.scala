package com.soundcloud.publicApiStrangler.mapping.timeline

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import com.soundcloud.scalakit.Urn
import com.soundcloud.service.response.representation.liebling.LikesCount
import play.api.libs.json.JsValue


class Track(jsonValue: JsValue,
            likesCounts: Seq[LikesCount],
            baseUrl: String,
            entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
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
  val label_id = (json \ "label_id").asOpt[Int]
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
  val reposts_count = (json \ "reposts_count").asOpt[Int]
  val state = (json \ "state").asOpt[String]
  val streamable = (json \ "streamable").asOpt[Boolean]
  val tag_list = (json \ "tag_list").asOpt[String]
  val track_type = (json \ "track_type").asOpt[String]
  val user = entitySummaryMapper.embed(Urn((json \ "user" \ "urn").as[String]))
  // Option[Any] so we can override with an embedded attribute when calling Liebling
  val likes_count: Option[Any] = likesCounts.find(_.target_urn == urn).map(_.likes_count)

  // deprecated fields, kept for structure only
  val attachments_uri: Option[String] = None
  val bpm = None
  val key_signature: Option[String] = None
  // Option[Any] so we can override with an embedded attribute when calling Liebling
  val user_favorite: Option[Any] = None
  val user_playback_count: Option[Int] = None
  val video_url: Option[String] = None
}
