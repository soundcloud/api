package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.liebling.{LieblingClient, LikesCount}
import com.soundcloud.publicApiStrangler.client.mothership.MoshimoshiClient
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.UserRepresentation
import com.soundcloud.publicApiStrangler.handler.support.requestParser.AccessParams
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.playlists.representation.{Playlist, VisiblePlaylist}
import com.soundcloud.publicApiStrangler.service.playlists.{PlaylistProtoMapper, PlaylistRequest}
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackRepresentation, TrackRepresentationsService}
import com.twitter.util.Future
import proto.soundcloud.playlists.api.{
  GetVisiblePlaylistsRequest,
  PlaylistPagination,
  PlaylistResponse,
  PlaylistRequest => ProtoPlaylistRequest,
  PlaylistsService => PlaylistsTwirpService
}

class PlaylistsService(
    playlistsTwirpService: PlaylistsTwirpService,
    tracksService: TrackRepresentationsService,
    moshimoshiClient: MoshimoshiClient,
    lieblingClient: LieblingClient,
    exceptionCollector: ExceptionCollector,
    playlistProtoMapper: PlaylistProtoMapper = new PlaylistProtoMapper()
) {

  def fetchPlaylistTracks(
      session: UserSession,
      playlistUrn: Urn,
      candidateSecretToken: Option[String],
      access: AccessParams,
      pagination: Option[OffsetBasedPagination]
  ): Future[Outcome[Collection[TrackRepresentation]]] = {
    val playlistRequest = PlaylistRequest(urn = playlistUrn, secretToken = candidateSecretToken)
    val playlistPagination =
      pagination.map(p => PlaylistPagination(cursor = p.offset.map(_.toString), limit = p.limit))

    for {
      visiblePlaylistObject <- getPlaylistObjects(session, List(playlistRequest), playlistPagination)
      visiblePlaylist = visiblePlaylistObject.flatMap(response => playlistProtoMapper.apply(response, pagination))
      playlistTrackRequests = visiblePlaylist.map(_.trackRequests).headOption
      tracks <- playlistTrackRequests
        .map(trackRequests => tracksService.tracks(session, trackRequests.requests, access))
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
      access: AccessParams,
      pagination: Option[OffsetBasedPagination],
      showTracks: Option[Boolean]
  ): Future[Outcome[Playlist]] = {
    val playlistRequest = PlaylistRequest(urn = playlistUrn, secretToken = candidateSecretToken)
    fetchPlaylists(session, List(playlistRequest), access, pagination, showTracks)
      .map(_.headOption)
      .map {
        case Some(playlist) => playlist.good
        case _ => NotFound("playlist not found").bad
      }
  }

  def fetchPlaylists(
      session: UserSession,
      playlistRequests: List[PlaylistRequest],
      access: AccessParams,
      pagination: Option[OffsetBasedPagination],
      showTracks: Option[Boolean]
  ): Future[List[Playlist]] = {
    val playlistPagination =
      pagination.map(p => PlaylistPagination(cursor = p.offset.map(_.toString), limit = p.limit))

    for {
      visiblePlaylistObjects <- getPlaylistObjects(session, playlistRequests, playlistPagination)
      visiblePlaylists = visiblePlaylistObjects.flatMap(response => playlistProtoMapper.apply(response, pagination))
      playlists <- resolvePlaylists(visiblePlaylists, session, access, showTracks.getOrElse(true))
    } yield playlists
  }

  def fetchPlaylistsMetadataOnly(
      session: UserSession,
      playlistRequests: List[PlaylistRequest]
  ): Future[List[Playlist]] = {
    // we set the limit to 0 (default) in pagination in order to retrieve no tracks and avoid
    // unnecessary calls to track-metadata in Playlists VAS
    val playlistPagination = PlaylistPagination()

    for {
      visiblePlaylistObjects <- getPlaylistObjects(session, playlistRequests, Some(playlistPagination))
      visiblePlaylists = visiblePlaylistObjects.flatMap(response => playlistProtoMapper.apply(response, None))
      playlists <- resolvePlaylists(visiblePlaylists, session, AccessParams.defaultAccess, showTracks = false)
    } yield playlists
  }

  private def resolvePlaylists(
      visiblePlaylists: List[VisiblePlaylist],
      session: UserSession,
      access: AccessParams,
      showTracks: Boolean
  ): Future[List[Playlist]] = {
    for {
      playlists <- getFullPlaylists(session, visiblePlaylists)
      playlistTracks <- Future
        .collect(
          visiblePlaylists
            .filter(_ => showTracks)
            .map(playlist =>
              tracksService
                .tracks(session, playlist.trackRequests.requests, access)
                .map(tracks => (Urn.parse(playlist.urn).get.identifier.toLong, tracks))
            )
        )
        .map(_.toMap)
      enrichedPlaylists = if (showTracks)
        playlists.map(playlist => Playlist.enrichPlaylistWithTracks(playlist, playlistTracks(playlist.id)))
      else playlists
    } yield enrichedPlaylists
  }

  private def getFullPlaylists(
      session: UserSession,
      visiblePlaylists: List[VisiblePlaylist]
  ): Future[List[Playlist]] = {
    val urns = visiblePlaylists.map(_.urn)

    if (urns.isEmpty) Future.value(List.empty)
    else {
      val userUrns: Seq[Urn] = visiblePlaylists.map(playlist => Urn.parse(playlist.userUrn).get) ++
        visiblePlaylists.flatMap(playlist => playlist.labelId.map(id => Urn("soundcloud", "users", id)))

      for {
        (users, likes) <- Future.join(
          moshimoshiClient.fetchUserObjects(session, userUrns.toSet),
          lieblingClient.likeCounts(session, urns.map(urn => Urn.parse(urn).get))
        )
      } yield mapPlaylists(urns, visiblePlaylists, users, likes, session)
    }
  }

  private def mapPlaylists(
      urns: List[String],
      visiblePlaylists: List[VisiblePlaylist],
      users: List[UserRepresentation],
      likeCounts: List[LikesCount],
      session: UserSession
  ): List[Playlist] = {
    for {
      urn <- urns
      playlist <- visiblePlaylists.find(_.urn == urn)
      likesCount <- likeCounts.find(_.target_urn == Urn.parse(urn).get).map(_.likes_count)
      owner <- users.find(_.urn.toString == playlist.userUrn)
      maybeLabelOwner = playlist.labelId.flatMap(id => users.find(_.urn.identifier == id))
    } yield Playlist.fromVisiblePlaylist(playlist, owner, maybeLabelOwner, session.user, likesCount)
  }

  private def getPlaylistObjects(
      session: UserSession,
      playlistRequests: List[PlaylistRequest],
      pagination: Option[PlaylistPagination]
  ): Future[List[PlaylistResponse]] = {
    val protoPlaylistRequests = playlistRequests.map(request =>
      ProtoPlaylistRequest(
        urn = request.urn.toString,
        secretToken = request.secretToken,
        pagination = pagination
      )
    )

    val getVisiblePlaylistsRequest = createGetVisiblePlaylistRequest(session, protoPlaylistRequests)

    playlistsTwirpService
      .getVisiblePlaylists(getVisiblePlaylistsRequest)
      .map(response => {
        response.playlistResponse.toList
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
