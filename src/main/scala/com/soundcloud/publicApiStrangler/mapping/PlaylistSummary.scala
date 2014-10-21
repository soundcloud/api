package com.soundcloud.publicApiStrangler.mapping

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.publicApiStrangler.mapper.EntitySummaryMapper
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsValue

class PlaylistSummary(json: JsValue,
               baseUrl: String,
               entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends JsonMapping(json) with UrnSupport {

  val kind = "playlist"
  val id = urn.getIdentifier.toInt
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
  val uri = s"https://$baseUrl/playlists/$id"
  val permalink_url = (json \ "permalink_url").asOpt[String]
  val artwork_url = (json \ "artwork_url").asOpt[String]
  val license = (json \ "license").asOpt[String]
  val user_id = if(userUrn.isDefined) userUrn.get.getIdentifier.toInt else None
  val user = if(userUrn.isDefined) entitySummaryMapper.embed(userUrn.get) else None
  val secret_token = (json \ "secret_token").as[String]
  val reposts_count = (json \ "reposts_count").asOpt[Int]
  val tracks_uri = s"https://$baseUrl/playlists/$id/tracks"
  val secret_uri = s"https://$baseUrl/playlists/$id?secret_token=$secret_token"


  private def userUrn: Option[Urn] = (json \ "user" \ "urn").asOpt[String] match {
    case None => None
    case Some(urn) => Some(Urn(urn))
  }

}
