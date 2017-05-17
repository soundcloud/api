package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.publicApiStrangler.representation.{TrackCursor, TrackMeta, TracksWithPagination}
import play.api.libs.json.{JsObject, JsValue}

object TracksWithPaginationMapper {
  def apply(json: JsValue): TracksWithPagination = {
    TracksWithPagination(
      tracks = (json \ "tracks").as[List[JsObject]].map(TrackMapper(_)),
      meta = TrackMeta((json \ "meta" \ "cursor").asOpt[JsObject].map(mapTrackCursor))
    )
  }

  private def mapTrackCursor(json: JsObject): TrackCursor =
    TrackCursor(
      nextHref = (json \ "next_href").as[String],
      after = (json \ "after").as[Int],
      limit = (json \ "limit").as[Int]
    )
}
