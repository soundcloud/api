package com.soundcloud.apipublic.service.playlists.representation

import proto.soundcloud.playlists.api.Counts

case class PlaylistCounts(
    likes: Option[Long],
    reposts: Option[Long]
)

object PlaylistCounts {
  def fromProto(counts: Counts) =
    PlaylistCounts(
      counts.likes,
      counts.reposts
    )
}
