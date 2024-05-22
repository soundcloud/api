package com.soundcloud.apipublic.client.mothership

import play.api.libs.json.{Json, Writes}

case class UserProfileAvatarUpdate(
    content_type: String,
    resize_url: String,
    origin_uri: String,
    width: Int,
    height: Int
)

object UserProfileAvatarUpdate {
  implicit val userProfileAvatarUpdateWrites: Writes[UserProfileAvatarUpdate] = Json.writes[UserProfileAvatarUpdate]
}
