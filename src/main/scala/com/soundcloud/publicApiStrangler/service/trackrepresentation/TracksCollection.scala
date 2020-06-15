package com.soundcloud.publicApiStrangler.service.trackrepresentation

import play.api.libs.json.Json

case class TracksCollection(tracks: List[TrackRepresentation], nextHref: Option[String])

object TracksCollection {
  def getRepresentation(tracksCollection: TracksCollection, hasLinkedPartitioning: Boolean) = {
    if (hasLinkedPartitioning) {
      val tracksJson = Json.obj("collection" -> Json.toJson(tracksCollection.tracks))
      val json = tracksCollection.nextHref
        .map(nextHref => {
          tracksJson ++ Json.obj("next_href" -> nextHref)
        })
        .getOrElse(tracksJson)

      Json.stringify(json)
    } else {
      Json.stringify(Json.toJson(tracksCollection.tracks))
    }
  }
}
