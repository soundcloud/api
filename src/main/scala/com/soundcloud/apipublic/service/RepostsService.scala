package com.soundcloud.apipublic.service

import com.soundcloud.apipublic.client.mothership.response.representation.UserRepresentation
import com.soundcloud.apipublic.client.reposts.RepostsClient
import com.soundcloud.apipublic.client.reposts.RepostsClient.{Created, Deleted, Failed, Forbidden, NotFound, Result}
import com.soundcloud.apipublic.client.tracks.TrackRequest
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.pagination.CursorBasedPagination
import com.soundcloud.apipublic.service.playlists.PlaylistRequest
import com.soundcloud.apipublic.service.playlists.representation.Playlist
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.trackrepresentation.{TrackRepresentation, TrackRepresentationsService}
import com.soundcloud.apipublic.service.users.UserRepresentationsService
import com.soundcloud.jvmkit.module.outcome.{ApplicationError, Bad, Good, GoodOps, Outcome}
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps.JvmkitSessionExt
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.util.Future
import proto.soundcloud.tracks.api.{
  RepostTrackRequest,
  TrackRepostersPagination,
  TrackRepostersRequest,
  TrackRepostersResponse,
  RepostsService => TrackRepostsService
}

class RepostsService(
    userRepresentationService: UserRepresentationsService,
    repostsClient: RepostsClient,
    trackRepostsService: TrackRepostsService,
    trackRepresentationsService: TrackRepresentationsService,
    playlistsService: PlaylistsService
) {

  def getTrackReposts(
      session: UserSession,
      userUrn: Urn,
      access: AccessParams,
      pagination: CursorBasedPagination
  ): Future[Outcome[Collection[TrackRepresentation]]] = {
    repostsClient
      .trackReposts(session, userUrn, pagination.pageSize, pagination.cursor)
      .flatMap { reposts =>
        trackRepresentationsService
          .tracks(session, reposts.urns.map(TrackRequest(_, None)), access)
          .map { tracks =>
            val nextHref = reposts.nextCursor.map(cursor => pagination.nextPage(cursor)).map(_.normalizedHref)
            Collection(tracks, nextHref).good
          }
      }
  }

  def getPlaylistReposts(
      session: UserSession,
      userUrn: Urn,
      pagination: CursorBasedPagination
  ): Future[Outcome[Collection[Playlist]]] = {
    repostsClient
      .playlistReposts(session, userUrn, pagination.pageSize, pagination.cursor)
      .flatMap { reposts =>
        playlistsService
          .fetchPlaylistsMetadataOnly(session, reposts.urns.map(PlaylistRequest(_, None)))
          .map { playlists =>
            val nextHref = reposts.nextCursor.map(cursor => pagination.nextPage(cursor)).map(_.normalizedHref)
            Collection(playlists, nextHref).good
          }
      }
  }

  def createTracksRepost(session: UserSession, trackUrn: Urn): Future[Result] = {
    val request = RepostTrackRequest(Some(session.asProtoSession), trackUrn.toString)

    trackRepostsService
      .repostTrack(request)
      .map(_ => Created)
      .handle {
        case TwinagleException(ErrorCode.NotFound, _, _, _) => NotFound
        case TwinagleException(ErrorCode.PermissionDenied, _, _, _) => Forbidden
        case TwinagleException(_, _, _, _) => Failed
      }
  }

  def deleteTracksRepost(session: UserSession, trackUrn: Urn): Future[Result] = {
    val request = RepostTrackRequest(Some(session.asProtoSession), trackUrn.toString)

    trackRepostsService
      .deleteTrackRepost(request)
      .map(_ => Deleted)
      .handle {
        case TwinagleException(ErrorCode.NotFound, _, _, _) => NotFound
        case TwinagleException(_, _, _, _) => Failed
      }
  }

  def createPlaylistsRepost(session: UserSession, playlist: Urn): Future[Result] = {
    repostsClient.createRepost(session, playlist)
  }

  def deletePlaylistsRepost(session: UserSession, playlist: Urn): Future[Result] = {
    repostsClient.deleteRepost(session, playlist)
  }

  def getPlaylistReposters(
      session: UserSession,
      target: Urn,
      pagination: CursorBasedPagination
  ): Future[Collection[UserRepresentation]] = {
    repostsClient
      .reposters(session, target, pagination.pageSize, pagination.cursor)
      .flatMap { reposts =>
        userRepresentationService
          .users(session, reposts.urns)
          .map { users =>
            val nextHref = reposts.nextCursor.map(cursor => pagination.nextPage(cursor)).map(_.normalizedHref)
            Collection(users, nextHref)
          }
      }
  }

  def getTrackReposters(
      session: UserSession,
      target: Urn,
      pagination: CursorBasedPagination
  ): Future[Outcome[Collection[UserRepresentation]]] = {
    fetchTrackReposters(session, target, pagination).flatMap {
      case Good(response) =>
        userRepresentationService
          .users(session, response.userUrns.map(u => Urn.parse(u).get))
          .map { users =>
            val nextHref = response.cursor.map(cursor => pagination.nextPage(cursor)).map(_.normalizedHref)
            Collection(users, nextHref).good
          }
      case Bad(err) => Future.value(err.bad)
      case _ => Future.value(com.soundcloud.jvmkit.module.outcome.NotFound().bad)
    }
  }

  private def fetchTrackReposters(
      session: UserSession,
      trackUrn: Urn,
      pagination: CursorBasedPagination
  ): Future[Outcome[TrackRepostersResponse]] = {
    val trackRepostersPagination = TrackRepostersPagination(limit = pagination.pageSize, cursor = pagination.cursor)
    val request = TrackRepostersRequest(
      userSession = Some(session.asProtoSession),
      trackUrn = trackUrn.toString,
      pagination = Some(trackRepostersPagination)
    )

    trackRepostsService
      .getTrackReposters(request)
      .map(res => TrackRepostersResponse(userUrns = res.userUrns, cursor = res.cursor).good)
      .handle {
        case e @ TwinagleException(_, _, _, _) => ApplicationError.fromTwinagleException(e).bad
      }
  }

}
