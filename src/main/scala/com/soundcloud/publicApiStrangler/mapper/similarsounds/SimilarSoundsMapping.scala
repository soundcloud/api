package com.soundcloud.publicApiStrangler.mapper.similarsounds

import com.soundcloud.bff.nextbff.mapper.EmbeddedList
import com.soundcloud.bff.nextbff.mapping.JsonMapping
import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.mapper.search.SearchEntityMapper
import com.soundcloud.publicApiStrangler.support.mapping.ObjectMapping

/**
  * Maps from the list of all available similar tracks to the specific page requested
  */
trait SimilarSoundsMapping extends ObjectMapping[SimilarSounds] {
  def currentPage: OffsetBasedPage[_]

  def searchEntityMapper: SearchEntityMapper

  val collection: EmbeddedList[JsonMapping] = searchEntityMapper.embed(similarTracksInPage)

  lazy val next_href = {
    val nextOffset = currentPage.offset + currentPage.limit
    if (hasNext)
      Some(currentPage.next(nextOffset).href)
    else
      None
  }

  val version = "baseline"

  private def similarTracksInPage: List[Urn] =
    resource.similarTracks.slice(currentPage.offset, currentPage.offset + currentPage.limit).toList

  private def hasNext: Boolean =
    resource.similarTracks.drop(currentPage.offset + currentPage.limit).nonEmpty
}

object SimilarSoundsMapping {
  val LinkedPartitioning = "linked_partitioning"
}
