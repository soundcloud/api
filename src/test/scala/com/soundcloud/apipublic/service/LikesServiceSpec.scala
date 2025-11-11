package com.soundcloud.apipublic.service

import com.google.protobuf.timestamp.Timestamp
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.pagination.CursorBasedPagination
import com.soundcloud.apipublic.service.playlists.representation.Playlist
import com.soundcloud.apipublic.service.playlists.{PlaylistBuilder, PlaylistRequest}
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.trackrepresentation.{
  TrackRepresentation,
  TrackRepresentationsService,
  TrackRepresentationsSpecificationContext
}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.rollout.{Rollout, RolloutFeature}
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.finagle.http.{ParamMap, Status}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import org.specs2.mutable.BeforeAfter
import proto.soundcloud.likes.api.{
  ChronoDirection,
  ChronoParams,
  GetLikesByUserChronoRequest,
  GetLikesByUserChronoResponse,
  GetLikesChronoResponseItem,
  LikesClientProtobuf,
  Collection => likesCollection
}
import proto.soundcloud.likes.api.v2.{
  ChronoParams => v2ChronoParams,
  ChronoResponse => v2ChronoResponse,
  Collection => v2likesCollection,
  GetLikesByUserChronoRequest => v2GetLikesByUserChronoRequest,
  LikesService => v2LikesClientProtobuf
}
import proto.soundcloud.playlists.api.{
  LikePlaylistRequest,
  LikePlaylistResponse,
  LikesClientProtobuf => PlaylistLikesClientProtobuf
}
import proto.soundcloud.tracks.api.{
  GetTrackLikersPagination,
  GetTrackLikersRequest,
  GetTrackLikersResponse,
  LikeTrackRequest,
  LikeTrackResponse,
  LikesClientProtobuf => TrackLikesClientProtobuf
}

import java.time.Instant

class LikesServiceSpec extends TrackRepresentationsSpecificationContext {
  private def useLikesV2Rollout = RolloutFeature("likes-v2")

  trait Context extends TrackRepresentationsContext with BeforeAfter {
    val trackRepresentationsService = mock[TrackRepresentationsService]
    val playlistsService = mock[PlaylistsService]
    val pagination = CursorBasedPagination(
      "https://api.soundcloud.com",
      "/users/1/favorites/",
      ParamMap(),
      Some("2"),
      1
    )

    val rollout = mock[Rollout]

    val tracksTwinagleClient = mock[TrackLikesClientProtobuf]
    val likesTwinagleClient = mock[LikesClientProtobuf]
    val v2LikesTwinagleClient = mock[v2LikesClientProtobuf]
    val playlistTwinagleClient = mock[PlaylistLikesClientProtobuf]

    val likesService = new LikesService(
      trackRepresentationsService,
      playlistsService,
      likesTwinagleClient,
      v2LikesTwinagleClient,
      tracksTwinagleClient,
      playlistTwinagleClient,
      rollout
    )

    override def before: Any = {}

    override def after: Any = {}
  }

  "#createTrackLike" >> {
    trait CreateTrackLike extends Context {
      val userUrn = Urn("soundcloud", "users", "1")
      val created = Instant.now
      val timestamp = Timestamp.of(created.getEpochSecond, created.getNano)
      val request = LikeTrackRequest(userSession = Some(session.asProtoSession), trackUrn = trackUrn.toString)
    }

    "returns an OkCreatedCreateResponse when Tracks successfully creates a like" in new CreateTrackLike {
      tracksTwinagleClient.likeTrack(request) returns Future.value(
        LikeTrackResponse(
          Some(timestamp),
          trackUrn.toString,
          userUrn.toString
        )
      )
      val result = Await.result(likesService.createTrackLike(session, trackUrn))
      result ==== CreateLikeResponse().good
    }

    "returns NotFoundCreateResponse when tracks responds with NotFound" in new CreateTrackLike {
      tracksTwinagleClient.likeTrack(request) returns Future.exception(
        TwinagleException(ErrorCode.NotFound, "Resource not found")
      )

      val result = Await.result(likesService.createTrackLike(session, trackUrn))
      result ==== NotFound().bad
    }

    "returns NotAuthorizedCreateResponse when tracks responds with PermissionDenied" in new CreateTrackLike {
      tracksTwinagleClient.likeTrack(request) returns Future.exception(
        TwinagleException(ErrorCode.PermissionDenied, "Operation not allowed")
      )

      val result = Await.result(likesService.createTrackLike(session, trackUrn))
      result ==== NotAllowed().bad
    }

    "returns SpamBlockedCreateResponse when tracks responds with ResourceExhausted" in new CreateTrackLike {
      tracksTwinagleClient.likeTrack(request) returns Future.exception(
        TwinagleException(ErrorCode.ResourceExhausted, "Spam alert")
      )

      val result = Await.result(likesService.createTrackLike(session, trackUrn))
      result ==== UnexpectedError(TwinagleException(ErrorCode.ResourceExhausted, "Spam alert")).bad
    }
  }

