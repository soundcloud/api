package com.soundcloud.publicApiStrangler.mapping.timeline.e1

import java.util.UUID

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.publicApiStrangler.mapper.timeline.e1.UUIDMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper}
import com.soundcloud.publicApiStrangler.mapping.timeline._
import com.soundcloud.scalakit.Urn
import play.api.libs.json.{JsObject, JsValue}

class TimelineWithUuids(jsonValue: JsValue,
               page: CursorBasedPage[Urn],
               entityMapper: EntityMapper,
               entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends Timeline(jsonValue, page) {

  val futureHref: Option[String] = futurePage(
    events,
    page.cursor.map(UUID.fromString)
  )

  private def futurePage(events: Seq[JsObject], currentUuid: Option[UUID]): Option[String] = {
    // if already paginating, don't include a future page
    if(currentUuid.isDefined && UUIDMapper.validUuid(currentUuid)) {
      None
    } else {
      Some(futurePage(events))
    }
  }

  override protected def mapChildren(events: Seq[JsObject]) = {
    events.map {
      event =>
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
