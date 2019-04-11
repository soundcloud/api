package com.soundcloud.publicApiStrangler.mapper.timeline.representation.e1

import java.util.UUID

import com.fasterxml.jackson.annotation.JsonInclude
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.jvmkit.module.json.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.mapper.timeline.representation.Timeline
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper}
import play.api.libs.json.{JsObject, JsValue}

class TimelineWithUuids(jsonValue: JsValue,
                        page: CursorBasedPage[Urn],
                        entityMapper: EntityMapper,
                        entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends Timeline(jsonValue, page) {

  // only include future_href if a cursor is not defined or invalid
  @JsonInclude(JsonInclude.Include.NON_NULL)
  val futureHref: Option[String] = futurePage(
    events,
    page.cursor.map(UUID.fromString)
  )

  private def futurePage(events: Seq[JsObject], currentUuid: Option[UUID]): Option[String] = {
    // if already paginating, don't include a future page
    if (currentUuid.isDefined) {
      None
    } else {
      Some(futurePage(events))
    }
  }

  override protected def mapChildren(events: Seq[JsObject]) = {
    events.map {
      event =>
        val urn = (event \ "urn").as[Urn]
        urn.collection match {
          case "tracks" => new TrackTimelineItem(event, entityMapper, entitySummaryMapper)
          case "playlists" => new PlaylistTimelineItem(event, entityMapper, entitySummaryMapper)
          case "comments" => new CommentTimelineItem(event, entityMapper, entitySummaryMapper)
          case "affiliations" => new ActorTimelineItem(event, entityMapper)
        }
    }
  }

}
