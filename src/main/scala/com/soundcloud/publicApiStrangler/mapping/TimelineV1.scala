package com.soundcloud.publicApiStrangler.mapping

import com.soundcloud.publicApiStrangler.mapper.{EntitySummaryMapper, EntityMapper}
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.bff.nextbff.pagination.{PageBuilder, CursorBasedPage}
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.finagle.jsonservice.Params
import play.api.libs.json.{JsObject, JsValue}

class TimelineV1(json: JsValue,
               page: CursorBasedPage[Urn],
               entityMapper: EntityMapper,
               entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends Timeline(json) {

  override protected def nextPage(cursor: Option[String]) = Some("https://" + page.next(cursor).href)

  override protected def mapChildren(events: Seq[JsObject]) = {
    events.map { event =>
      new TimelineItemV1(event, entityMapper, entitySummaryMapper)
    }
  }

}
