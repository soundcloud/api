package com.soundcloud.apipublic.client.mothership.response.representation

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.subscriptions.{SubmarineCreatorSubscription, SubmarineToLegacyMapper}
import com.soundcloud.apipublic.support.HtmlSanitizer
import play.api.libs.json.{Json, Writes}

case class Product(id: String, name: String)

object Product {
  implicit val writes: Writes[Product] = Json.writes[Product]
}

case class CreatorSubscription(product: Product, recurring: Option[Boolean] = None)

object CreatorSubscription {
  implicit val writes: Writes[CreatorSubscription] = Json.writes[CreatorSubscription]

  // maps to the old mothership representation
  def from(
      submarineSubscription: SubmarineCreatorSubscription,
      shouldIncludeRecurring: Boolean = false
  ): CreatorSubscription = {
    val plan = SubmarineToLegacyMapper.from(submarineSubscription)
    val maybeRecurring = if (shouldIncludeRecurring) Some(submarineSubscription.recurring) else None
    CreatorSubscription(Product(plan.id, plan.name), maybeRecurring)
  }
}

case class UserRepresentation(
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
    created_at: Option[String],
    updated_at: Option[String],
    discogs_name: Option[String],
    first_name: Option[String],
    last_name: Option[String],
    full_name: Option[String],
    myspace_name: Option[String],
    website_title: Option[String],
    website: Option[String],
    plan: Option[String],
    subscriptions: Seq[CreatorSubscription],
    public_favorites_count: Option[Long],
    public_playlists_count: Option[Int], // deprecated, kept for structure
    comments_count: Option[Int], // deprecated, kept for structure
    likes_count: Option[Long], // deprecated, kept for structure
    reposts_count: Option[Long], // deprecated, kept for structure
    online: Boolean = false // deprecated, kept for structure
)

object UserRepresentation {
  implicit val writes = Writes[UserRepresentation] { user =>
    Json.obj(
      "avatar_url" -> user.avatar_url.replaceAll("\\?[0-9]+$", "").replaceAll("^http:", "https:"),
      "id" -> user.urn.identifier.toLong,
      "kind" -> "user",
      "permalink_url" -> user.permalink_url,
      "uri" -> s"https://api.soundcloud.com/users/${user.urn.identifier}",
      "username" -> HtmlSanitizer.sanitize(user.username),
      "permalink" -> user.permalink,
      "created_at" -> user.created_at,
      "last_modified" -> user.updated_at,
      "first_name" -> user.first_name.map(HtmlSanitizer.sanitize),
      "last_name" -> user.last_name.map(HtmlSanitizer.sanitize),
      "full_name" -> user.full_name.map(HtmlSanitizer.sanitize),
      "city" -> user.city.map(HtmlSanitizer.sanitize),
      "description" -> user.description.map(HtmlSanitizer.sanitize),
      "country" -> user.country.map(HtmlSanitizer.sanitize),
      "track_count" -> user.public_tracks_count,
      "public_favorites_count" -> user.public_favorites_count,
      "reposts_count" -> user.reposts_count,
      "followers_count" -> user.followers_count,
      "followings_count" -> user.followings_count,
      "plan" -> user.plan,
      "myspace_name" -> user.myspace_name.map(HtmlSanitizer.sanitize),
      "discogs_name" -> user.discogs_name.map(HtmlSanitizer.sanitize),
      "website_title" -> user.website_title.map(HtmlSanitizer.sanitize),
      "website" -> user.website.map(HtmlSanitizer.sanitize),
      "comments_count" -> user.comments_count,
      "online" -> user.online,
      "likes_count" -> user.likes_count,
      "playlist_count" -> user.public_playlists_count,
      "subscriptions" -> Json.toJson(user.subscriptions)
    )
  }
}
