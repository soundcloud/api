package com.soundcloud.publicApiStrangler.client.mothership.response.representation

import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.{Json, Writes}

case class User(
    urn: Urn,
    permalink: String,
    username: String,
    avatar_url: String,
    permalink_url: String,
    city: Option[String],
    country: Option[String],
    tracks_count: Int,
    followers_count: Option[Int],
    followings_count: Option[Int],
    verified: Boolean,
    description: Option[String],
    updated_at: Option[String]
)

object User {
  implicit val writes = Writes[User] { user =>
    Json.obj(
      "id" -> user.urn.identifier.toLong,
      "kind" -> "user",
      "permalink" -> user.permalink,
      "username" -> user.username,
      "last_modified" -> user.updated_at,
      "uri" -> s"https://api.soundcloud.com/users/${user.urn.identifier}",
      "permalink_url" -> user.permalink_url,
      "avatar_url" -> user.avatar_url.replaceAll("\\?[0-9]+$", "").replaceAll("^http:", "https:")
    )
  }
}
