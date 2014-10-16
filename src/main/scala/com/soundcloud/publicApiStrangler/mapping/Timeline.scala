package com.soundcloud.publicApiStrangler.mapping

import com.soundcloud.publicApiStrangler.mapper.{EntitySummaryMapper, EntityMapper}
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.bff.nextbff.pagination.{PageBuilder, CursorBasedPage}
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.finagle.jsonservice.Params
import play.api.libs.json.{JsObject, JsValue}

abstract class Timeline(json: JsValue)(implicit context: MappingContext) extends JsonMapping(json) {

  val collection: Seq[JsonMapping] = mapChildren(events)
  val nextHref = nextPage((json \ "meta" \ "next_page_cursor").asOpt[String])

  def events = (json \ "events").as[Seq[JsObject]]

  protected def nextPage(cursor: Option[String]): Option[String]
  protected def mapChildren(events: Seq[JsObject]): Seq[JsonMapping]

}
