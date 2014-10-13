package com.soundcloud.publicApiStrangler.mapping

import com.soundcloud.publicApiStrangler.mapper.EntityMapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.scalakit.Urn
import play.api.libs.json.{JsObject, JsValue}

class Playlist(json: JsValue,
               val likesByUrn: Map[Urn, Int],
               baseUrl: String,
               entityMapper: EntityMapper)(implicit context: MappingContext)
  extends JsonMapping(json) with UrnSupport with LikesCountSupport {

  val kind = "playlist"
  val id = urn.getIdentifier
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
  val likes_count = likesByUrn(urn)
  val reposts_count = (json \ "reposts_count").asOpt[Int]
  val tracks_uri = s"https://$baseUrl/playlists/$id/tracks"
  val secret_uri = s"https://$baseUrl/playlists/$id?secret_token=$secret_token"

  // deprecated fields, kept for structure only
  val downloadable = None
  val `type` = None
  val purchase_url = None
  val playlist_type = None
  val ean = None
  val purchase_title = None
  val created_with = None

}
