package com.soundcloud.publicApiStrangler.mapping.search

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.publicApiStrangler.mapping.timeline.User
import com.soundcloud.scalakit.Urn
import play.api.libs.json.{JsObject, JsValue}

/**
 * Representation of a user as returned by search on public API.
 *
 * Similar to the user representation on timeline, but with some differences.
 */
class SearchUser(jsonValue: JsValue,
                 baseUrl: String)
                // Yep, that was my reaction, too.
                (implicit if_this_is_named_context_then_serialization_fails: MappingContext)
  extends User(jsonValue, baseUrl) {

  // TODO: add comments_count, likes_count, reposts_count fields iff widget needs them

  override val playlist_count = (json \ "public_playlists_count").asOpt[Int]
  override val track_count = (json \ "public_tracks_count").asOpt[Int]

  val subscriptions = (json \ "subscriptions").as[Seq[JsObject]].map { sub =>
    val id = Urn((sub \ "product" \ "urn").as[String]).getIdentifier
    val name = (sub \ "product" \ "name").as[String]
    Subscription(Product(id, name))
  }
}

case class Product(id: String, name: String)
case class Subscription(product: Product)
