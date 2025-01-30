package com.soundcloud.apipublic.service

import com.soundcloud.apipublic.client.mothership.request.representation.Value
import com.soundcloud.apipublic.client.mothership.{MoshimoshiClient, PlaylistArtworkUpdate}
import com.soundcloud.apipublic.client.tracks.TrackRequest
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.artwork.HocuspocusUtils
import com.soundcloud.apipublic.service.pagination.OffsetBasedPagination
import com.soundcloud.apipublic.service.playlists.representation.PlaylistCreateOrUpdate
import com.soundcloud.apipublic.service.playlists.{PlaylistBuilder, PlaylistRequest, UpdatePlaylistArtworkRequest}
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.trackrepresentation.{
  TrackRepresentation,
  TrackRepresentationSpecContext,
  TrackRepresentationsService
}
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.hocuspocus.{HocuspocusService, Image, Kind}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.{UserSession, UserSessionBuilder}
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.finagle.http.ParamMap
import com.twitter.io.{BufReader, Reader}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import proto.soundcloud.common.session.{UserSession => ProtoUserSession}
import proto.soundcloud.playlists.api.{
  Counts => ProtoCounts,
  CreatePlaylistRequest,
  CreatePlaylistResponse,
  GetVisiblePlaylistsRequest,
  GetVisiblePlaylistsResponse,
  PlaylistPagination,
  PlaylistResponse,
  UpdatePlaylistRequest,
  UpdatePlaylistResponse,
  Playlist => ProtoPlaylist,
  PlaylistRequest => ProtoPlaylistRequest,
  PlaylistsService => PlaylistsTwirpService,
  TrackRequest => ProtoTrackRequest,
  WritesService => PlaylistsWritesTwirpService
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

    val requestedPlaylistTrack = createTrackRepresentationFromVisibleTrack()
    val requestedPlaylistTrack1 = createTrackRepresentationFromVisibleTrack()
    val requestedPlaylistTrack2 = createTrackRepresentationFromVisibleTrack()

    val candidateSecretToken = Some("s3creT")
    val pagination = PlaylistPagination(cursor = Some("2"), limit = 2)

    val session: UserSession = new UserSessionBuilder().setUser(requestingUserUrn).build()
    val protoSession: ProtoUserSession = session.asProtoSession

    val playlistsTwirpServiceMock = mock[PlaylistsTwirpService]
    val trackServiceMock = mock[TrackRepresentationsService]
    val moshimoshiClientMock = mock[MoshimoshiClient]
    val playlistsWritesTwirpServiceMock = mock[PlaylistsWritesTwirpService]
    val telemetry = Telemetry.defaultInstance
    val exceptionCollector = mock[ExceptionCollector]

    val protoPlaylist =
      ProtoPlaylist(
        urn = requestedPlaylistUrn.toString,
        userUrn = playlistOwner.urn.toString,
        labelId = Some(labelUrn.identifier),
        counts = Some(ProtoCounts(Some(5L)))
      )

    val protoTrack = Seq(ProtoTrackRequest(urn = requestedPlaylistTrackUrn.toString))
    val hocusPocusService = mock[HocuspocusService]
    val playlistsService =
      new PlaylistsService(
        playlistsTwirpServiceMock,
        trackServiceMock,
        moshimoshiClientMock,
        playlistsWritesTwirpServiceMock,
        exceptionCollector,
        hocusPocusService
      )
    val playlistRequests = List(PlaylistRequest(requestedPlaylistUrn, candidateSecretToken))
    val access = AccessParams.defaultAccess

    def setUpMocksForReadPlaylists(
        pagination: Option[PlaylistPagination] = None,
        visiblePlaylistsRequest: GetVisiblePlaylistsRequest,
        trackRequests: List[TrackRequest] = List.empty,
        requestedPlaylistTracks: List[TrackRepresentation] = List(requestedPlaylistTrack),
        protoTrackRequests: Seq[ProtoTrackRequest] = protoTrack,
        playlistAccess: AccessParams = AccessParams.defaultAccess,
        shouldFailGetVisiblePlaylist: Boolean = false
    ) = {
      val expectedPlaylistResponse = GetVisiblePlaylistsResponse(playlistResponse = Seq(
        PlaylistResponse(
          playlist = Some(protoPlaylist),
          trackRequests = protoTrackRequests,
          pagination = pagination
        )
      )
      )

      val visiblePlaylistResponse = if (shouldFailGetVisiblePlaylist) {
        GetVisiblePlaylistsResponse(Seq.empty)
      } else {
        expectedPlaylistResponse
      }

      when(playlistsTwirpServiceMock.getVisiblePlaylists(getVisiblePlaylistsRequest = visiblePlaylistsRequest))
        .thenReturn(Future.value(visiblePlaylistResponse))
      when(
        trackServiceMock.tracks(session, trackRequests, playlistAccess)
      ).thenReturn(Future.value(requestedPlaylistTracks))
    }

    def setUpMocksForWritePlaylists(
        playlistCreate: PlaylistCreateOrUpdate,
        response: Future[CreatePlaylistResponse]
    ) = {
      when(
        playlistsWritesTwirpServiceMock
          .createPlaylist(
            CreatePlaylistRequest(playlist = Some(playlistCreate.toProto), userSession = Some(session.asProtoSession))
          )
      ).thenReturn(response)
    }

    def setUpMocksForUpdatePlaylists(
        playlistCreate: PlaylistCreateOrUpdate,
        playlistUrn: Urn,
        response: Future[UpdatePlaylistResponse]
    ) = {
      when(
        playlistsWritesTwirpServiceMock.updatePlaylist(
          UpdatePlaylistRequest(
            playlist = Some(playlistCreate.toProto),
            urn = playlistUrn.toString,
            userSession = Some(session.asProtoSession)
          )
        )
      ).thenReturn(response)
    }
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
      GetVisiblePlaylistsRequest(
        playlistRequests = Seq(requestWithPagination),
        userSession = Some(protoSession),
        fieldMask = Some(PlaylistsService.playlistWithCountsFieldMask)
      )
    val getVisiblePlaylist =
      GetVisiblePlaylistsRequest(
        playlistRequests = Seq(requestWithoutPagination),
        userSession = Some(protoSession),
        fieldMask = Some(PlaylistsService.playlistWithCountsFieldMask)
      )

    val protoTracks = Seq(
      ProtoTrackRequest(urn = requestedPlaylistTrackUrn.toString),
      ProtoTrackRequest(urn = requestedPlaylistTrackUrn1.toString),
      ProtoTrackRequest(urn = requestedPlaylistTrackUrn2.toString)
    )

    when(moshimoshiClientMock.fetchUserObjects(session, Set(playlistOwner.urn, labelUrn)))
      .thenReturn(Future.value(List(playlistOwner, defaultLabel)))
  }

  trait NoLabelIdContext extends SuccessContext {
    override val protoPlaylist =
      ProtoPlaylist(
        urn = requestedPlaylistUrn.toString,
        userUrn = playlistOwner.urn.toString,
        labelId = None,
        counts = Some(ProtoCounts(Some(5L)))
      )

    when(moshimoshiClientMock.fetchUserObjects(session, Set(playlistOwner.urn)))
      .thenReturn(Future.value(List(playlistOwner)))

  }

  trait NotFoundContext extends Context {
    val request = ProtoPlaylistRequest(
      urn = requestedPlaylistUrn.toString,
      secretToken = candidateSecretToken
    )

    lazy val getVisiblePlaylist =
      GetVisiblePlaylistsRequest(
        playlistRequests = Seq(request),
        userSession = Some(protoSession),
        fieldMask = Some(PlaylistsService.playlistWithCountsFieldMask)
      )

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
      setUpMocksForReadPlaylists(
        pagination = Some(PlaylistPagination(cursor = Some("4"), limit = 2)),
        trackRequests = List(TrackRequest(urn = requestedPlaylistTrackUrn, secretToken = None)),
        visiblePlaylistsRequest = getVisiblePlaylistWithPagination
      )

      val result =
        Await.result(
          playlistsService
            .fetchPlaylist(
              session,
              requestedPlaylistUrn,
              candidateSecretToken,
              access,
              Some(offsetBasedPagination),
              Some(true)
            )
        )

      result match {
        case Good(playlist) =>
          playlist.id ==== requestedPlaylistUrn.identifier.toLong
          playlist.userId ==== playlistOwner.urn.identifier.toLong
          playlist.tracks.get.length ==== 1
          playlist.tracks.get.head.urn ==== requestedPlaylistTrack.urn
          playlist.likesCount ==== 5
        case _ => failure(s"returned ${result.toString} instead of Good(_)")
      }
    }

    "can fetch a playlist w/o tracks given playlist, pagination and showTracks=false" in new SuccessContext {
      setUpMocksForReadPlaylists(
        pagination = Some(PlaylistPagination(cursor = Some("4"), limit = 2)),
        trackRequests = List(TrackRequest(urn = requestedPlaylistTrackUrn, secretToken = None)),
        visiblePlaylistsRequest = getVisiblePlaylistWithPagination
      )

      val result =
        Await.result(
          playlistsService
            .fetchPlaylist(
              session,
              requestedPlaylistUrn,
              candidateSecretToken,
              access,
              Some(offsetBasedPagination),
              Some(false)
            )
        )

      result match {
        case Good(playlist) =>
          playlist.id ==== requestedPlaylistUrn.identifier.toLong
          playlist.userId ==== playlistOwner.urn.identifier.toLong
          playlist.tracks ==== None
          playlist.likesCount ==== 5
        case _ => failure(s"returned ${result.toString} instead of Good(_)")
      }
    }

    "can fetch a playlist with labelId not present" in new NoLabelIdContext {
      setUpMocksForReadPlaylists(
        pagination = Some(PlaylistPagination(cursor = Some("4"), limit = 2)),
        trackRequests = List(TrackRequest(urn = requestedPlaylistTrackUrn, secretToken = None)),
        visiblePlaylistsRequest = getVisiblePlaylistWithPagination
      )

      val result =
        Await.result(
          playlistsService
            .fetchPlaylist(
              session,
              requestedPlaylistUrn,
              candidateSecretToken,
              access,
              Some(offsetBasedPagination),
              Some(true)
            )
        )

      result match {
        case Good(playlist) =>
          playlist.id ==== requestedPlaylistUrn.identifier.toLong
          playlist.labelId ==== None
        case _ => failure(s"returned ${result.toString} instead of Good(_)")
      }
    }

    "can fetch a playlist without pagination" in new SuccessContext {
      setUpMocksForReadPlaylists(
        pagination = None,
        visiblePlaylistsRequest = getVisiblePlaylist,
        trackRequests = List(TrackRequest(urn = requestedPlaylistTrackUrn, secretToken = None))
      )

      val result =
        Await.result(
          playlistsService
            .fetchPlaylist(session, requestedPlaylistUrn, candidateSecretToken, access, None, Some(true))
        )

      result match {
        case Good(playlist) =>
          playlist.id ==== requestedPlaylistUrn.identifier.toLong
          playlist.userId ==== playlistOwner.urn.identifier.toLong
          playlist.tracks.get.length ==== 1
          playlist.tracks.get.head.urn ==== requestedPlaylistTrack.urn
          playlist.likesCount ==== 5
        case _ => failure(s"returned ${result.toString} instead of Good(_)")
      }
    }

    "returns NotFound if no playlist returned from client" in new NotFoundContext {
      val result =
        Await.result(
          playlistsService
            .fetchPlaylist(session, requestedPlaylistUrn, candidateSecretToken, access, None, Some(true))
        )

      result ==== Bad(NotFound("playlist not found"))
    }
  }

  "#fetchPlaylistTracks" >> {
    "can retrieve playlist tracks without pagination" in new SuccessContext {
      override val getVisiblePlaylist =
        GetVisiblePlaylistsRequest(playlistRequests = Seq(requestWithoutPagination), userSession = Some(protoSession))

      setUpMocksForReadPlaylists(
        pagination = None,
        visiblePlaylistsRequest = getVisiblePlaylist,
        requestedPlaylistTracks = List(requestedPlaylistTrack, requestedPlaylistTrack1, requestedPlaylistTrack2),
        trackRequests = List(TrackRequest(urn = requestedPlaylistTrackUrn, secretToken = None))
      )

      val result =
        Await.result(
          playlistsService
            .fetchPlaylistTracks(session, requestedPlaylistUrn, candidateSecretToken, access, None)
        )

      result match {
        case Good(tracksCollection: Collection[TrackRepresentation]) =>
          tracksCollection.items.length ==== 3
          tracksCollection.items.head.urn ==== requestedPlaylistTrack.urn
          tracksCollection.items(1).urn ==== requestedPlaylistTrack1.urn
          tracksCollection.items(2).urn ==== requestedPlaylistTrack2.urn
          tracksCollection.nextHref.isEmpty
        case _ => failure(s"incorrectly returned ${result.toString}")
      }
    }

    "can retrieve playlist tracks with pagination" in new SuccessContext {
      override val getVisiblePlaylistWithPagination =
        GetVisiblePlaylistsRequest(playlistRequests = Seq(requestWithPagination), userSession = Some(protoSession))

      override val protoTrack: Seq[ProtoTrackRequest] = Seq(protoTracks.head)
      setUpMocksForReadPlaylists(
        pagination = Some(PlaylistPagination(cursor = Some("2"), limit = 2)),
        visiblePlaylistsRequest = getVisiblePlaylistWithPagination,
        trackRequests = List(TrackRequest(urn = requestedPlaylistTrackUrn, secretToken = None))
      )

      val result =
        Await.result(
          playlistsService
            .fetchPlaylistTracks(
              session,
              requestedPlaylistUrn,
              candidateSecretToken,
              AccessParams.defaultAccess,
              Some(offsetBasedPagination)
            )
        )

      result match {
        case Good(tracksCollection: Collection[TrackRepresentation]) =>
          tracksCollection.items.length ==== 1
          tracksCollection.items(0).urn ==== requestedPlaylistTrack.urn
          tracksCollection.nextHref ==== Some("https://api.soundcloud.com/playlists?limit=2&offset=2")
        case _ => failure(s"incorrectly returned ${result.toString}")
      }
    }

    "returns NotFound if no playlist is returned from the client" in new NotFoundContext {
      override lazy val getVisiblePlaylist =
        GetVisiblePlaylistsRequest(playlistRequests = Seq(request), userSession = Some(protoSession))

      val result =
        Await.result(
          playlistsService
            .fetchPlaylistTracks(session, requestedPlaylistUrn, candidateSecretToken, AccessParams.defaultAccess, None)
        )

      result ==== Bad(NotFound("playlist not found"))
    }
  }

  "#fetchPlaylists" >> {
    "can fetch a list of playlists given playlist, secret token, and pagination" in new SuccessContext {
      setUpMocksForReadPlaylists(
        pagination = Some(PlaylistPagination(cursor = Some("4"), limit = 2)),
        visiblePlaylistsRequest = getVisiblePlaylistWithPagination,
        trackRequests = List(TrackRequest(urn = requestedPlaylistTrackUrn, secretToken = None))
      )

      val result =
        Await.result(
          playlistsService
            .fetchPlaylists(session, playlistRequests, access, Some(offsetBasedPagination), None)
        )

      result.length ==== 1
      result.head.id ==== requestedPlaylistUrn.identifier.toLong
      result.head.userId ==== playlistOwner.urn.identifier.toLong
      result.head.tracks.get.length ==== 1
      result.head.tracks.get.head.urn ==== requestedPlaylistTrack.urn
      result.head.likesCount ==== 5
    }

    "can fetch a list of playlists w/o tracks given playlist, pagination and showTracks=false" in new SuccessContext {
      setUpMocksForReadPlaylists(
        pagination = Some(PlaylistPagination(cursor = Some("4"), limit = 2)),
        visiblePlaylistsRequest = getVisiblePlaylistWithPagination,
        trackRequests = List(TrackRequest(urn = requestedPlaylistTrackUrn, secretToken = None))
      )

      val result =
        Await.result(
          playlistsService
            .fetchPlaylists(session, playlistRequests, access, Some(offsetBasedPagination), Some(false))
        )

      result.length ==== 1
      result.head.id ==== requestedPlaylistUrn.identifier.toLong
      result.head.userId ==== playlistOwner.urn.identifier.toLong
      result.head.tracks ==== None
      result.head.likesCount ==== 5
    }

    "can fetch a list of playlists without pagination" in new SuccessContext {
      setUpMocksForReadPlaylists(
        None,
        getVisiblePlaylist,
        trackRequests = List(TrackRequest(urn = requestedPlaylistTrackUrn, secretToken = None))
      )

      val result =
        Await.result(
          playlistsService
            .fetchPlaylists(session, playlistRequests, access, None, None)
        )

      result.length ==== 1
      result.head.id ==== requestedPlaylistUrn.identifier.toLong
      result.head.userId ==== playlistOwner.urn.identifier.toLong
      result.head.tracks.get.length ==== 1
      result.head.tracks.get.head.urn ==== requestedPlaylistTrack.urn
      result.head.likesCount ==== 5
    }

    "returns empty list if no playlist returned from client" in new NotFoundContext {
      val result =
        Await.result(
          playlistsService
            .fetchPlaylists(session, playlistRequests, access, None, None)
        )

      result ==== List.empty
    }
  }

  "#fetchPlaylistMetadataOnly" >> {
    "returns playlists without tracks field" in new SuccessContext {
      val emptyPagination = PlaylistPagination()
      val request = requestWithPagination.copy(pagination = Some(emptyPagination))
      val getPlaylistRequest =
        GetVisiblePlaylistsRequest(
          playlistRequests = Seq(request),
          userSession = Some(protoSession),
          fieldMask = Some(PlaylistsService.playlistWithCountsFieldMask)
        )

      setUpMocksForReadPlaylists(
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
      result.head.tracks must beEmpty
      result.head.likesCount ==== 5
    }

    "returns an empty list if no playlists found" in new SuccessContext {
      val emptyPagination = PlaylistPagination()
      val request = requestWithPagination.copy(pagination = Some(emptyPagination))
      val getPlaylistRequest =
        GetVisiblePlaylistsRequest(
          playlistRequests = Seq(request),
          userSession = Some(protoSession),
          fieldMask = Some(PlaylistsService.playlistWithCountsFieldMask)
        )

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

  trait CreateOrUpdatePlaylist extends SuccessContext {
    val playlistUrn = Urn("soundcloud", "playlists", "1")
    val createOrUpdatePlaylist = PlaylistCreateOrUpdate(
      public = Value(true),
      title = Value("title"),
      tracks = Value(Seq(Map("id" -> "1")))
    )

    val playlistRequest =
      ProtoPlaylistRequest(urn = Urn("soundcloud", "playlists", "1").toString, secretToken = None, pagination = None)

    val visiblePlaylistRequests =
      GetVisiblePlaylistsRequest(
        playlistRequests = Seq(playlistRequest),
        userSession = Some(protoSession),
        fieldMask = Some(PlaylistsService.playlistWithCountsFieldMask)
      )

    def shouldFailGetVisiblePlaylist = false

    val nullPlaylist = PlaylistCreateOrUpdate()

    setUpMocksForReadPlaylists(
      trackRequests = List(TrackRequest(urn = requestedPlaylistTrackUrn, secretToken = None)),
      visiblePlaylistsRequest = visiblePlaylistRequests,
      playlistAccess = AccessParams.explicitAccess,
      shouldFailGetVisiblePlaylist = shouldFailGetVisiblePlaylist
    )
  }

  "#createPlaylist" >> {

    "creates the playlist" in new CreateOrUpdatePlaylist {
      val playlist = new PlaylistBuilder().setId(1).build

      setUpMocksForWritePlaylists(
        playlistCreate = createOrUpdatePlaylist,
        response =
          Future.value(CreatePlaylistResponse(urn = Urn("soundcloud", "playlists", playlist.id.toString).toString))
      )

      val result = Await.result(playlistsService.createPlaylist(session, createOrUpdatePlaylist, None).value)

      result match {
        case Good(playlist) =>
          playlist.id ==== requestedPlaylistUrn.identifier.toLong
          playlist.userId ==== playlistOwner.urn.identifier.toLong
          playlist.tracks.get.length ==== 1
          playlist.tracks.get.head.urn ==== requestedPlaylistTrack.urn
          playlist.likesCount ==== 5
        case _ => failure(s"returned ${result.toString} instead of Good(_)")
      }
    }

    trait ArtworkContext extends CreateOrUpdatePlaylist {
      val testImage = "test-image.jpg"

      val buf = Await.result(
        BufReader.readAll(Reader.fromStream(this.getClass.getClassLoader.getResourceAsStream(testImage)))
      )
      val artworkUpdateRequest = UpdatePlaylistArtworkRequest(buf)
      val expectedRaw = HocuspocusUtils.toRaw(buf, Some(session))

      val expectedArtworkUpdate = PlaylistArtworkUpdate("bucket", "filename")

      hocusPocusService.storeImage(expectedRaw) returns Future.value(
        Image(kind = Kind.ARTWORKS, originUri = "s3://bucket/filename")
      )

      val playlist = new PlaylistBuilder().setId(1).build

      setUpMocksForWritePlaylists(
        playlistCreate = createOrUpdatePlaylist,
        response =
          Future.value(CreatePlaylistResponse(urn = Urn("soundcloud", "playlists", playlist.id.toString).toString))
      )

      when(
        playlistsWritesTwirpServiceMock.updatePlaylistArtwork(
          proto.soundcloud.playlists.api.UpdatePlaylistArtworkRequest(
            Some(session.asProtoSession),
            urn = playlistUrn.toString,
            expectedArtworkUpdate.bucket,
            expectedArtworkUpdate.filename
          )
        )
      ).thenReturn(
        Future(proto.soundcloud.playlists.api.UpdatePlaylistArtworkResponse(_root_.scalapb.UnknownFieldSet.empty))
      )

      val _ =
        Await.result(playlistsService.createPlaylist(session, createOrUpdatePlaylist, Some(artworkUpdateRequest)).value)
    }

    "updates playlist artwork metadata via VAS service when rollout flag is enabled" in new ArtworkContext {
      verify(playlistsWritesTwirpServiceMock).updatePlaylistArtwork(
        proto.soundcloud.playlists.api.UpdatePlaylistArtworkRequest(
          Some(session.asProtoSession),
          urn = playlistUrn.toString,
          expectedArtworkUpdate.bucket,
          expectedArtworkUpdate.filename
        )
      )
    }

    "returns Not Valid if Playlists returned Invalid Argument" in new CreateOrUpdatePlaylist {
      setUpMocksForWritePlaylists(
        playlistCreate = createOrUpdatePlaylist,
        response = Future.exception(TwinagleException(ErrorCode.InvalidArgument, "invalid argument", null, null))
      )

      val result = Await.result(playlistsService.createPlaylist(session, createOrUpdatePlaylist, None).value)

      result ==== Bad(NotValid("invalid argument"))
    }

    "returns NotValid if all fields missing from params" in new CreateOrUpdatePlaylist {
      val result = Await.result(playlistsService.createPlaylist(session, nullPlaylist, None).value)

      result ==== Bad(NotValid("All fields missing"))
    }

    "does not call the twirp service if all fields missing from params" in new CreateOrUpdatePlaylist {
      val _ = Await.result(playlistsService.createPlaylist(session, nullPlaylist, None).value)
      verifyNoInteractions(playlistsWritesTwirpServiceMock)
    }

    "returns Not Authorised if Playlists returned Permission Denied" in new CreateOrUpdatePlaylist {
      setUpMocksForWritePlaylists(
        playlistCreate = createOrUpdatePlaylist,
        response = Future.exception(TwinagleException(ErrorCode.PermissionDenied, "permission denied", null, null))
      )

      val result = Await.result(playlistsService.createPlaylist(session, createOrUpdatePlaylist, None).value)

      result ==== Bad(NotAuthorized("permission denied"))
    }

    "throws error if Playlists returns unexpected response" in new CreateOrUpdatePlaylist {
      val playlist = new PlaylistBuilder().setId(1).build

      when(
        playlistsWritesTwirpServiceMock
          .createPlaylist(
            CreatePlaylistRequest(
              playlist = Some(createOrUpdatePlaylist.toProto),
              userSession = Some(session.asProtoSession)
            )
          )
      ).thenReturn(Future.exception(TwinagleException(ErrorCode.Internal, "oops", null, null)))

      Await.result(playlistsService.createPlaylist(session, createOrUpdatePlaylist, None).value) must throwA[
        RuntimeException
      ]
    }

    "logs exception if read after write fails" in new CreateOrUpdatePlaylist {
      override def shouldFailGetVisiblePlaylist = true

      val playlist = new PlaylistBuilder().setId(1).build

      setUpMocksForWritePlaylists(
        playlistCreate = createOrUpdatePlaylist,
        response =
          Future.value(CreatePlaylistResponse(urn = Urn("soundcloud", "playlists", playlist.id.toString).toString))
      )

      val result = Await.result(playlistsService.createPlaylist(session, createOrUpdatePlaylist, None).value)

      result match {
        case Good(_) =>
          failure(s"returned ${result.toString} instead of Bad(NotFound())")
        case Bad(bad) => bad ==== NotFound("playlist not found")
      }

      verify(exceptionCollector).addMessage(
        "read-after-write-playlists",
        "reading after creating a playlist failed",
        collectRequestBody = true
      )
    }
  }

  "#updatePlaylist" >> {

    "updates the playlist" in new CreateOrUpdatePlaylist {
      setUpMocksForUpdatePlaylists(createOrUpdatePlaylist, playlistUrn, Future.value(UpdatePlaylistResponse()))

      val result =
        Await.result(playlistsService.updatePlaylist(session, playlistUrn, createOrUpdatePlaylist, None).value)

      result match {
        case Good(playlist) =>
          playlist.id ==== requestedPlaylistUrn.identifier.toLong
          playlist.userId ==== playlistOwner.urn.identifier.toLong
          playlist.tracks.get.length ==== 1
          playlist.tracks.get.head.urn ==== requestedPlaylistTrack.urn
          playlist.likesCount ==== 5
        case _ => failure(s"returned ${result.toString} instead of Good(_)")
      }
    }

    "returns Not Found if Playlists returned Not Found" in new CreateOrUpdatePlaylist {
      setUpMocksForUpdatePlaylists(
        createOrUpdatePlaylist,
        playlistUrn,
        response = Future.exception(TwinagleException(ErrorCode.NotFound, "not found", null, null))
      )

      val result =
        Await.result(playlistsService.updatePlaylist(session, playlistUrn, createOrUpdatePlaylist, None).value)

      result ==== Bad(NotFound("playlist not found"))
    }

    "returns Not Valid if Playlists returned Invalid Argument" in new CreateOrUpdatePlaylist {
      setUpMocksForUpdatePlaylists(
        createOrUpdatePlaylist,
        playlistUrn,
        response = Future.exception(TwinagleException(ErrorCode.InvalidArgument, "invalid argument", null, null))
      )

      val result =
        Await.result(playlistsService.updatePlaylist(session, playlistUrn, createOrUpdatePlaylist, None).value)

      result ==== Bad(NotValid("invalid argument"))
    }

    "returns Not Authorised if Playlists returned Permission Denied" in new CreateOrUpdatePlaylist {
      setUpMocksForUpdatePlaylists(
        createOrUpdatePlaylist,
        playlistUrn,
        response = Future.exception(TwinagleException(ErrorCode.PermissionDenied, "permission denied", null, null))
      )

      val result =
        Await.result(playlistsService.updatePlaylist(session, playlistUrn, createOrUpdatePlaylist, None).value)

      result ==== Bad(NotAuthorized("permission denied"))
    }

    "throws error if Playlists returns unexpected response" in new CreateOrUpdatePlaylist {
      setUpMocksForUpdatePlaylists(
        createOrUpdatePlaylist,
        playlistUrn,
        response = Future.exception(TwinagleException(ErrorCode.Internal, "oops", null, null))
      )

      Await.result(playlistsService.updatePlaylist(session, playlistUrn, createOrUpdatePlaylist, None).value) must throwA[
        RuntimeException
      ]
    }

    "does not call the twirp service if all fields missing from params" in new CreateOrUpdatePlaylist {
      val _ = Await.result(playlistsService.updatePlaylist(session, playlistUrn, nullPlaylist, None).value)
      verifyNoInteractions(playlistsWritesTwirpServiceMock)
    }
  }

}
