package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionHandler.FutureExtensions
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.client.mothership.MoshimoshiClient
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.playlists.representation.{Collection, Playlist, VisiblePlaylist}
import com.soundcloud.publicApiStrangler.service.playlists.{PlaylistProtoMapper, PlaylistRequest}
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackRepresentation, TrackRepresentationsService}
import com.twitter.util.Future
import proto.soundcloud.playlists.api.{
  GetVisiblePlaylistsRequest,
  PlaylistPagination,
  PlaylistRequest => ProtoPlaylistRequest,
  PlaylistsService => PlaylistsTwirpService
}

import scala.util.control.NonFatal

class PlaylistsService(
    playlistsTwirpService: PlaylistsTwirpService,
    tracksService: TrackRepresentationsService,
    moshimoshiClient: MoshimoshiClient,
    exceptionCollector: ExceptionCollector,
    playlistProtoMapper: PlaylistProtoMapper = new PlaylistProtoMapper()
) {

  def fetchPlaylistTracks(
      session: UserSession,
      playlistUrn: Urn,
      candidateSecretToken: Option[String],
      pagination: Option[OffsetBasedPagination]
  ): Future[Outcome[Collection[TrackRepresentation]]] = {
    val playlistRequest = PlaylistRequest(urn = playlistUrn, secretToken = candidateSecretToken)
    for {
      visiblePlaylist <- getPlaylistObjects(session, List(playlistRequest), pagination)
      playlistTrackRequests = visiblePlaylist.map(_.trackRequests).headOption
      tracks <- playlistTrackRequests
        .map(trackRequests => tracksService.tracks(session, trackRequests.requests))
        .getOrElse(Future.value(List.empty))
    } yield {
      playlistTrackRequests match {
        case Some(_) =>
          val nextHref =
            playlistTrackRequests.flatMap(trackRequests => trackRequests.pagination.map(_.normalizedHref))
          Collection(tracks, nextHref).good
        case _ => NotFound("playlist not found").bad
      }
    }
  }

  def fetchPlaylist(
      session: UserSession,
      playlistUrn: Urn,
      candidateSecretToken: Option[String],
      pagination: Option[OffsetBasedPagination]
  ): Future[Outcome[Playlist]] = {
    val playlistRequest = PlaylistRequest(urn = playlistUrn, secretToken = candidateSecretToken)
    fetchPlaylists(session, List(playlistRequest), pagination)
      .map(_.headOption)
      .map {
        case Some(playlist) => playlist.good
        case _ => NotFound("playlist not found").bad
      }
  }

  def fetchPlaylists(
      session: UserSession,
      playlistRequests: List[PlaylistRequest],
      pagination: Option[OffsetBasedPagination]
  ): Future[List[Playlist]] = {
    for {
      visiblePlaylists <- getPlaylistObjects(session, playlistRequests, pagination)
      maybePlaylists <- Future
        .collect(
          visiblePlaylists.map(playlist =>
            getFullPlaylist(playlist, session).handleAndReport(exceptionCollector) {
              case NonFatal(_) => None
            }
          )
        )
        .map(_.toList)
      playlists = maybePlaylists.flatten
    } yield playlists
  }

  private def getFullPlaylist(
      visiblePlaylist: VisiblePlaylist,
      session: UserSession
  ): Future[Option[Playlist]] = {
    for {
      tracks <- tracksService.tracks(session, visiblePlaylist.trackRequests.requests)
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

  private def getPlaylistObjects(
      session: UserSession,
      playlistRequests: List[PlaylistRequest],
      pagination: Option[OffsetBasedPagination]
  ): Future[List[VisiblePlaylist]] = {
    val playlistPagination =
      pagination.map(p => PlaylistPagination(cursor = p.offset.map(_.toString), limit = p.limit))
    val protoPlaylistRequests = playlistRequests.map(request =>
      ProtoPlaylistRequest(
        urn = request.urn.toString,
        secretToken = request.secretToken,
        pagination = playlistPagination
      )
    )

    val getVisiblePlaylistsRequest = createGetVisiblePlaylistRequest(session, protoPlaylistRequests)

    playlistsTwirpService
      .getVisiblePlaylists(getVisiblePlaylistsRequest)
      .map(response => {
        response.playlistResponse.toList.flatMap(response => playlistProtoMapper.apply(response, pagination))
      })
  }

  private def createGetVisiblePlaylistRequest(
      session: UserSession,
      playlistRequests: Seq[ProtoPlaylistRequest]
  ): GetVisiblePlaylistsRequest = {
    val protoUserSession = session.asProtoSession

    val subscriptionCountryCode = session.getExtraHeader(UserSession.CONSUMER_SUBSCRIPTION_COUNTRY)

    GetVisiblePlaylistsRequest(
      playlistRequests = playlistRequests,
      userSession = Some(protoUserSession),
      subscriptionCountryCode = Option(subscriptionCountryCode)
    )
  }
}
