package com.soundcloud.publicApiStrangler.mapping

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.publicApiStrangler.mapper.EntitySummaryMapper
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsValue


class TrackSummary(json: JsValue,
            baseUrl: String,
            entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends JsonMapping(json) with UrnSupport {

  val id = urn.getIdentifier.toInt
  val kind = "track"
  val created_at = (json \ "created_at").asOpt[String]
  val last_modified = (json \ "last_modified").asOpt[String]
  val permalink = (json \ "permalink").asOpt[String]
  val permalink_url = (json \ "permalink_url").asOpt[String]
  val title = (json \ "title").asOpt[String]
  val duration = (json \ "duration").asOpt[Int]
  val sharing = (json \ "sharing").asOpt[String]
  val waveform_url = (json \ "waveform_url").asOpt[String]
  val stream_url = (json \ "stream_url").asOpt[String]
  val uri = (json \ "self" \ "url").asOpt[String]
  val user_id = (json \ "user_id").asOpt[Int]
  val user_uri = s"https://$baseUrl/users/$user_id"

}
