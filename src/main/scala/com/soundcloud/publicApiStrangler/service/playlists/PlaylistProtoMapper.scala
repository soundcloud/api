package com.soundcloud.publicApiStrangler.service.playlists

import com.soundcloud.jvmkit.module.twirp.proto.WellKnownOps._
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.playlists.representation.{PlaylistTrackRequests, VisiblePlaylist}
import proto.soundcloud.playlists.api.{
  Playlist,
  PlaylistPagination,
  PlaylistResponse,
  TrackRequest => ProtoTrackRequest
}

class PlaylistProtoMapper {
  def createPlaylist(
      playlist: Playlist,
      trackRequests: Seq[ProtoTrackRequest],
      currentPagination: Option[OffsetBasedPagination],
      nextPagination: Option[PlaylistPagination]
  ): VisiblePlaylist = {
    VisiblePlaylist(
      urn = playlist.urn,
      title = playlist.title,
      description = playlist.description,
      createdAt = playlist.createdAt.map(_.asInstant),
      duration = playlist.duration,
      genre = playlist.genre,
      permalink = playlist.permalink,
      permalinkUrl = playlist.permalinkUrl,
      artworkUrl = playlist.artworkUrl,
      trackCount = playlist.trackCount,
      userTags = playlist.userTags.toList,
      releaseDate = playlist.releaseDate.map(_.asInstant),
      public = playlist.public,
      sharing = playlist.sharing,
      secretToken = playlist.secretToken,
      updatedAt = playlist.updatedAt.map(_.asInstant),
      userUrn = playlist.userUrn,
      likesCount = playlist.likesCount,
      isAlbum = playlist.isAlbum,
      setType = playlist.setType,
      managedByFeeds = playlist.managedByFeeds,
      repostsCount = playlist.repostsCount,
      publishedAt = playlist.publishedAt.map(_.asInstant),
      embeddableBy = playlist.embeddableBy,
      license = playlist.license,
      labelName = playlist.labelName,
      labelId = playlist.labelId,
      purchaseTitle = playlist.purchaseTitle,
      purchaseUrl = playlist.purchaseUrl,
      cursor = playlist.cursor,
      ean = playlist.ean,
      streamable = playlist.streamable,
      uri = playlist.uri,
      trackRequests = PlaylistTrackRequests.build(trackRequests.toList, currentPagination, nextPagination)
    )
  }

  def apply(
      protoPlaylist: PlaylistResponse,
      pagination: Option[OffsetBasedPagination]
  ): Option[VisiblePlaylist] = {
    protoPlaylist.playlist match {
      case Some(playlist) =>
        Some(createPlaylist(playlist, protoPlaylist.trackRequests, pagination, protoPlaylist.pagination))
      case _ => None
    }
  }
}
