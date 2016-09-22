package com.soundcloud.publicApiStrangler.mapping.timeline

import java.util.UUID

import com.fasterxml.jackson.annotation.JsonInclude
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.jvmkit.Urn
import com.soundcloud.scalakit.finagle.jsonservice.Params
import play.api.libs.json.{JsObject, JsValue}

abstract class Timeline(jsonValue: JsValue, page: CursorBasedPage[Urn])(implicit context: MappingContext)
  extends JsonMapping(jsonValue) {

  val collection: Seq[TimelineItem] = mapChildren(events).filterNot(contentDisallowed)

  @JsonInclude(JsonInclude.Include.NON_NULL)
  val nextHref = nextPage(
    (json \ "meta" \ "next_page_cursor").asOpt[String].map(UUID.fromString)
  )

  def events = (json \ "events").as[Seq[JsObject]]

  private def doNotShow = Set("user-mention")

  def contentDisallowed(item: TimelineItem) = doNotShow.contains(item.`type`)

  protected def nextPage(uuid: Option[UUID]): Option[String] = {
    uuid match {
      case Some(_) => Some(cursorUrl(page.extraParams.filterKeys(_ != "uuid[to]"), uuid))
      case None => None
    }
  }

  protected def futurePage(events: Seq[JsObject]): String = {
    val params = page.extraParams.toMap
    val updatedParams = futureUuid.map(u => params ++ Params("uuid[to]" -> u.toString)).getOrElse(params)

    cursorUrl(updatedParams, None)
  }

  private def futureUuid: Option[UUID] = (json \ "meta" \ "previous_page_cursor").asOpt[String].map(UUID.fromString)

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
