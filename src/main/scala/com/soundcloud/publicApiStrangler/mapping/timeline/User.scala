package com.soundcloud.publicApiStrangler.mapping.timeline

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCounts
import play.api.libs.json.{JsObject, JsValue}

class User(jsonValue: JsValue, baseUrl: String, maybeFollowCounts: Option[FollowCounts])(implicit context: MappingContext)
  extends UserSummary(jsonValue, baseUrl) {

  val first_name = (json \ "first_name").asOpt[String]
  val last_name = (json \ "last_name").asOpt[String]
  val full_name = (json \ "full_name").asOpt[String]
  val city = (json \ "city").asOpt[String]
  val description = (json \ "description").asOpt[String]
  val country = (json \ "country").asOpt[String]
  val track_count = (json \ "tracks_count").asOpt[Int]
  val public_favorites_count = (json \ "public_favorites_count").asOpt[Int]
  val followers_count = maybeFollowCounts.map(_.followers).orElse((json \ "followers_count").asOpt[Long])
  val followings_count = maybeFollowCounts.map(_.followings).orElse((json \ "followings_count").asOpt[Long])
  val plan = (json \ "plan").asOpt[String]
  val myspace_name = nameInNetwork("myspace")
  val discogs_name = nameInNetwork("discogs")
  val website_title = nameInNetwork("personal", "title")
  val website = nameInNetwork("personal", "url")


  private def nameInNetwork(networkName: String, fieldName: String = "username"): Option[String] = {
    (json \ "links").as[Seq[JsObject]].filter(
      data => (data \ "network").as[String] == networkName
    ) match {
      case networkData :: _ => (networkData \ fieldName).asOpt[String]
      case _ => None
    }
  }

  // deprecated fields, kept for structure only
  val reposts_count = None
  val comments_count = None
  val online = false
  val likes_count = None
  val playlist_count: Option[Int] = None

}
