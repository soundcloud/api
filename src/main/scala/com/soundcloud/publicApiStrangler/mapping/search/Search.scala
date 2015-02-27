package com.soundcloud.publicApiStrangler.mapping.search

import com.soundcloud.bff._
import com.soundcloud.bff.nextbff.mapping.JsonMapping
import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.publicApiStrangler.mapper.search.SearchEntityMapper
import com.soundcloud.scalakit._
import play.api.libs.json.JsValue

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

  val collection = (json \ "docs")
    .as[List[JsObject]]
    .map(doc => Urn((doc \ "urn").as[String]))
    .map(entityMapper.embed)

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
