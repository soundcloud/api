package com.soundcloud.publicApiStrangler.mapping.timeline

import java.util.UUID

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.publicApiStrangler.mapper.timeline.e1.UUIDMapper
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.finagle.jsonservice.{StringParam, Param, Params}
import play.api.libs.json.{JsObject, JsValue}

abstract class Timeline(jsonValue: JsValue, page: CursorBasedPage[Urn])(implicit context: MappingContext)
  extends JsonMapping(jsonValue) {

  val collection: Seq[TimelineItem] = mapChildren(events).filter(contentAllowed)

  @JsonInclude(JsonInclude.Include.NON_NULL)
  val nextHref = nextPage(
    UUIDMapper.fromCursor((json \ "meta" \ "next_page_cursor").asOpt[String])
  )

  def events = (json \ "events").as[Seq[JsObject]]

  def contentAllowed(item: TimelineItem) = {
    item.`type` != "promoted-stream"
  }

  protected def nextPage(uuid: Option[UUID]): Option[String] = {
    uuid match {
      case Some(_) => Some(cursorUrl(page.extraParams.filterKeys(_ != "uuid[to]"), uuid))
      case None => None
    }
  }

  protected def futurePage(events: Seq[JsObject]): String = {
    // TODO don't use a var here :(
    var params = page.extraParams.toMap
    futureUuid(events).map(u => params += ("uuid[to]" -> u))
    cursorUrl(params, None)
  }

  private def futureUuid(events: Seq[JsObject]): Option[String] = {
    // always use the uuid of the first item as future uuid
    val latestId = events.flatMap(e => (e \ "cursor").asOpt[String]).headOption
    UUIDMapper.fromCursor(latestId).map(_.toString)
  }

  protected def mapChildren(events: Seq[JsObject]): Seq[TimelineItem]

  protected def cursorUrl(extraParams: Params, cursorUuid: Option[UUID]): String =
      CursorBasedPage(
        page.param,
        page.baseUrl,
        page.path,
        extraParams,
        cursorUuid.map(_.toString),
        page.limit
      ).href
}
