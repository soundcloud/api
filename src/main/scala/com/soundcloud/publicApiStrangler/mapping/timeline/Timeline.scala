package com.soundcloud.publicApiStrangler.mapping.timeline

import java.util.UUID

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.publicApiStrangler.mapper.timeline.e1.UUIDMapper
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.finagle.jsonservice.{StringParam, Param, Params}
import play.api.libs.json.{JsObject, JsValue}

abstract class Timeline(jsonValue: JsValue, page: CursorBasedPage[Urn])(implicit context: MappingContext)
  extends JsonMapping(jsonValue) {

  val collection: Seq[TimelineItem] = mapChildren(events).filter(contentAllowed)
  val futureHref = futurePage(
    events
  )
  val nextHref = nextPage(
    UUIDMapper.fromCursor((json \ "meta" \ "next_page_cursor").asOpt[String]),
    futureUuid(events)
  )

  def events = (json \ "events").as[Seq[JsObject]]

  def contentAllowed(item: TimelineItem) = {
    item.`type` != "promoted-stream"
  }

  protected def nextPage(uuid: Option[UUID], futureUuid: Option[String]): Option[String] =
    Some(
      cursorUrl(
        page.extraParams.filterKeys(_ != "uuid[to]"),
        uuid
      )
    )

  protected def mapChildren(events: Seq[JsObject]): Seq[TimelineItem]

  private def futurePage(events: Seq[JsObject]): String = {

    // TODO don't use a var here :(
    var params = page.extraParams.toMap
    futureUuid(events).map(u => params += ("uuid[to]" -> u))
    params = params.filterKeys(_ != "uuid[future]")

    cursorUrl(params, None)
  }

  private def futureUuid(events: Seq[JsObject]): Option[String] = {
    // when there is an uuid[future], use it.
    page.extraParams.get("uuid[future]").map(
      uuid =>
        Some(uuid.value.mkString(""))
    ).getOrElse {
      // use the uuid of the first item
      val latestId = events.flatMap(e => (e \ "cursor").asOpt[String]).headOption
      UUIDMapper.fromCursor(latestId).map(_.toString)
    }
  }

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
