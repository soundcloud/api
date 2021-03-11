package com.soundcloud.publicApiStrangler.service.timeline

import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import play.api.libs.json.Json

case class Timeline(timelineItems: List[TimelineItem], metaInfo: TimelineMeta, pagination: CursorBasedPagination) {

  private def getCursor(maybeCursor: Option[String]): String = {
    maybeCursor match {
      case Some(cursor) => s"&cursor=$cursor"
      case None => ""
    }
  }

  def getRepresentation(): String = {
    val collectionJson = timelineItems.map(item => item.getRepresentation())
    Json.stringify(
      Json.obj(
        "collection" -> collectionJson,
        "next_href" -> s"${pagination.baseUrl}${pagination.path}?limit=${pagination.pageSize}${getCursor(metaInfo.nextPageCursor)}",
        "future_href" -> s"${pagination.baseUrl}${pagination.path}?limit=${pagination.pageSize}${getCursor(metaInfo.previousPageCursor)}"
      )
    )
  }
}
