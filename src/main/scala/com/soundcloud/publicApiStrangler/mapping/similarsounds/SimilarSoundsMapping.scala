package com.soundcloud.publicApiStrangler.mapping.similarsounds

import com.soundcloud.bff.nextbff.mapper.EmbeddedList
import com.soundcloud.bff.nextbff.mapping.JsonMapping
import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.publicApiStrangler.mapper.search.SearchEntityMapper
import com.soundcloud.publicApiStrangler.support.mapping.ObjectMapping
import com.soundcloud.service.response.representation.SimilarSounds

trait SimilarSoundsMapping extends ObjectMapping[SimilarSounds] {

  def currentPage: OffsetBasedPage[_]

  def searchEntityMapper: SearchEntityMapper

  val collection: EmbeddedList[JsonMapping] = searchEntityMapper.embed(resource.similarTracks.toList)

  lazy val next_href = {
    val nextOffset = currentPage.offset + currentPage.limit
    val hasNext = !resource.meta.nextHref.isEmpty
    if (hasNext)
      Some(currentPage.next(nextOffset).href)
    else
      None
  }

}

object SimilarSoundsMapping {
  val LinkedPartitioning = "linked_partitioning"
}

