package com.soundcloud.publicApiStrangler.mapping.timeline.publicApi

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper}
import com.soundcloud.publicApiStrangler.mapping.timeline.Timeline
import com.soundcloud.scalakit.Urn
import play.api.libs.json.{JsObject, JsValue}

class TimelineWithOrigin(jsonValue: JsValue,
               page: CursorBasedPage[Urn],
               entityMapper: EntityMapper,
               entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends Timeline(jsonValue, page) {

  override protected def nextPage(cursor: Option[String]) = Some("https://" + page.next(cursor).href)

  override protected def mapChildren(events: Seq[JsObject]) = {
    events.map {
      event =>
        new TimelineItemWithOrigin(event, entityMapper, entitySummaryMapper)
    }
  }

}
