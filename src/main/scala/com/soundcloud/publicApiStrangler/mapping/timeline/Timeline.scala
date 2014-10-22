package com.soundcloud.publicApiStrangler.mapping.timeline

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.finagle.jsonservice.Params
import play.api.libs.json.{JsObject, JsValue}

abstract class Timeline(jsonValue: JsValue, page: CursorBasedPage[Urn])(implicit context: MappingContext)
  extends JsonMapping(jsonValue) {

  val collection: Seq[TimelineItem] = mapChildren(events).filter(contentAllowed)
  val futureHref = futurePage(events)
  val nextHref = nextPage((json \ "meta" \ "next_page_cursor").asOpt[String])

  def events = (json \ "events").as[Seq[JsObject]]

  def contentAllowed(item: TimelineItem) = {
    item.`type` != "promoted-stream"
  }

  protected def nextPage(cursor: Option[String]): Option[String]
  protected def mapChildren(events: Seq[JsObject]): Seq[TimelineItem]

  private def futurePage(events: Seq[JsObject]): Option[String] = {
    if(page.cursor.isDefined) return None

    events.flatMap(e => (e \ "cursor").asOpt[String]) match {
      case latestId :: others => Some(cursorUrl(page.extraParams.updated("uuid[to]", latestId), None))
      case _ => None
    }
  }

  protected def cursorUrl(extraParams: Params, cursor: Option[String]): String =
    "https://" +
      CursorBasedPage(
        page.param,
        page.baseUrl,
        page.path,
        extraParams,
        cursor,
        page.limit
      ).href
}
