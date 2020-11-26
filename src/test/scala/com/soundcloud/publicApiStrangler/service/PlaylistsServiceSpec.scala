package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.{UserSession, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.client.mothership.MoshimoshiClient
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackRepresentation,
  TrackRepresentationSpecContext,
  TrackRepresentationsService
}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.ParamMap
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.service.playlists.PlaylistRequest
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import proto.soundcloud.common.session.{UserSession => ProtoUserSession}
import proto.soundcloud.playlists.api.{
  GetVisiblePlaylistsRequest,
  GetVisiblePlaylistsResponse,
  PlaylistPagination,
  PlaylistResponse,
  Playlist => ProtoPlaylist,
  PlaylistRequest => ProtoPlaylistRequest,
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
    val requestedPlaylistTrackUrn1 = Urn("soundcloud", "tracks", "2")
    val requestedPlaylistTrackUrn2 = Urn("soundcloud", "tracks", "3")

    val requestedPlaylistTrack = createTrackRepresentation()
    val requestedPlaylistTrack1 = createTrackRepresentation()
    val requestedPlaylistTrack2 = createTrackRepresentation()

    val candidateSecretToken = Some("s3creT")
    val pagination = PlaylistPagination(cursor = Some("2"), limit = 2)

    val session: UserSession = new UserSessionBuilder().setUser(requestingUserUrn).build()
    val protoSession: ProtoUserSession = session.asProtoSession

    val playlistsTwirpServiceMock = mock[PlaylistsTwirpService]
    val trackServiceMock = mock[TrackRepresentationsService]
    val moshimoshiClientMock = mock[MoshimoshiClient]
    val telemetry = Telemetry.defaultInstance
    val exceptionCollector = new ExceptionCollector(telemetry)

    val playlistsService =
      new PlaylistsService(playlistsTwirpServiceMock, trackServiceMock, moshimoshiClientMock, exceptionCollector)
    val playlistRequests = List(PlaylistRequest(requestedPlaylistUrn, candidateSecretToken))
  }

  trait SuccessContext extends Context {
    val requestWithPagination = ProtoPlaylistRequest(
      urn = requestedPlaylistUrn.toString,
      secretToken = candidateSecretToken,
      pagination = Some(pagination)
    )

    val requestWithoutPagination = ProtoPlaylistRequest(
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

    val protoTrack = Seq(ProtoTrackRequest(urn = requestedPlaylistTrackUrn.toString))
    val protoTracks = Seq(
      ProtoTrackRequest(urn = requestedPlaylistTrackUrn.toString),
      ProtoTrackRequest(urn = requestedPlaylistTrackUrn1.toString),
      ProtoTrackRequest(urn = requestedPlaylistTrackUrn2.toString)
    )

    when(moshimoshiClientMock.fetchUserObjects(session, Set(playlistOwner.urn)))
      .thenReturn(Future.value(List(playlistOwner)))
    when(moshimoshiClientMock.fetchUserObjects(session, Set(labelUrn))).thenReturn(Future.value(List(defaultLabel)))

    def setUpMocksForPlaylists(
        pagination: Option[PlaylistPagination],
        visiblePlaylistsRequest: GetVisiblePlaylistsRequest,
        trackRequests: List[TrackRequest],
        requestedPlaylistTracks: List[TrackRepresentation] = List(requestedPlaylistTrack),
        protoTrackRequests: Seq[ProtoTrackRequest] = protoTrack
    ) = {
      val expectedPlaylistResponse = GetVisiblePlaylistsResponse(playlistResponse = Seq(
        PlaylistResponse(
          playlist = Some(protoPlaylist),
          trackRequests = protoTrackRequests,
          pagination = pagination
        )
      )
      )

      when(playlistsTwirpServiceMock.getVisiblePlaylists(getVisiblePlaylistsRequest = visiblePlaylistsRequest))
        .thenReturn(Future.value(expectedPlaylistResponse))
      when(
        trackServiceMock.tracks(session, trackRequests)
      ).thenReturn(Future.value(requestedPlaylistTracks))
    }
  }

  trait NotFoundContext extends Context {
    val request = ProtoPlaylistRequest(
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

  "#fetchPlaylist" >> {
    "can fetch a playlist given playlist, secret token, and pagination" in new SuccessContext {
      setUpMocksForPlaylists(
        pagination = Some(PlaylistPagination(cursor = Some("4"), limit = 2)),
        trackRequests = List(TrackRequest(urn = requestedPlaylistTrackUrn, secretToken = None)),
        visiblePlaylistsRequest = getVisiblePlaylistWithPagination
      )

      val result =
        Await.result(
          playlistsService
            .fetchPlaylist(session, requestedPlaylistUrn, candidateSecretToken, Some(offsetBasedPagination))
        )

      result match {
        case Good(playlist) =>
          playlist.id ==== requestedPlaylistUrn.identifier.toLong
          playlist.userId ==== playlistOwner.urn.identifier.toLong
          playlist.tracks.get.length ==== 1
          playlist.tracks.get(0).id ==== requestedPlaylistTrack.id
        case _ => failure(s"returned ${result.toString} instead of Good(_)")
      }
    }

    "can fetch a playlist without pagination" in new SuccessContext {
      setUpMocksForPlaylists(
        pagination = None,
        visiblePlaylistsRequest = getVisiblePlaylist,
        trackRequests = List(TrackRequest(urn = requestedPlaylistTrackUrn, secretToken = None))
      )

      val result =
        Await.result(
          playlistsService
            .fetchPlaylist(session, requestedPlaylistUrn, candidateSecretToken, None)
        )

      result match {
        case Good(playlist) =>
          playlist.id ==== requestedPlaylistUrn.identifier.toLong
          playlist.userId ==== playlistOwner.urn.identifier.toLong
          playlist.tracks.get.length ==== 1
          playlist.tracks.get(0).id ==== requestedPlaylistTrack.id
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

  "#fetchPlaylistTracks" >> {
    "can retrieve playlist tracks without pagination" in new SuccessContext {
      setUpMocksForPlaylists(
        pagination = None,
        visiblePlaylistsRequest = getVisiblePlaylist,
        requestedPlaylistTracks = List(requestedPlaylistTrack, requestedPlaylistTrack1, requestedPlaylistTrack2),
        trackRequests = List(TrackRequest(urn = requestedPlaylistTrackUrn, secretToken = None))
      )

      val result =
        Await.result(
          playlistsService
            .fetchPlaylistTracks(session, requestedPlaylistUrn, candidateSecretToken, None)
        )

      result match {
        case Good(tracksCollection: Collection[TrackRepresentation]) =>
          tracksCollection.items.length ==== 3
          tracksCollection.items(0).id ==== requestedPlaylistTrack.id
          tracksCollection.items(1).id ==== requestedPlaylistTrack1.id
          tracksCollection.items(2).id ==== requestedPlaylistTrack2.id
          tracksCollection.nextHref.isEmpty
        case _ => failure(s"incorrectly returned ${result.toString}")
      }
    }

    "can retrieve playlist tracks with pagination" in new SuccessContext {
      override val protoTrack: Seq[ProtoTrackRequest] = Seq(protoTracks.head)
      setUpMocksForPlaylists(
        pagination = Some(PlaylistPagination(cursor = Some("2"), limit = 2)),
        visiblePlaylistsRequest = getVisiblePlaylistWithPagination,
        trackRequests = List(TrackRequest(urn = requestedPlaylistTrackUrn, secretToken = None))
      )

      val result =
        Await.result(
          playlistsService
            .fetchPlaylistTracks(session, requestedPlaylistUrn, candidateSecretToken, Some(offsetBasedPagination))
        )

      result match {
        case Good(tracksCollection: Collection[TrackRepresentation]) =>
          tracksCollection.items.length ==== 1
          tracksCollection.items(0).id ==== requestedPlaylistTrack.id
          tracksCollection.nextHref ==== Some("https://api.soundcloud.com/playlists?limit=2&offset=2")
        case _ => failure(s"incorrectly returned ${result.toString}")
      }
    }

    "returns NotFound if no playlist is returned from the client" in new NotFoundContext {
      val result =
        Await.result(
          playlistsService
            .fetchPlaylistTracks(session, requestedPlaylistUrn, candidateSecretToken, None)
        )

      result ==== Bad(NotFound("playlist not found"))
    }
  }

  "#fetchPlaylists" >> {
    "can fetch a list of playlists given playlist, secret token, and pagination" in new SuccessContext {
      setUpMocksForPlaylists(
        pagination = Some(PlaylistPagination(cursor = Some("4"), limit = 2)),
        visiblePlaylistsRequest = getVisiblePlaylistWithPagination,
        trackRequests = List(TrackRequest(urn = requestedPlaylistTrackUrn, secretToken = None))
      )

      val result =
        Await.result(
          playlistsService
            .fetchPlaylists(session, playlistRequests, Some(offsetBasedPagination))
        )

      result.length ==== 1
      result(0).id ==== requestedPlaylistUrn.identifier.toLong
      result(0).userId ==== playlistOwner.urn.identifier.toLong
      result(0).tracks.get.length ==== 1
      result(0).tracks.get(0).id ==== requestedPlaylistTrack.id
    }

    "can fetch a list of playlists without pagination" in new SuccessContext {
      setUpMocksForPlaylists(
        None,
        getVisiblePlaylist,
        trackRequests = List(TrackRequest(urn = requestedPlaylistTrackUrn, secretToken = None))
      )

      val result =
        Await.result(
          playlistsService
            .fetchPlaylists(session, playlistRequests, None)
        )

      result.length ==== 1
      result(0).id ==== requestedPlaylistUrn.identifier.toLong
      result(0).userId ==== playlistOwner.urn.identifier.toLong
      result(0).tracks.get.length ==== 1
      result(0).tracks.get(0).id ==== requestedPlaylistTrack.id
    }

    "returns empty list if no playlist returned from client" in new NotFoundContext {
      val result =
        Await.result(
          playlistsService
            .fetchPlaylists(session, playlistRequests, None)
        )

      result ==== List.empty
    }
  }

  "#fetchPlaylistMetadataOnly" >> {
    "returns playlists without tracks field" in new SuccessContext {
      val emptyPagination = PlaylistPagination()
      val request = requestWithPagination.copy(pagination = Some(emptyPagination))
      val getPlaylistRequest =
        GetVisiblePlaylistsRequest(playlistRequests = Seq(request), userSession = Some(protoSession))

      setUpMocksForPlaylists(
        pagination = None,
        visiblePlaylistsRequest = getPlaylistRequest,
        trackRequests = List.empty,
        requestedPlaylistTracks = List.empty,
        protoTrackRequests = Seq.empty
      )

      val result =
        Await.result(
          playlistsService
            .fetchPlaylistsMetadataOnly(session, List(PlaylistRequest(requestedPlaylistUrn, candidateSecretToken)))
        )

      result.length ==== 1
      result(0).tracks must beEmpty
    }

    "returns an empty list if no playlists found" in new SuccessContext {
      val emptyPagination = PlaylistPagination()
      val request = requestWithPagination.copy(pagination = Some(emptyPagination))
      val getPlaylistRequest =
        GetVisiblePlaylistsRequest(playlistRequests = Seq(request), userSession = Some(protoSession))

      when(playlistsTwirpServiceMock.getVisiblePlaylists(getVisiblePlaylistsRequest = getPlaylistRequest))
        .thenReturn(Future.value(GetVisiblePlaylistsResponse(playlistResponse = Seq.empty)))

      val result =
        Await.result(
          playlistsService
            .fetchPlaylistsMetadataOnly(session, List(PlaylistRequest(requestedPlaylistUrn, candidateSecretToken)))
        )

      result.length ==== 0
    }
  }
}
