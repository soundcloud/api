package com.soundcloud.publicApiStrangler.service.playlists.representation

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import proto.soundcloud.playlists.api.{PlaylistPagination, TrackRequest => ProtoTrackRequest}

case class PlaylistTrackRequests(requests: List[TrackRequest], pagination: Option[OffsetBasedPagination])

object PlaylistTrackRequests {
  def build(
      trackRequests: List[ProtoTrackRequest],
      currentPagination: Option[OffsetBasedPagination],
      nextPagination: Option[PlaylistPagination]
  ): PlaylistTrackRequests = {
    val nextPage = (currentPagination, nextPagination) match {
      case (Some(current), Some(next)) => Some(current.nextPage(next.cursor.map(_.toInt).get))
      case _ => None
    }

    PlaylistTrackRequests(
      trackRequests.map(request => TrackRequest(Urn.parse(request.urn).get, request.secretToken)),
      nextPage
    )
  }
}
