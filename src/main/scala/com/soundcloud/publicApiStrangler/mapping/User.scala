package com.soundcloud.publicApiStrangler.mapping

import com.soundcloud.bff.nextbff.mapping.MappingContext
import play.api.libs.json.{JsObject, JsValue}


class User(json: JsValue, baseUrl: String)(implicit context: MappingContext)
  extends UserSummary(json, baseUrl) {

  val first_name = (json \ "first_name").asOpt[String]
  val last_name = (json \ "last_name").asOpt[String]
  val full_name = (json \ "full_name").asOpt[String]
  val city = (json \ "city").asOpt[String]
  val description = (json \ "description").asOpt[String]
  val followers_count = (json \ "followers_count").asOpt[Int]
  val country = (json \ "country").asOpt[String]
  val track_count = (json \ "track_count").asOpt[Int]
  val public_favorites_count = (json \ "public_favorites_count").asOpt[Int]
  val followings_count = (json \ "followings_count").asOpt[Int]
  val plan = (json \ "plan").asOpt[String]
  val subscriptions = (json \ "subscriptions").asOpt[String] // TODO test
  val myspace_name = nameInNetwork("myspace")
  val discogs_name = nameInNetwork("discogs")
  val website_title = nameInNetwork("personal")
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
  val playlist_count = None

}