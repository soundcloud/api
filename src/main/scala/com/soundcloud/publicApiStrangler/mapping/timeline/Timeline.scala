package com.soundcloud.publicApiStrangler.mapping.timeline

import java.util.UUID

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.publicApiStrangler.mapper.timeline.e1.UUIDMapper
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.finagle.jsonservice.Params
import play.api.libs.json.{JsObject, JsValue}

abstract class Timeline(jsonValue: JsValue, page: CursorBasedPage[Urn])(implicit context: MappingContext)
  extends JsonMapping(jsonValue) {

  val collection: Seq[TimelineItem] = mapChildren(events).filter(contentAllowed)
  val futureHref = futurePage(
    events,
    page.cursor.map(UUID.fromString)
  )
  val nextHref = nextPage(UUIDMapper.fromCursor((json \ "meta" \ "next_page_cursor").asOpt[String]))

  def events = (json \ "events").as[Seq[JsObject]]

  def contentAllowed(item: TimelineItem) = {
    item.`type` != "promoted-stream"
  }

  protected def nextPage(uuid: Option[UUID]): Option[String] =
    Some(
      cursorUrl(
        page.extraParams.filterKeys(_ != "uuid[to]"),
        uuid
      )
    )

  protected def mapChildren(events: Seq[JsObject]): Seq[TimelineItem]

  private def futurePage(events: Seq[JsObject], currentUuid: Option[UUID]): Option[String] = {
    if(currentUuid.isDefined && UUIDMapper.validUuid(currentUuid)) return None

    events.flatMap(e => (e \ "cursor").asOpt[String]) match {
      case latestId :: others =>
        Some(
          cursorUrl(
            page.extraParams.updated(
              "uuid[to]",
              UUIDMapper.fromCursor(Some(latestId)).get.toString
            ),
            None
          )
        )
      case _ => None
    }
  }

  protected def cursorUrl(extraParams: Params, uuid: Option[UUID]): String =
      CursorBasedPage(
        page.param,
        page.baseUrl,
        page.path,
        extraParams,
        uuid.map(_.toString),
        page.limit
      ).href
}
