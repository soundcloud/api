package com.soundcloud.publicApiStrangler.service

import com.soundcloud.bff.finagle.Request
import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.client.trackmetadata.Track


case class TrackPagination(limit: Option[Int],
                           offset: Option[Int],
                           linkedPartitioning: Boolean,
                           createdAtFrom: Option[String],
                           createdAtTo: Option[String]) {

  def calculateTrackUrnPage(trackUrns: List[Urn]): Set[Urn] = {
    val start = offset.getOrElse(0)
    val end = start + limit.getOrElse(Int.MaxValue)
    // sort by id desc
    trackUrns.sortBy(-_.getIdentifier.toInt).slice(start, end).toSet
  }

  def calculateFinalPage(tracks: List[Track]): List[Track] = {
    tracks.sortBy(-_.urn.getIdentifier.toInt)
  }

}

object TrackPagination {
  def fromRequest(req: Request) = {
    TrackPagination(
      req.params.getInt("limit"),
      req.params.getInt("offset"),
      req.params.get("linked_partitioning").isDefined,
      req.params.get("created_at[from]"),
      req.params.get("created_at[to]"))
  }
}