  "#deleteTrackLike" >> {
    trait DeleteTrackLike extends Context {
      val userUrn = Urn("soundcloud", "users", "1")
      val created = Instant.now
      val timestamp = Timestamp.of(created.getEpochSecond, created.getNano)
      val request = LikeTrackRequest(userSession = Some(session.asProtoSession), trackUrn = trackUrn.toString)
    }

    "returns LikeDeleted when tracks successfully deletes a like" in new DeleteTrackLike {
      tracksTwinagleClient.unlikeTrack(request) returns Future.value(
        LikeTrackResponse(
          Some(timestamp),
          trackUrn.toString,
          userUrn.toString
        )
      )
      val result = Await.result(likesService.deleteTrackLike(session, trackUrn))
      result ==== DeleteLikeResponse().good
    }

    "returns LikeNotFound when like was not found" in new DeleteTrackLike {
      tracksTwinagleClient.unlikeTrack(request) returns Future.exception(
        TwinagleException(ErrorCode.NotFound, "Resource not found")
      )

      val result = Await.result(likesService.deleteTrackLike(session, trackUrn))
      result ==== NotFound().bad
    }
  }

  "#trackLikers" >> {
    trait TrackLikers extends Context {
      val userUrn = Urn("soundcloud", "users", "1")
      val requestPagination = GetTrackLikersPagination(pagination.cursor, pagination.pageSize)
      val request = GetTrackLikersRequest(Some(session.asProtoSession), trackUrn.toString, Some(requestPagination))
    }

    "returns TrackLikersResponse when tracks successfully gets likers" in new TrackLikers {
      tracksTwinagleClient.getTrackLikers(request) returns Future.value(
        GetTrackLikersResponse(
          Seq(userUrn.toString)
        )
      )
      val result = Await.result(likesService.trackLikers(session, trackUrn, pagination))
      result ==== TrackLikersResponse(Seq(userUrn), None).good
    }

    "sets the next cursor when there is pagination" in new TrackLikers {
      tracksTwinagleClient.getTrackLikers(request) returns Future.value(
        GetTrackLikersResponse(
          Seq(userUrn.toString),
          pagination.cursor
        )
      )
      val result = Await.result(likesService.trackLikers(session, trackUrn, pagination))
      val nextHref = pagination.nextPage(pagination.cursor.get).normalizedHref
      result match {
        case Good(response) => response.nextHRef ==== Some(nextHref)
        case _ => ko
      }
    }

    "returns NotFound when tracks service returns not found" in new TrackLikers {
      tracksTwinagleClient.getTrackLikers(request) returns Future.exception(
        TwinagleException(ErrorCode.NotFound, "Resource not found")
      )

      val result = Await.result(likesService.trackLikers(session, trackUrn, pagination))
      result ==== NotFound().bad
    }
  }

  "#createPlaylistLike" >> {
    trait CreatePlaylistLike extends Context {
      val userUrn = Urn("soundcloud", "users", "1")
      val playlistUrn = Urn("soundcloud", "playlist", "1")
      val created = Instant.now
      val timestamp = Timestamp.of(created.getEpochSecond, created.getNano)
      val request = LikePlaylistRequest(userSession = Some(session.asProtoSession), urn = playlistUrn.toString)
    }

    "returns an OkCreatedCreateResponse when Playlists successfully creates a like" in new CreatePlaylistLike {
      playlistTwinagleClient.likePlaylist(request) returns Future.value(
        LikePlaylistResponse(
          Some(timestamp),
          playlistUrn.toString,
          userUrn.toString
        )
      )

      Await.result(likesService.createPlaylistLike(session, playlistUrn)) ==== CreateLikeResponse().good
    }

    "returns NotFoundCreateResponse when playlists responds with NotFound" in new CreatePlaylistLike {
      playlistTwinagleClient.likePlaylist(request) returns Future.exception(
        TwinagleException(ErrorCode.NotFound, "Resource not found")
      )

      Await.result(likesService.createPlaylistLike(session, playlistUrn)) ==== NotFound().bad
    }

    "returns NotAuthorizedCreateResponse when playlists responds with PermissionDenied" in new CreatePlaylistLike {
      playlistTwinagleClient.likePlaylist(request) returns Future.exception(
        TwinagleException(ErrorCode.PermissionDenied, "Operation not allowed")
      )

      Await.result(likesService.createPlaylistLike(session, playlistUrn)) ==== NotAllowed().bad
    }

    "returns SpamBlockedCreateResponse when playlists responds with ResourceExhausted" in new CreatePlaylistLike {
      playlistTwinagleClient.likePlaylist(request) returns Future.exception(
        TwinagleException(ErrorCode.ResourceExhausted, "Spam alert")
      )

      Await.result(likesService.createPlaylistLike(session, playlistUrn)) ==== HttpServiceError(
        HttpResponseFields(Status.TooManyRequests.code)
      ).bad

    }
  }

