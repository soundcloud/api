package com.soundcloud.publicApiStrangler.service.representation.collection

import play.api.libs.json.{Json, Writes}

case class Collection[T](items: List[T], nextHref: Option[String])

object Collection {
  def getRepresentation[T: Writes](collection: Collection[T], hasLinkedPartitioning: Boolean): String = {
    if (hasLinkedPartitioning) {
      val json = Json.obj("collection" -> Json.toJson(collection.items), "next_href" -> collection.nextHref)
      Json.stringify(json)
    } else {
      Json.stringify(Json.toJson(collection.items))
    }
  }
}
