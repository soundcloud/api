package com.soundcloud.publicApiStrangler.mapping.search

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonValue}
import com.soundcloud.bff.nextbff.mapper.EmbeddedItem
import com.soundcloud.bff.nextbff.mapping.JsonMapping
import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.CommonJsonFormats._
import com.soundcloud.publicApiStrangler.mapper.search.SearchEntityMapper
import play.api.libs.json.{JsObject, JsValue}

trait Pagination[T] {
  def next_href: Option[String]
}

trait SearchFacet extends JsonMapping {
  val name = (json \ "name").as[String]
  val facets = (json \ "facets").as[Seq[JsValue]].map {
    f =>
      new JsonMapping(f) {
        val filter = (f \ "filter").as[String]
        val count = (f \ "count").as[Int]
        val value = (f \ "value").as[String]
      }
  }
}

// when we want to support faceted searches (the /search/* endpoints),
// SearchMapper should return new JsonMapping(json) with Search with Faceted
trait Faceted {
  this: Search =>

  val facets = (json \ "facets").as[Option[Seq[JsObject]]].map { facetsJson =>
    facetsJson.map(facet => new JsonMapping(facet) with SearchFacet)
  }
}

trait Search extends JsonMapping {
  def entityMapper: SearchEntityMapper

  // ignored so that implementations can choose how to expose the results
  @JsonIgnore
  protected val mapSearchResults = {
    val urns = (json \ "docs").as[List[JsObject]]
      .map(doc => (doc \ "urn").as[Urn])
    entityMapper.embed(urns)
  }

}

trait LegacySearch extends Search {

  @JsonValue
  def value = mapSearchResults

}

trait PaginatedSearch extends Search with Pagination[EmbeddedItem[JsonMapping]] {

  val collection = mapSearchResults

  lazy val next_href = {
    val nextOffset = (json \ "offset").as[Int] + (json \ "limit").as[Int]
    val hasNext = nextOffset < (json \ "total_results").as[Int]
    if (hasNext)
      Some(currentPage.next(nextOffset).href)
    else
      None
  }

  protected def currentPage: OffsetBasedPage[_]
}
