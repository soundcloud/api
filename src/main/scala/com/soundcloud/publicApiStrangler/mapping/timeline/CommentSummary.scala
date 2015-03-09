package com.soundcloud.publicApiStrangler.mapping.timeline

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsValue

// TODO: ADD MAPPING TESTS FOR THIS CLASS
class CommentSummary(jsonValue: JsValue, baseUrl: String)(implicit context: MappingContext)
  extends JsonMapping(jsonValue)
  with UrnSupport {

  val kind = "comment"
  val id = urn.getIdentifier.toInt
  val created_at = (json \ "created_at").asOpt[String]
  val user_id = userUrn.map(_.getIdentifier.toInt)
  val uri = s"$baseUrl/comments/$id"
  val track_id = trackUrn.getIdentifier.toInt
  val timestamp = (json \ "timestamp").asOpt[Int]
  val body = (json \ "body").asOpt[String]

  def trackUrn = Urn((json \ "track").as[String])

  protected def userUrn = ((json \ "user" \ "urn").asOpt[String]).map(Urn(_))

  override def isValid = userUrn.isDefined
}
