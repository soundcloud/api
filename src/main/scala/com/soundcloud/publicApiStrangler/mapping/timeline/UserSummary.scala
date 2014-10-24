package com.soundcloud.publicApiStrangler.mapping.timeline

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import play.api.libs.json.JsValue


class UserSummary(jsonValue: JsValue, baseUrl: String)(implicit context: MappingContext)
  extends JsonMapping(jsonValue) with UrnSupport {

  val avatar_url = (json \ "avatar_url").asOpt[String]
  val id = urn.getIdentifier.toInt
  val kind = "user"
  val permalink_url = (json \ "permalink_url").asOpt[String]
  val uri = s"$baseUrl/users/$id"
  val username = (json \ "username").asOpt[String]
  val permalink = (json \ "permalink").asOpt[String]
  val last_modified = (json \ "last_modified").asOpt[String]

}
