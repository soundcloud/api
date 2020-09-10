package com.soundcloud.publicApiStrangler.service.playlists.representation

import play.api.libs.json.{Json, Writes}

case class Collection[T](items: List[T], nextHref: Option[String])

object Collection {
  def getRepresentation[T: Writes](collection: Collection[T], hasLinkedPartitioning: Boolean): String = {
    if (hasLinkedPartitioning) {
      val collectionJson = Json.obj("collection" -> Json.toJson(collection.items))
      val json = collection.nextHref
        .map(nextHref => {
          collectionJson ++ Json.obj("next_href" -> nextHref)
        })
        .getOrElse(collectionJson)

      Json.stringify(json)
    } else {
      Json.stringify(Json.toJson(collection.items))
    }
  }
}
