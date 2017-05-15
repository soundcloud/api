package com.soundcloud.publicApiStrangler.mapping.timeline

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import play.api.libs.json.JsValue

class CommentSummary(jsonValue: JsValue,
               baseUrl: String,
               entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends JsonMapping(jsonValue) with UrnSupport {

  val kind = "comment"
  val id = urn.getIdentifier.toInt
  val created_at = (json \ "created_at").asOpt[String]
  val user_id = userUrn.getIdentifier.toInt
  val uri = s"$baseUrl/comments/$id"
  val track_id = trackUrn.getIdentifier.toInt
  val timestamp = (json \ "timestamp").asOpt[Int]
  val body = (json \ "body").asOpt[String]

  def trackUrn = new Urn((json \ "track").as[String])
  def userUrn = new Urn((json \ "user" \ "self" \ "urn").as[String])

}
