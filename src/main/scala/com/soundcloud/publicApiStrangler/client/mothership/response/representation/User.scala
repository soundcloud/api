package com.soundcloud.publicApiStrangler.client.mothership.response.representation

import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.{Json, Writes}

case class Product(id: String, name: String)

object Product {
  implicit val writes: Writes[Product] = Json.writes[Product]
}

case class Subscription(product: Product)

object Subscription {
  implicit val writes: Writes[Subscription] = Json.writes[Subscription]
}

case class User(
    urn: Urn,
    permalink: String,
    username: String,
    avatar_url: String,
    permalink_url: String,
    city: Option[String],
    country: Option[String],
    tracks_count: Int,
    public_tracks_count: Option[Int],
    followers_count: Option[Long],
    followings_count: Option[Long],
    verified: Boolean,
    description: Option[String],
    updated_at: Option[String],
    discogs_name: Option[String],
    first_name: Option[String],
    last_name: Option[String],
    full_name: Option[String],
    myspace_name: Option[String],
    website_title: Option[String],
    website: Option[String],
    plan: Option[String],
    subscriptions: Seq[Subscription],
    public_favorites_count: Option[Long],
    public_playlists_count: Option[Int], // deprecated, kept for structure
    comments_count: Option[Int], // deprecated, kept for structure
    likes_count: Option[Long], // deprecated, kept for structure
    reposts_count: Option[Long], // deprecated, kept for structure
    online: Boolean = false // deprecated, kept for structure
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
