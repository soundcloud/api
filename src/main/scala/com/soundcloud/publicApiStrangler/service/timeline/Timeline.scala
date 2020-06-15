package com.soundcloud.publicApiStrangler.service.timeline

import java.util.UUID

import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import play.api.libs.json.Json

case class Timeline(timelineItems: List[TimelineItem], metaInfo: TimelineMeta, pagination: CursorBasedPagination) {

  def getRepresentation(): String = {
    val collectionJson = Json.arr(timelineItems.map(item => item.getRepresentation()))
    Json.stringify(
      Json.obj(
        "collection" -> collectionJson,
        "next_href" -> s"${pagination.baseUrl}${pagination.path}?limit=${pagination.pageSize}&cursor=${metaInfo.nextPageCursor
          .getOrElse(None)}",
        "future_href" -> s"${pagination.baseUrl}${pagination.path}?limit=${pagination.pageSize}&cursor=${metaInfo.previousPageCursor
          .map(cursor => UUID.fromString(cursor))
          .getOrElse(None)}"
      )
    )
  }
}
