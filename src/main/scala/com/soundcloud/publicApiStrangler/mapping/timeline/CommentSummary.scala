package com.soundcloud.publicApiStrangler.mapping.timeline

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import com.soundcloud.scalakit.Urn
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

  def trackUrn = Urn((json \ "track").as[String])
  def userUrn = {
    val correctUrn = (json \ "user" \ "self" \ "urn").asOpt[String]
    val wrongUrn = correctUrn.getOrElse((json \ "user" \ "urn").as[String])
    Urn(wrongUrn)
  }

}
