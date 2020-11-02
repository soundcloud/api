package com.soundcloud.publicApiStrangler.mapper.timeline.representation

import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json._

class UserSummary(json: JsValue, baseUrl: String) {
  val avatar_url = (json \ "avatar_url").asOpt[String]
  val id = (json \ "self" \ "urn").as[Urn]
  val kind = "user"
  val permalink_url = (json \ "permalink_url").asOpt[String]
  val uri = s"$baseUrl/users/${id.identifier}"
  val username = (json \ "username").asOpt[String]
  val permalink = (json \ "permalink").asOpt[String]
  val last_modified = (json \ "last_modified").asOpt[String]
}
