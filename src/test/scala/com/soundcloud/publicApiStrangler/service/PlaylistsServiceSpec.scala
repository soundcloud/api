package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.{UserSession, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.client.mothership.MoshimoshiClient
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackRepresentationSpecContext,
  TrackRepresentationsService
}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.ParamMap
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import com.soundcloud.outcome._
import proto.soundcloud.common.session.{UserSession => ProtoUserSession}
import proto.soundcloud.playlists.api.{
  GetVisiblePlaylistsRequest,
  GetVisiblePlaylistsResponse,
  PlaylistPagination,
  PlaylistRequest,
  PlaylistResponse,
  Playlist => ProtoPlaylist,
  PlaylistsService => PlaylistsTwirpService,
  TrackRequest => ProtoTrackRequest
}

class PlaylistsServiceSpec extends UnitSpecification {
  trait Context extends Scope with TrackRepresentationSpecContext {
    val offsetBasedPagination =
      OffsetBasedPagination("https://api.soundcloud.com", "/playlists", ParamMap(), Some(2), 2)
    val requestingUserUrn = Urn("soundcloud", "users", "1")
    val playlistOwner = defaultUser
    val requestedPlaylistUrn = Urn("soundcloud", "playlists", "1")
    val requestedPlaylistTrackUrn = Urn("soundcloud", "tracks", "1")

    val requestedPlaylistTrack = createTrackRepresentation()

    val candidateSecretToken = Some("s3creT")
    val pagination = PlaylistPagination(cursor = Some("2"), limit = 2)

    val session: UserSession = new UserSessionBuilder().setUser(requestingUserUrn).build()
    val protoSession: ProtoUserSession = session.asProtoSession

    val playlistsTwirpServiceMock = mock[PlaylistsTwirpService]
    val trackServiceMock = mock[TrackRepresentationsService]
    val moshimoshiClientMock = mock[MoshimoshiClient]

    val playlistsService = new PlaylistsService(playlistsTwirpServiceMock, trackServiceMock, moshimoshiClientMock)
  }

  "fetchPlaylist" >> {
    trait SuccessContext extends Context {
      val requestWithPagination = PlaylistRequest(
        urn = requestedPlaylistUrn.toString,
        secretToken = candidateSecretToken,
        pagination = Some(pagination)
      )

      val requestWithoutPagination = PlaylistRequest(
        urn = requestedPlaylistUrn.toString,
        secretToken = candidateSecretToken,
        pagination = None
      )

      val getVisiblePlaylistWithPagination =
        GetVisiblePlaylistsRequest(playlistRequests = Seq(requestWithPagination), userSession = Some(protoSession))
      val getVisiblePlaylist =
        GetVisiblePlaylistsRequest(playlistRequests = Seq(requestWithoutPagination), userSession = Some(protoSession))

      val protoPlaylist =
        ProtoPlaylist(
          urn = requestedPlaylistUrn.toString,
          userUrn = playlistOwner.urn.toString,
          labelId = Some(labelUrn.identifier)
        )

      val protoTracks = Seq(ProtoTrackRequest(urn = requestedPlaylistTrackUrn.toString))

      when(moshimoshiClientMock.fetchUserObjects(session, Set(playlistOwner.urn)))
        .thenReturn(Future.value(List(playlistOwner)))
      when(moshimoshiClientMock.fetchUserObjects(session, Set(labelUrn))).thenReturn(Future.value(List(defaultLabel)))
    }

    trait NotFoundContext extends Context {
      val request = PlaylistRequest(
        urn = requestedPlaylistUrn.toString,
        secretToken = candidateSecretToken
      )

      val getVisiblePlaylist =
        GetVisiblePlaylistsRequest(playlistRequests = Seq(request), userSession = Some(protoSession))

      val expectedPlaylistResponse = GetVisiblePlaylistsResponse(playlistResponse = Seq(
        PlaylistResponse(
          playlist = None,
          trackRequests = Seq.empty,
          pagination = None
        )
      )
      )

      when(playlistsTwirpServiceMock.getVisiblePlaylists(getVisiblePlaylistsRequest = getVisiblePlaylist))
        .thenReturn(Future.value(expectedPlaylistResponse))
    }

    "can fetch a playlist given playlist, secret token, and pagination" in new SuccessContext {
      val expectedPlaylistResponse = GetVisiblePlaylistsResponse(playlistResponse = Seq(
        PlaylistResponse(
          playlist = Some(protoPlaylist),
          trackRequests = protoTracks,
          pagination = Some(PlaylistPagination(cursor = Some("4"), limit = 2))
        )
      )
      )

      when(playlistsTwirpServiceMock.getVisiblePlaylists(getVisiblePlaylistsRequest = getVisiblePlaylistWithPagination))
        .thenReturn(Future.value(expectedPlaylistResponse))
      when(
        trackServiceMock.tracks(session, List(TrackRequest(urn = requestedPlaylistTrackUrn, secretToken = None)))
      ).thenReturn(Future.value(List(requestedPlaylistTrack)))

      val result =
        Await.result(
          playlistsService
            .fetchPlaylist(session, requestedPlaylistUrn, candidateSecretToken, Some(offsetBasedPagination))
        )

      result match {
        case Good(playlist) =>
          playlist.id ==== requestedPlaylistUrn.identifier.toLong
          playlist.userId ==== playlistOwner.urn.identifier.toLong
          playlist.tracks.length ==== 1
          playlist.tracks(0).id ==== requestedPlaylistTrack.id
        case _ => failure(s"returned ${result.toString} instead of Good(_)")
      }
    }

    "can fetch a playlist without pagination" in new SuccessContext {
      val expectedPlaylistResponse = GetVisiblePlaylistsResponse(playlistResponse = Seq(
        PlaylistResponse(
          playlist = Some(protoPlaylist),
          trackRequests = protoTracks,
          pagination = None
        )
      )
      )

      when(playlistsTwirpServiceMock.getVisiblePlaylists(getVisiblePlaylistsRequest = getVisiblePlaylist))
        .thenReturn(Future.value(expectedPlaylistResponse))
      when(
        trackServiceMock.tracks(session, List(TrackRequest(urn = requestedPlaylistTrackUrn, secretToken = None)))
      ).thenReturn(Future.value(List(requestedPlaylistTrack)))

      val result =
        Await.result(
          playlistsService
            .fetchPlaylist(session, requestedPlaylistUrn, candidateSecretToken, None)
        )

      result match {
        case Good(playlist) =>
          playlist.id ==== requestedPlaylistUrn.identifier.toLong
          playlist.userId ==== playlistOwner.urn.identifier.toLong
          playlist.tracks.length ==== 1
          playlist.tracks(0).id ==== requestedPlaylistTrack.id
        case _ => failure(s"returned ${result.toString} instead of Good(_)")
      }
    }

    "returns NotFound if no playlist returned from client" in new NotFoundContext {
      val result =
        Await.result(
          playlistsService
            .fetchPlaylist(session, requestedPlaylistUrn, candidateSecretToken, None)
        )

      result ==== Bad(NotFound("playlist not found"))
    }
  }
}
