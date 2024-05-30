package com.soundcloud.apipublic.service

import com.google.protobuf.field_mask.FieldMask
import com.soundcloud.apipublic.client.mothership.MoshimoshiClient
import com.soundcloud.apipublic.client.mothership.response.representation.UserRepresentation
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.artwork.HocuspocusUtils
import com.soundcloud.apipublic.service.pagination.OffsetBasedPagination
import com.soundcloud.apipublic.service.playlists.representation.{Playlist, PlaylistCreateOrUpdate, VisiblePlaylist}
import com.soundcloud.apipublic.service.playlists.{PlaylistProtoMapper, PlaylistRequest, UpdatePlaylistArtworkRequest}
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.trackrepresentation.{TrackRepresentation, TrackRepresentationsService}
import com.soundcloud.hocuspocus.{HocuspocusService, Image}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionHandler.FutureExtensions
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.io.Buf
import com.twitter.util.Future
import proto.soundcloud.playlists.api.{
  CreatePlaylistRequest,
  GetVisiblePlaylistsRequest,
  Playlist => ProtoPlaylist,
  PlaylistPagination,
  PlaylistResponse,
  UpdatePlaylistRequest,
  UpdatePlaylistResponse,
  PlaylistRequest => ProtoPlaylistRequest,
  PlaylistsService => PlaylistsTwirpService,
  UpdatePlaylistArtworkRequest => UpdatePlaylistArtworkTwirpRequest,
  WritesService => PlaylistsWritesTwirpService
}
import scalapb.FieldMaskUtil