  "#deletePlaylistLike" >> {
    trait DeletePlaylistLike extends Context {
      val userUrn = Urn("soundcloud", "users", "1")
      val playlistUrn = Urn("soundcloud", "playlist", "1")
      val created = Instant.now
      val timestamp = Timestamp.of(created.getEpochSecond, created.getNano)
      val request = LikePlaylistRequest(userSession = Some(session.asProtoSession), urn = playlistUrn.toString)
    }

    "returns an OkDeletedResponse when Playlists successfully deletes a like" in new DeletePlaylistLike {
      playlistTwinagleClient.unlikePlaylist(request) returns Future.value(
        LikePlaylistResponse(
          Some(timestamp),
          playlistUrn.toString,
          userUrn.toString
        )
      )

      Await.result(likesService.deletePlaylistLike(session, playlistUrn)) ==== DeleteLikeResponse().good
    }

    "returns NotFoundDeleteResponse when playlists responds with NotFound" in new DeletePlaylistLike {
      playlistTwinagleClient.unlikePlaylist(request) returns Future.exception(
        TwinagleException(ErrorCode.NotFound, "Resource not found")
      )

      Await.result(likesService.deletePlaylistLike(session, playlistUrn)) ==== NotFound().bad
    }

    "returns exception when playlists responds with unexpected error" in new DeletePlaylistLike {
      playlistTwinagleClient.unlikePlaylist(request) returns Future.exception(
        TwinagleException(ErrorCode.Internal, "Internal error")
      )

      Await.result(likesService.deletePlaylistLike(session, playlistUrn)) must throwA[RuntimeException]
    }

  }

  trait LikesByUserContext extends Context {
    def buildLikesRequest(collections: Seq[likesCollection]) = GetLikesByUserChronoRequest(
      userUrn = requestingUserUrn.toString,
      chronoParams = Some(
        ChronoParams(
          direction = ChronoDirection.DESC,
          limit = Some(pagination.pageSize),
          cursor = pagination.cursor
        )
      ),
      collections = collections
    )

    def buildV2LikesRequest(collections: Seq[v2likesCollection]) = v2GetLikesByUserChronoRequest(
      userUrn = requestingUserUrn.toString,
      chronoParams = Some(
        v2ChronoParams(
          direction = v2ChronoParams.Direction.DESC,
          limit = Some(pagination.pageSize),
          cursor = pagination.cursor
        )
      ),
      collections = collections
    )

    val access = AccessParams.defaultAccess
    val playlist = new PlaylistBuilder().build
    val playlistUrn = Urn("soundcloud", "playlists", "1001")

    lazy val tracksCollection =
      Await.result(likesService.userTracksLikes(session, requestingUserUrn, access, pagination))
    lazy val playlistsCollection = Await.result(likesService.userPlaylistsLikes(session, requestingUserUrn, pagination))
  }

  trait RolloutEnabledContext extends LikesByUserContext {
    rollout.isActive(useLikesV2Rollout) returns Future.value(true)
  }

  trait RolloutDisabledContext extends LikesByUserContext {
    rollout.isActive(useLikesV2Rollout) returns Future.value(false)
  }

