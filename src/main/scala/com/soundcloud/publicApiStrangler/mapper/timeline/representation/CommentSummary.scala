package com.soundcloud.publicApiStrangler.mapper.timeline.representation

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.jvmkit.module.json.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import play.api.libs.json.JsValue

class CommentSummary(jsonValue: JsValue, baseUrl: String, entitySummaryMapper: EntitySummaryMapper)(
    implicit context: MappingContext
) extends JsonMapping(jsonValue)
    with UrnSupport {
  val kind = "comment"
  val id = urn.identifier.toInt
  val created_at = (json \ "created_at").asOpt[String]
  val user_id = userUrn.identifier.toInt
  val uri = s"$baseUrl/comments/$id"
  val track_id = trackUrn.identifier.toInt
  val timestamp = (json \ "timestamp").asOpt[Int]
  val body = (json \ "body").asOpt[String]

  def trackUrn = (json \ "track").as[Urn]

  def userUrn = (json \ "user" \ "self" \ "urn").as[Urn]
}