class PlaylistsService(
    playlistsTwirpService: PlaylistsTwirpService,
    tracksService: TrackRepresentationsService,
    moshimoshiClient: MoshimoshiClient,
    playlistsWritesTwirpService: PlaylistsWritesTwirpService,
    exceptionCollector: ExceptionCollector,
    hocuspocusService: HocuspocusService,
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
      visiblePlaylistObjects <- getPlaylistObjects(
        session,
        playlistRequests,
        playlistPagination,
        Some(PlaylistsService.playlistWithCountsFieldMask)
      )
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
      visiblePlaylistObjects <- getPlaylistObjects(
        session,
        playlistRequests,
        Some(playlistPagination),
        Some(PlaylistsService.playlistWithCountsFieldMask)
      )
      visiblePlaylists = visiblePlaylistObjects.flatMap(response => playlistProtoMapper.apply(response, None))
      playlists <- resolvePlaylists(visiblePlaylists, session, AccessParams.defaultAccess, showTracks = false)
    } yield playlists
  }

  def createPlaylist(
      session: UserSession,
      playlistCreate: PlaylistCreateOrUpdate,
      artworkUpdateRequest: Option[UpdatePlaylistArtworkRequest]
  ): OutcomeF[Playlist] = {
    val playlist = for {
      createdUrn <- createPlaylistMetadata(playlistCreate, session)
      _ <- artworkUpdateRequest.map(updatePlaylistArtwork(session, createdUrn, _)).getOrElse(().goodF)
      playlist <- fetchPlaylist(session, createdUrn, None, AccessParams.explicitAccess, None, None).outcomeF
    } yield playlist
    playlist.leftMap {
      case notFound: NotFound =>
        exceptionCollector.addMessage(
          "read-after-write-playlists",
          "reading after creating a playlist failed",
          collectRequestBody = true
        )
        notFound
      case other => other
    }
  }

  private def createPlaylistMetadata(
      playlistCreateOrUpdate: PlaylistCreateOrUpdate,
      session: UserSession
  ): OutcomeF[Urn] = {
    if (playlistCreateOrUpdate.allFieldsMissing) {
      NotValid("All fields missing").badF
    } else {
      playlistsWritesTwirpService
        .createPlaylist(
          CreatePlaylistRequest(
            playlist = Some(playlistCreateOrUpdate.toProto),
            userSession = Some(session.asProtoSession)
          )
        )
        .map(res => Urn.parse(res.urn).get.good)
        .handle {
          case TwinagleException(ErrorCode.InvalidArgument, msg, _, _) => Bad(NotValid(msg))
          case TwinagleException(ErrorCode.PermissionDenied, msg, _, _) => Bad(NotAuthorized(msg))
          case TwinagleException(_, msg, _, _) =>
            throw new RuntimeException(s"unexpected response from playlists: ${msg}")
        }
        .outcomeF
    }
  }

  private def updatePlaylistArtwork(
      session: UserSession,
      playlistUrn: Urn,
      playlistArtworkRequest: UpdatePlaylistArtworkRequest
  ): OutcomeF[Unit] =
    for {
      _ <- updateArtwork(session, playlistUrn, playlistArtworkRequest)
      _ <- Future.Unit.outcomeF
    } yield ()

  private def updateArtwork(
      session: UserSession,
      playlistUrn: Urn,
      playlistArtworkRequest: UpdatePlaylistArtworkRequest
  ): OutcomeF[Unit] = {
    for {
      createdImage <- uploadImageToHocuspocus(session, playlistArtworkRequest.imageData)
      updateParams <- extractPlaylistArtworkUpdateParams(createdImage).liftF
      _ <- playlistsWritesTwirpService
        .updatePlaylistArtwork(
          updateParams
            .copy(Some(session.asProtoSession), playlistUrn.toString, updateParams.bucket, updateParams.filename)
        )
        .outcomeF
    } yield ()
  }

  private def uploadImageToHocuspocus(session: UserSession, imageData: Buf): OutcomeF[Image] = {
    val handleErrors: PartialFunction[Throwable, Outcome[Image]] = {
      case TwinagleException(ErrorCode.InvalidArgument, _, _, _) => NotValid("Invalid Image").bad
    }

    hocuspocusService
      .storeImage(HocuspocusUtils.toRaw(imageData, Some(session)))
      .map(_.good)
      .handleAndReport(exceptionCollector, true)(handleErrors)
      .outcomeF
      .catchToUnexpectedError
  }

  private def extractPlaylistArtworkUpdateParams(createdImage: Image): Outcome[UpdatePlaylistArtworkTwirpRequest] = {
    val s3UrlRegex = HocuspocusUtils.s3UrlRegex
    createdImage.originUri match {
      case s3UrlRegex(bucket, filename) => UpdatePlaylistArtworkTwirpRequest(bucket = bucket, filename = filename).good
      case _ => NotValid("Invalid image location").bad
    }
  }

  def updatePlaylist(
      session: UserSession,
      playlistUrn: Urn,
      playlistCreate: PlaylistCreateOrUpdate,
      artworkUpdateRequest: Option[UpdatePlaylistArtworkRequest]
  ): OutcomeF[Playlist] = {
    for {
      _ <- updatePlaylistMetadata(playlistCreate, playlistUrn, session)
      _ <- artworkUpdateRequest.map(updatePlaylistArtwork(session, playlistUrn, _)).getOrElse(().goodF)
      playlist <- fetchPlaylist(session, playlistUrn, None, AccessParams.explicitAccess, None, None).outcomeF
    } yield playlist
  }

  private def updatePlaylistMetadata(
      playlistCreateOrUpdate: PlaylistCreateOrUpdate,
      playlistUrn: Urn,
      session: UserSession
  ): OutcomeF[UpdatePlaylistResponse] = {
    val updatePlaylistRequest = UpdatePlaylistRequest(
      Some(playlistCreateOrUpdate.toProto),
      playlistUrn.toString,
      Some(session.asProtoSession)
    )
    if (playlistCreateOrUpdate.allFieldsMissing) {
      UpdatePlaylistResponse().goodF
    } else {
      playlistsWritesTwirpService
        .updatePlaylist(updatePlaylistRequest)
        .map(Good(_))
        .handle {
          case TwinagleException(ErrorCode.NotFound, _, _, _) => Bad(NotFound("playlist not found"))
          case TwinagleException(ErrorCode.InvalidArgument, msg, _, _) => Bad(NotValid(msg))
          case TwinagleException(ErrorCode.PermissionDenied, msg, _, _) => Bad(NotAuthorized(msg))
          case TwinagleException(code, msg, meta, _) =>
            throw new RuntimeException(s"unexpected response from playlists: msg: ${msg}, code: ${code}, meta: ${meta}")
        }
        .outcomeF
    }
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

      moshimoshiClient
        .fetchUserObjects(session, userUrns.toSet)
        .map(users => createPlaylistWithUserData(urns, visiblePlaylists, users, session))
    }
  }

  private def createPlaylistWithUserData(
      urns: List[String],
      visiblePlaylists: List[VisiblePlaylist],
      users: List[UserRepresentation],
      session: UserSession
  ): List[Playlist] = {
    for {
      urn <- urns
      playlist <- visiblePlaylists.find(_.urn == urn)
      owner <- users.find(_.urn.toString == playlist.userUrn)
      maybeLabelOwner = playlist.labelId.flatMap(id => users.find(_.urn.identifier == id))
    } yield Playlist.fromVisiblePlaylist(playlist, owner, maybeLabelOwner, session.user)
  }

  private def getPlaylistObjects(
      session: UserSession,
      playlistRequests: List[PlaylistRequest],
      pagination: Option[PlaylistPagination],
      fieldMask: Option[FieldMask] = None
  ): Future[List[PlaylistResponse]] = {
    val protoPlaylistRequests = playlistRequests.map(request =>
      ProtoPlaylistRequest(
        urn = request.urn.toString,
        secretToken = request.secretToken,
        pagination = pagination
      )
    )

    val getVisiblePlaylistsRequest = createGetVisiblePlaylistRequest(session, protoPlaylistRequests, fieldMask)

    playlistsTwirpService
      .getVisiblePlaylists(getVisiblePlaylistsRequest)
      .map(response => {
        response.playlistResponse.toList
      })
  }

  private def createGetVisiblePlaylistRequest(
      session: UserSession,
      playlistRequests: Seq[ProtoPlaylistRequest],
      fieldMask: Option[FieldMask]
  ): GetVisiblePlaylistsRequest = {
    val protoUserSession = session.asProtoSession

    val subscriptionCountryCode = session.getExtraHeader(UserSession.CONSUMER_SUBSCRIPTION_COUNTRY)

    GetVisiblePlaylistsRequest(
      playlistRequests = playlistRequests,
      userSession = Some(protoUserSession),
      subscriptionCountryCode = Option(subscriptionCountryCode),
      fieldMask = fieldMask
    )
  }
}

object PlaylistsService {

  val playlistWithCountsFieldMask: FieldMask = FieldMaskUtil.selectFieldNumbers[ProtoPlaylist](
    Set(ProtoPlaylist.COUNTS_FIELD_NUMBER)
  )
}