  "#userTracksLikes" >> {
    val createdAtUTC = "2025-08-26T10:15:30.123Z"

    "when all data is available" in new LikesByUserContext with RolloutDisabledContext {
      val likesRequest = buildLikesRequest(Seq(likesCollection.TRACKS))
      val likesPage = GetLikesByUserChronoResponse(
        items = Seq(
          GetLikesChronoResponseItem(
            createdAt.toString,
            "user-track-likes",
            requestingUserUrn.toString,
            trackUrn.toString,
            "some_cursor"
          )
        )
      )

      when(trackRepresentationsService.tracks(session, List(trackRequest), access))
        .thenReturn(Future.value(List(createTrackRepresentation)))
      when(likesTwinagleClient.getLikesByUserChrono(likesRequest))
        .thenReturn(Future.value(likesPage))

      tracksCollection match {
        case rep => rep must beAnInstanceOf[Collection[TrackRepresentation]]
      }
      tracksCollection.nextHref === Some(pagination.nextPage("some_cursor").normalizedHref)
    }

    "use likesV2Client when rollout is active" in new LikesByUserContext with RolloutEnabledContext {
      val likesRequest = buildLikesRequest(Seq(likesCollection.TRACKS))
      val v2LikesRequest = buildV2LikesRequest(Seq(v2likesCollection.TRACKS))

      val v2LikesPage = v2ChronoResponse(
        items = Seq(
          v2ChronoResponse.Item(
            Some(Timestamp(Instant.parse(createdAtUTC))),
            "user-track-likes",
            requestingUserUrn.toString,
            trackUrn.toString,
            "some_cursor"
          )
        )
      )

      when(trackRepresentationsService.tracks(session, List(trackRequest), access))
        .thenReturn(Future.value(List(createTrackRepresentation)))
      when(v2LikesTwinagleClient.getLikesByUserChrono(v2LikesRequest))
        .thenReturn(Future.value(v2LikesPage))

      tracksCollection match {
        case rep => rep must beAnInstanceOf[Collection[TrackRepresentation]]
      }
      tracksCollection.nextHref === Some(pagination.nextPage("some_cursor").normalizedHref)

      there was no(likesTwinagleClient).getLikesByUserChrono(likesRequest)
      there was one(v2LikesTwinagleClient).getLikesByUserChrono(v2LikesRequest)
    }

    "do not use likesV2Client when rollout is inactive" in new LikesByUserContext with RolloutDisabledContext {
      val likesRequest = buildLikesRequest(Seq(likesCollection.TRACKS))
      val likesPage = GetLikesByUserChronoResponse(
        items = Seq(
          GetLikesChronoResponseItem(
            createdAtUTC,
            "user-track-likes",
            requestingUserUrn.toString,
            trackUrn.toString,
            "some_cursor"
          )
        )
      )

      when(trackRepresentationsService.tracks(session, List(trackRequest), access))
        .thenReturn(Future.value(List(createTrackRepresentation)))
      when(likesTwinagleClient.getLikesByUserChrono(likesRequest))
        .thenReturn(Future.value(likesPage))

      tracksCollection match {
        case rep => rep must beAnInstanceOf[Collection[TrackRepresentation]]
      }
      tracksCollection.nextHref === Some(pagination.nextPage("some_cursor").normalizedHref)

      there was one(likesTwinagleClient).getLikesByUserChrono(likesRequest)
      there was no(v2LikesTwinagleClient).getLikesByUserChrono(any)
    }

    "when there is no next href" in new LikesByUserContext with RolloutDisabledContext {
      val likesRequest = buildLikesRequest(Seq(likesCollection.TRACKS))
      val likesPage = GetLikesByUserChronoResponse()

      when(trackRepresentationsService.tracks(session, List(), access))
        .thenReturn(Future.value(List()))
      when(likesTwinagleClient.getLikesByUserChrono(likesRequest))
        .thenReturn(Future.value(likesPage))

      tracksCollection match {
        case rep => rep must beAnInstanceOf[Collection[TrackRepresentation]]
      }
      tracksCollection.nextHref === None
    }
  }

  "#userPlaylistsLikes" >> {
    "when all data is available" in new LikesByUserContext {
      val likesRequest = buildLikesRequest(Seq(likesCollection.PLAYLISTS))
      val likesPage = GetLikesByUserChronoResponse(
        items = Seq(
          GetLikesChronoResponseItem(
            createdAt.toString,
            "user-playlist-likes",
            requestingUserUrn.toString,
            playlistUrn.toString,
            "some_cursor"
          )
        )
      )

      when(playlistsService.fetchPlaylistsMetadataOnly(session, List(PlaylistRequest(playlistUrn, None))))
        .thenReturn(Future.value(List(playlist)))
      when(likesTwinagleClient.getLikesByUserChrono(likesRequest))
        .thenReturn(Future.value(likesPage))

      playlistsCollection match {
        case rep => rep must beAnInstanceOf[Collection[Playlist]]
      }
      playlistsCollection.nextHref === Some(pagination.nextPage("some_cursor").normalizedHref)
    }

    "when there is no next href" in new LikesByUserContext {
      val likesRequest = buildLikesRequest(Seq(likesCollection.PLAYLISTS))
      val likesPage = GetLikesByUserChronoResponse()

      when(playlistsService.fetchPlaylistsMetadataOnly(session, List()))
        .thenReturn(Future.value(List()))
      when(likesTwinagleClient.getLikesByUserChrono(likesRequest))
        .thenReturn(Future.value(likesPage))

      playlistsCollection match {
        case rep => rep must beAnInstanceOf[Collection[Playlist]]
      }
      playlistsCollection.nextHref === None
    }
  }
}
