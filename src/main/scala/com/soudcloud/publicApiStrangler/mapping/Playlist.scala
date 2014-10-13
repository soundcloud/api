package com.soudcloud.publicApiStrangler.mapping

import com.soudcloud.publicApiStrangler.mapper.EntityMapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsValue

class Playlist(json: JsValue, entityMapper: EntityMapper)(implicit context: MappingContext) extends JsonMapping(json) {

//  val downloadable = (json \ "downloadable").asOpt[Boolean]
//  val `type` = (json \ "type").asOpt[String]
//  val purchase_url = (json \ "purchase_url").asOpt[String]
//  val playlist_type = (json \ "playlist_type").asOpt[String]
//  val ean = (json \ "ean").asOpt[String]
//  val purchase_title = (json \ "purchase_title").asOpt[String]
//  "created_with": {
//    "id": 3152,
//    "kind": "app",
//    "name": "SoundCloud Android",
//    "uri": "https:\/\/api.soundcloud.com\/apps\/3152",
//    "permalink_url": "http:\/\/soundcloud.com\/apps\/android",
//    "external_url": "https:\/\/market.android.com\/details?id=com.soundcloud.android",
//    "creator": "jonathanschmidt"
//  }



  val kind = "playlist"
  val id = Urn((json \ "self" \ "urn").as[String]).getIdentifier
  val created_at = (json \ "created_at").asOpt[String]
  val user_id = Urn((json \ "user" \ "urn").as[String]).getIdentifier
  val duration = (json \ "duration").asOpt[Int]
  val last_modified = (json \ "last_modified").asOpt[String]
  val sharing = (json \ "sharing").asOpt[String]
  val tag_list = (json \ "tag_list").asOpt[String]
  val permalink = (json \ "permalink").asOpt[String]
  val track_count = (json \ "track_count").asOpt[Int]
  val streamable = (json \ "streamable").asOpt[Boolean]
  val embeddable_by = (json \ "embeddable_by").asOpt[String]
  val description = (json \ "description").asOpt[String]
  val genre = (json \ "genre").asOpt[String]
  val release = (json \ "release").asOpt[String]
  val label_name = (json \ "label_name").asOpt[String]
  val title = (json \ "title").asOpt[String]
  val release_year = (json \ "release_year").asOpt[String]
  val release_month = (json \ "release_month").asOpt[String]
  val release_day = (json \ "release_day").asOpt[String]
  val uri = (json \ "self" \ "url").asOpt[String]
  val permalink_url = (json \ "permalink_url").asOpt[String]
  val artwork_url = (json \ "artwork_url").asOpt[String]
  val license = (json \ "license").asOpt[String]
  val user = entityMapper.embed(Urn((json \ "user" \ "urn").as[String]))
  val secret_token = (json \ "secret_token").as[String]
  val likes_count = (json \ "likes_count").asOpt[Int]
  val reposts_count = (json \ "reposts_count").asOpt[Int]
  val tracks_uri = s"https://api.soundcloud.com/playlists/$id/tracks"
  val secret_uri = s"https://api.soundcloud.com/playlists/$id?secret_token=$secret_token"

}
