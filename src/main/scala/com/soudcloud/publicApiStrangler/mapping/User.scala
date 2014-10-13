package com.soudcloud.publicApiStrangler.mapping

import com.soundcloud.bff.nextbff.mapping.{MappingContext, JsonMapping}
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsValue


class User(json: JsValue)(implicit context: MappingContext) extends JsonMapping(json) {

  val avatar_url = (json \ "avatar_url").asOpt[String]
  val id = Urn((json \ "self" \ "urn").as[String]).getIdentifier
  val kind = "user"
  val permalink_url = (json \ "permalink_url").asOpt[String]
  val uri = (json \ "self" \ "url").asOpt[String]
  val username = (json \ "username").asOpt[String]
  val permalink = s"https://api.soundcloud.com/users/$username"

}
