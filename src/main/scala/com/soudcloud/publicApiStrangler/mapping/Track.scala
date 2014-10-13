package com.soudcloud.publicApiStrangler.mapping

import com.soudcloud.publicApiStrangler.mapper.EntityMapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsValue


class Track(json: JsValue, entityMapper: EntityMapper)(implicit context: MappingContext) extends JsonMapping(json) {

//  val attachments_uri = (json \ "attachments_uri").asOpt[String]
//  val bpm = (json \ "bpm").asOpt[String]
//  val key_signature = (json \ "key_signature").asOpt[String]
//  val likes_count = (json \ "likes_count").asOpt[Int]
//  val policy = (json \ "policy").asOpt[String]
//  val user_favorite = (json \ "user_favorite").asOpt[Boolean]
//  val user_playback_count = (json \ "user_playback_count").asOpt[Int]
//  val video_url = (json \ "video_url").asOpt[String]

  val artwork_url = (json \ "artwork_url").asOpt[String]
  val comment_count = (json \ "comments_count").asOpt[Int]
  val commentable = (json \ "commentable").asOpt[Boolean]
  val created_at = (json \ "created_at").asOpt[String]
  val description = (json \ "description").asOpt[String]
  val download_count = (json \ "downloads_count").asOpt[Int]
  val downloadable = (json \ "downloadable").asOpt[Boolean]
  val duration = (json \ "duration").asOpt[Int]
  val embeddable_by = (json \ "embeddable_by").asOpt[String]
  val favoritings_count = (json \ "favoritings_count").asOpt[Int]
  val genre = (json \ "genre").asOpt[String]
  val id = Urn((json \ "self" \ "urn").as[String]).getIdentifier
  val isrc = (json \ "isrc").asOpt[String]
  val kind = "track"
  val label_id = (json \ "label_id").asOpt[Int]
  val label_name = (json \ "label_name").asOpt[String]
  val license = (json \ "license").asOpt[String]
  val original_content_size = (json \ "original_content_size").asOpt[Int]
  val original_format = (json \ "original_format").asOpt[String]
  val permalink = (json \ "permalink").asOpt[String]
  val permalink_url = (json \ "permalink_url").asOpt[String]
  val playback_count = (json \ "playback_count").asOpt[Int]
  val purchase_title = (json \ "purchase_title").asOpt[String]
  val purchase_url = (json \ "purchase_url").asOpt[String]
  val release = (json \ "release").asOpt[String]
  val release_day = (json \ "release_day").asOpt[Int]
  val release_month = (json \ "release_month").asOpt[Int]
  val release_year = (json \ "release_year").asOpt[Int]
  val reposts_count = (json \ "reposts_count").asOpt[Int]
  val sharing = (json \ "sharing").asOpt[String]
  val state = (json \ "state").asOpt[String]
  val stream_url = (json \ "stream_url").asOpt[String]
  val streamable = (json \ "streamable").asOpt[Boolean]
  val tag_list = (json \ "tag_list").asOpt[String]
  val title = (json \ "title").asOpt[String]
  val track_type = (json \ "track_type").asOpt[String]
  val uri = (json \ "self" \ "url").asOpt[String]
  val user = entityMapper.embed(Urn((json \ "user" \ "urn").as[String]))
  val user_id = (json \ "user_id").asOpt[Int]
  val waveform_url = (json \ "waveform_url").asOpt[String]

}
