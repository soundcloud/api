package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCounts
import com.soundcloud.publicApiStrangler.client.liebling.UserTotalLikes
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{
  Product,
  Subscription,
  UserRepresentation
}
import play.api.libs.json.{JsObject, JsValue}

object UserRepresentationMapper {
  private def getSubscriptions(json: JsValue): Seq[Subscription] = {
    (json \ "subscriptions")
      .asOpt[Seq[JsObject]]
      .map(_.map(sub => {
        val id = (sub \ "product" \ "urn").as[Urn].identifier
        val name = (sub \ "product" \ "name").as[String]
        Subscription(Product(id, name))
      }))
      .getOrElse(Seq.empty)
  }

  private def getNameInNetwork(json: JsValue, networkName: String, fieldName: String = "username"): Option[String] = {
    (json \ "links")
      .asOpt[Seq[JsObject]]
      .flatMap(links =>
        links.filter(data => (data \ "network").as[String] == networkName) match {
          case networkData +: _ => (networkData \ fieldName).asOpt[String]
          case _ => None
        }
      )
  }

  def apply(
      json: JsValue,
      maybeFollowCounts: Option[Map[Urn, FollowCounts]] = None,
      maybeRepostsCounts: Option[Map[Urn, Long]] = None,
      maybeTotalLikesCounts: Option[Map[Urn, UserTotalLikes]] = None,
      currentUser: Option[Urn] = None
  ): UserRepresentation = {
    val urn = (json \ "self" \ "urn").as[Urn]
    val isCurrentUser = currentUser.contains(urn)
    val followCount = maybeFollowCounts.flatMap(_.get(urn))
    val repostCount = maybeRepostsCounts.flatMap(_.get(urn))
    val publicFavoritesCount = maybeTotalLikesCounts.flatMap(_.get(urn))

    val privateTracksCount = if (isCurrentUser) (json \ "private_tracks_count").asOpt[Long] else None
    val privatePlaylissCount = if (isCurrentUser) (json \ "private_playlists_count").asOpt[Long] else None
    val isPrimaryEmailConfirmed = if (isCurrentUser) (json \ "primary_email_confirmed").asOpt[Boolean] else None
    val locale = if (isCurrentUser) (json \ "locale").asOpt[String] else None

    new UserRepresentation(
      urn = urn,
      permalink = (json \ "permalink").as[String],
      username = (json \ "username").as[String],
      avatar_url = (json \ "avatar_url").as[String],
      permalink_url = (json \ "permalink_url").as[String],
      city = (json \ "city").asOpt[String],
      country = (json \ "country").asOpt[String],
      tracks_count = (json \ "tracks_count").as[Int],
      public_tracks_count = (json \ "public_tracks_count").asOpt[Int],
      followings_count = followCount.map(_.followings).orElse((json \ "followings_count").asOpt[Long]),
      followers_count = followCount.map(_.followers).orElse((json \ "followers_count").asOpt[Long]),
      verified = (json \ "verified").as[Boolean],
      description = (json \ "description").asOpt[String],
      created_at = (json \ "created_at").asOpt[String],
      updated_at = (json \ "updated_at").asOpt[String],
      discogs_name = getNameInNetwork(json, "discogs"),
      first_name = (json \ "first_name").asOpt[String],
      last_name = (json \ "last_name").asOpt[String],
      full_name = (json \ "full_name").asOpt[String],
      myspace_name = getNameInNetwork(json, "myspace"),
      website_title = getNameInNetwork(json, "personal", "title"),
      website = getNameInNetwork(json, "personal", "url"),
      plan = (json \ "plan").asOpt[String],
      subscriptions = getSubscriptions(json),
      public_favorites_count =
        publicFavoritesCount.map(_.totalLikeCount).orElse((json \ "public_favorites_count").asOpt[Long]),
      public_playlists_count = (json \ "public_playlists_count").asOpt[Int],
      comments_count = (json \ "comments_count").asOpt[Int],
      likes_count = (json \ "public_favorites_count").asOpt[Long],
      reposts_count = repostCount.orElse((json \ "reposts_count").asOpt[Long]),
      private_tracks_count = privateTracksCount,
      private_playlists_count = privatePlaylissCount,
      primary_email_confirmed = isPrimaryEmailConfirmed,
      locale = locale
    )
  }
}
