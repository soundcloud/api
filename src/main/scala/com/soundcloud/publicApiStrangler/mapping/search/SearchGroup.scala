package com.soundcloud.publicApiStrangler.mapping.search

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import com.soundcloud.publicApiStrangler.mapping.timeline.UrnSupport
import play.api.libs.json.JsValue

class SearchGroup(jsonValue: JsValue,
            baseUrl: String,
            entitySummaryMapper: EntitySummaryMapper)
           (implicit context: MappingContext)
  extends JsonMapping(jsonValue) with UrnSupport {
  val kind = "group"
  val id = urn.getIdentifier.toInt
  val created_at = (json \ "created_at").as[String]
  val permalink = (json \ "permalink").as[String]
  val track_count = (json \ "track_count").as[Int]
  val members_count = (json \ "members_count").as[Int]
  val moderated = (json \ "moderated").as[Boolean]
  val name = (json \ "name").as[String]
  val short_description = (json \ "short_description").asOpt[String]
  val description = (json \ "description").asOpt[String]
  val uri = s"$baseUrl/groups/$id"
  val artwork_url = (json \ "artwork_url").asOpt[String]
  val permalink_url = (json \ "permalink_url").as[String]

  val creator = (json \ "creator" \ "urn").asOpt[String]
    .map(s => entitySummaryMapper.embed(new Urn(s)))
}
