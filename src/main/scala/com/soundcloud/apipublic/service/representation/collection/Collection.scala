package com.soundcloud.apipublic.service.representation.collection

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

  def getNonNullRepresentation[T: Writes](collection: Collection[T], hasLinkedPartitioning: Boolean): String = {
    if (hasLinkedPartitioning) {
      var json = Json.obj("collection" -> Json.toJson(collection.items))
      collection.nextHref match {
        case Some(nextHref) => json = json ++ Json.obj("next_href" -> nextHref)
        case None => json = json
      }
      Json.stringify(json)
    } else {
      Json.stringify(Json.toJson(collection.items))
    }
  }
}
