package com.soundcloud.publicApiStrangler.mapping

import com.soundcloud.publicApiStrangler.mapper.EntityMapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.finagle.jsonservice.Params
import play.api.libs.json.{JsObject, JsValue}

class Timeline(json: JsValue,
               page: CursorBasedPage[Urn],
               entityMapper: EntityMapper)(implicit context: MappingContext) extends JsonMapping(json) {

  val collection = mapChildren((json \ "events").as[Seq[JsObject]])
  val futureHref = futurePage(collection)
  val nextHref = nextPage((json \ "meta" \ "next_page_cursor").asOpt[String])


  private def futurePage(events: Seq[TimelineItem]): Option[String] = {
    events.filter(_.uuid.isDefined) match {
      case latestEvent :: others => Some(cursorUrl(page.extraParams.updated("uuid[to]", latestEvent.uuid.get), None))
      case _ => None
    }
  }

  private def nextPage(cursor: Option[String]) = cursorUrl(page.extraParams.filterKeys(_ != "uuid[to]"), cursor)


  private def cursorUrl(extraParams: Params, cursor: Option[String]): String =
    "https://" +
      CursorBasedPage(
        page.param,
        page.baseUrl,
        page.path,
        extraParams,
        cursor,
        page.limit
      ).href

  private def mapChildren(events: Seq[JsObject]) = {
    events.map { event =>
      val urn = Urn((event \ "urn").as[String])
      urn.getCollection match {
        case "tracks" => new TrackTimelineItem(event, entityMapper)
        case "playlists" => new PlaylistTimelineItem(event, entityMapper)
      }
    }
  }

}
