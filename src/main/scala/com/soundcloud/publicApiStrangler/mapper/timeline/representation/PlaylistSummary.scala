package com.soundcloud.publicApiStrangler.mapper.timeline.representation

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.jvmkit.module.json.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import play.api.libs.json.JsValue

class PlaylistSummary(jsonValue: JsValue,
                      repostCountsByUrn: Map[Urn, Long],
                      baseUrl: String,
                      entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends JsonMapping(jsonValue) with UrnSupport {

  val kind = "playlist"
  val id = urn.identifier.toInt
  val created_at = (json \ "created_at").asOpt[String]
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
  val uri = s"$baseUrl/playlists/$id"
  val permalink_url = (json \ "permalink_url").asOpt[String]
  val artwork_url = (json \ "artwork_url").asOpt[String]
  val license = (json \ "license").asOpt[String]
  val user_id = if (userUrn.isDefined) userUrn.get.identifier.toInt else None
  val user = if (userUrn.isDefined) entitySummaryMapper.embed(userUrn.get) else None
  val secret_token = (json \ "secret_token").as[String]
  val reposts_count = repostCountsByUrn.get(urn).orElse((json \ "reposts_count").asOpt[Long])
  val tracks_uri = s"$baseUrl/playlists/$id/tracks"
  val secret_uri = s"$baseUrl/playlists/$id?secret_token=$secret_token"


  private def userUrn: Option[Urn] = (json \ "user" \ "urn").asOpt[Urn]

}
