package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.client.mothership.MoshimoshiClient
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.playlists.PlaylistProtoMapper
import com.soundcloud.publicApiStrangler.service.playlists.representation.{Playlist, VisiblePlaylist}
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackRepresentationsService
import com.twitter.util.Future
import proto.soundcloud.playlists.api.{
  GetVisiblePlaylistsRequest,
  PlaylistPagination,
  PlaylistRequest,
  PlaylistsService => PlaylistsTwirpService
}

class PlaylistsService(
    playlistsTwirpService: PlaylistsTwirpService,
    tracksService: TrackRepresentationsService,
    moshimoshiClient: MoshimoshiClient,
    playlistProtoMapper: PlaylistProtoMapper = new PlaylistProtoMapper()
) {

  def fetchPlaylist(
      session: UserSession,
      playlistUrn: Urn,
      candidateSecretToken: Option[String],
      pagination: Option[OffsetBasedPagination]
  ): Future[Outcome[Playlist]] = {
    for {
      visiblePlaylist <- getPlaylistObject(session, playlistUrn, pagination, candidateSecretToken)
      maybePlaylist <- visiblePlaylist
        .map(playlist => getFullPlaylist(playlist, session))
        .getOrElse(Future.None)
    } yield {
      maybePlaylist match {
        case Some(playlist) => playlist.good
        case _ => NotFound("playlist not found").bad
      }
    }
  }

  private def getFullPlaylist(
      visiblePlaylist: VisiblePlaylist,
      session: UserSession
  ): Future[Option[Playlist]] = {
    for {
      tracks <- tracksService.tracks(session, visiblePlaylist.trackRequests)
      playlistOwner <- moshimoshiClient
        .fetchUserObjects(session, Set(Urn.parse(visiblePlaylist.userUrn).get))
        .map(_.head)
      maybeLabelOwner <- visiblePlaylist.labelId
        .map(id => moshimoshiClient.fetchUserObjects(session, Set(Urn("soundcloud", "users", id))).map(_.headOption))
        .getOrElse(Future.None)
    } yield Some(
      Playlist.fromVisiblePlaylist(visiblePlaylist, tracks, playlistOwner, maybeLabelOwner, session.user)
    )
  }

  private def getPlaylistObject(
      session: UserSession,
      playlistUrn: Urn,
      pagination: Option[OffsetBasedPagination],
      candidateSecretToken: Option[String]
  ): Future[Option[VisiblePlaylist]] = {
    val playlistPagination =
      pagination.map(p => PlaylistPagination(cursor = p.offset.map(_.toString), limit = p.limit))
    val playlistRequest = PlaylistRequest(
      urn = playlistUrn.toString,
      secretToken = candidateSecretToken,
      pagination = playlistPagination
    )

    val getVisiblePlaylistsRequest = createGetVisiblePlaylistRequest(session, Seq(playlistRequest))

    playlistsTwirpService
      .getVisiblePlaylists(getVisiblePlaylistsRequest)
      .map(response => {
        response.playlistResponse.toList.flatMap(playlistProtoMapper.apply).headOption
      })
  }

  private def createGetVisiblePlaylistRequest(
      session: UserSession,
      playlistRequests: Seq[PlaylistRequest]
  ): GetVisiblePlaylistsRequest = {
    val protoUserSession = session.asProtoSession

    val subscriptionCountryCode = session.getExtraHeader(UserSession.CONSUMER_SUBSCRIPTION_COUNTRY)

    GetVisiblePlaylistsRequest(
      playlistRequests = playlistRequests,
      userSession = Some(protoUserSession),
      subscriptionCountryCode = if (subscriptionCountryCode != null) Some(subscriptionCountryCode) else None
    )
  }
}
