package com.soundcloud.publicApiStrangler.mapping.timeline

import java.util.UUID

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonInclude}
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.finagle.jsonservice.Params
import play.api.libs.json.{JsObject, JsValue}

abstract class Timeline(jsonValue: JsValue, page: CursorBasedPage[Urn])(implicit context: MappingContext)
  extends JsonMapping(jsonValue) {

  val collection: Seq[TimelineItem] = mapChildren(events).filter(contentAllowed)

  @JsonInclude(JsonInclude.Include.NON_NULL)
  val nextHref = nextPage(
    (json \ "meta" \ "next_page_cursor").asOpt[String].map(UUID.fromString)
  )

  def events = (json \ "events").as[Seq[JsObject]]

  private def doNotShow = Set("promoted-stream", "user-mention")

  def contentAllowed(item: TimelineItem) = !doNotShow.contains(item.`type`)

  protected def nextPage(uuid: Option[UUID]): Option[String] = {
    uuid match {
      case Some(_) => Some(cursorUrl(page.extraParams.filterKeys(_ != "uuid[to]"), uuid))
      case None => None
    }
  }

  protected def futurePage(events: Seq[JsObject]): String = {
    // TODO don't use a var here :(
    var params = page.extraParams.toMap

    futureUuid.map(u => params += ("uuid[to]" -> u.toString))
    cursorUrl(params, None)
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
