package com.soundcloud.publicApiStrangler.mapper.timeline.representation

import com.soundcloud.publicApiStrangler.client.followcounts.FollowCounts
import play.api.libs.json.{JsObject, JsValue, Json, Writes}

class User(
    json: JsValue,
    baseUrl: String,
    maybeFollowCounts: Option[FollowCounts],
    maybeRepostsCount: Option[Long]
) extends UserSummary(json, baseUrl) {
  val first_name = (json \ "first_name").asOpt[String]
  val last_name = (json \ "last_name").asOpt[String]
  val full_name = (json \ "full_name").asOpt[String]
  val city = (json \ "city").asOpt[String]
  val description = (json \ "description").asOpt[String]
  val country = (json \ "country").asOpt[String]
  val track_count = (json \ "tracks_count").asOpt[Int]
  val public_favorites_count = (json \ "public_favorites_count").asOpt[Long]
  val followers_count = maybeFollowCounts.map(_.followers).orElse((json \ "followers_count").asOpt[Long])
  val followings_count = maybeFollowCounts.map(_.followings).orElse((json \ "followings_count").asOpt[Long])
  val plan = (json \ "plan").asOpt[String]
  val myspace_name = nameInNetwork("myspace")
  val discogs_name = nameInNetwork("discogs")
  val website_title = nameInNetwork("personal", "title")
  val website = nameInNetwork("personal", "url")

  private def nameInNetwork(networkName: String, fieldName: String = "username"): Option[String] = {
    (json \ "links")
      .as[Seq[JsObject]]
      .filter(data => (data \ "network").as[String] == networkName) match {
      case networkData +: _ => (networkData \ fieldName).asOpt[String]
      case _ => None
    }
  }

  // deprecated fields
  val reposts_count = maybeRepostsCount.orElse((json \ "reposts_count").asOpt[Long])
  val comments_count = (json \ "comments_count").asOpt[Int]
  val online = false
  val likes_count = (json \ "public_favorites_count").asOpt[Long]
  val playlist_count: Option[Int] = (json \ "public_playlists_count").asOpt[Int]
}

object User {
  implicit val writes = new Writes[User] {
    override def writes(u: User): JsValue = {
      Json.obj(
        "avatar_url" -> u.avatar_url,
        "id" -> u.id.identifier.toLong,
        "kind" -> u.kind,
        "permalink_url" -> u.permalink_url,
        "uri" -> u.uri,
        "username" -> u.username,
        "permalink" -> u.permalink,
        "last_modified" -> u.last_modified,
        "first_name" -> u.first_name,
        "last_name" -> u.last_name,
        "full_name" -> u.full_name,
        "city" -> u.city,
        "description" -> u.description,
        "country" -> u.country,
        "track_count" -> u.track_count,
        "public_favorites_count" -> u.public_favorites_count,
        "followers_count" -> u.followers_count,
        "followings_count" -> u.followings_count,
        "plan" -> u.plan,
        "myspace_name" -> u.myspace_name,
        "discogs_name" -> u.discogs_name,
        "website_title" -> u.website_title,
        "website" -> u.website,
        "reposts_count" -> u.reposts_count,
        "comments_count" -> u.comments_count,
        "online" -> u.online,
        "likes_count" -> u.likes_count,
        "playlist_count" -> u.playlist_count
      )
    }
  }
}
