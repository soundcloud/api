package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.representation.Playlist
import play.api.libs.json.JsValue

object PlaylistMapper {
  def apply(json: JsValue): Playlist = {
    Playlist(
      urn = new Urn((json \ "self" \ "urn").as[String]),
      user_urn = new Urn((json \ "user" \ "urn").as[String]),
      title = (json \ "title").as[String],
      permalink = (json \ "permalink").as[String],
      description = (json \ "description").asOpt[String],
      created_at = (json \ "created_at").as[String],
      duration = (json \ "duration").as[Long],
      genre = (json \ "genre").asOpt[String].getOrElse(""),
      permalink_url = (json \ "permalink_url").as[String],
      artwork_url = (json \ "artwork_url").asOpt[String].getOrElse(""),
      track_count = (json \ "track_count").as[Int],
      likes_count = (json \ "favoritings_count").asOpt[Int].orElse((json \ "likes_count").asOpt[Int]).getOrElse(0),
      reposts_count = (json \ "reposts_count").asOpt[Int].getOrElse(0),
      sharing = (json \ "sharing").as[String],
      tag_list = (json \ "tag_list").as[String]
    )
  }
}