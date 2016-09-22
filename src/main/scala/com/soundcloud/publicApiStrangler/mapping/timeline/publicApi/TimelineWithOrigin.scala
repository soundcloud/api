package com.soundcloud.publicApiStrangler.mapping.timeline.publicApi

import com.fasterxml.jackson.annotation.JsonInclude
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper}
import com.soundcloud.publicApiStrangler.mapping.timeline.Timeline
import play.api.libs.json.{JsObject, JsValue}

class TimelineWithOrigin(jsonValue: JsValue,
               page: CursorBasedPage[Urn],
               entityMapper: EntityMapper,
               entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends Timeline(jsonValue, page) {

  // always include a future_href
  @JsonInclude(JsonInclude.Include.NON_NULL)
  val futureHref: String = futurePage(events)

  override protected def mapChildren(events: Seq[JsObject]) = {
    events.map {
      event =>
        new TimelineItemWithOrigin(event, entityMapper, entitySummaryMapper)
    }
  }

}
