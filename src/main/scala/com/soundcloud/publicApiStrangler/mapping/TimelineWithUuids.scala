package com.soundcloud.publicApiStrangler.mapping

import com.soundcloud.publicApiStrangler.mapper.{EntitySummaryMapper, EntityMapper}
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.finagle.jsonservice.Params
import play.api.libs.json.{JsObject, JsValue}

class TimelineWithUuids(json: JsValue,
               page: CursorBasedPage[Urn],
               entityMapper: EntityMapper,
               entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends Timeline(json, page) {

  override protected def nextPage(cursor: Option[String]) = Some(cursorUrl(page.extraParams.filterKeys(_ != "uuid[to]"), cursor))

  override protected def mapChildren(events: Seq[JsObject]) = {
    events.map { event =>
      val urn = Urn((event \ "urn").as[String])
      urn.getCollection match {
        case "tracks" => new TrackTimelineItem(event, entityMapper, entitySummaryMapper)
        case "playlists" => new PlaylistTimelineItem(event, entityMapper, entitySummaryMapper)
        case "comments" => new CommentTimelineItem(event, entityMapper, entitySummaryMapper)
        case "affiliations" => new ActorTimelineItem(event, entityMapper)
      }
    }
  }

}
