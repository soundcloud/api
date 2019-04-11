package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.jvmkit.module.json.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.User
import play.api.libs.json.JsValue

object UserMapper {
  def apply(json: JsValue): User = {
    new User(
      (json \ "self" \ "urn").as[Urn],
      (json \ "permalink").as[String],
      (json \ "username").as[String],
      (json \ "avatar_url").as[String],
      (json \ "permalink_url").as[String],
      (json \ "city").asOpt[String],
      (json \ "country").asOpt[String],
      (json \ "tracks_count").as[Int],
      (json \ "followers_count").asOpt[Int],
      (json \ "followings_count").asOpt[Int],
      (json \ "verified").as[Boolean],
      (json \ "description").asOpt[String],
      (json \ "updated_at").asOpt[String]
    )
  }
}
