package com.soundcloud.apipublic.service

import com.google.protobuf.timestamp.Timestamp
import com.soundcloud.apipublic.client.liebling._
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
import com.soundcloud.jvmkit.module.rollout.Rollout
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.finagle.http.{ParamMap, Status}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import org.specs2.mutable.BeforeAfter
import proto.soundcloud.likes.{api => likes}
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

  trait Context extends TrackRepresentationsContext with BeforeAfter {

    val trackRepresentationsService = mock[TrackRepresentationsService]
    val playlistsService = mock[PlaylistsService]
    val lieblingClient = mock[LieblingClient]
    val pagination = CursorBasedPagination(
      "https://api.soundcloud.com",
      "/users/1/favorites/",
      ParamMap(),
      Some("2"),
      1
    )
    val tracksTwinagleClient = mock[TrackLikesClientProtobuf]
    val likesTwinagleClient = mock[likes.LikesClientProtobuf]
    val playlistTwinagleClient = mock[PlaylistLikesClientProtobuf]
    val rollout = mock[Rollout]

    val likesService = new LikesService(
      trackRepresentationsService,
      playlistsService,
      lieblingClient,
      likesTwinagleClient,
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

  "#userTrackLikeForUrn" >> {
    "when all data is available" in new Context {
      val track = trackVisibilityTrack()
      when(trackRepresentationsService.tracks(session, List(trackRequest), AccessParams.defaultAccess))
        .thenReturn(Future.value(List(createTrackRepresentation)))
      when(lieblingClient.userTracksLikesForUrns(session, trackOwnerUrn, List(track.urn)))
        .thenReturn(Future.value(List(trackUrn)))

      val tracksCollection =
        Await.result(likesService.userTrackLikeForUrn(session, trackOwnerUrn, track.urn))

      tracksCollection match {
        case rep =>
          rep must beAnInstanceOf[Option[TrackRepresentation]]
      }
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

    "returns an OkCreatedCreateResponse when Playlists successfully creates a like, rollout flag is on" in new CreatePlaylistLike {
      playlistTwinagleClient.likePlaylist(request) returns Future.value(
        LikePlaylistResponse(
          Some(timestamp),
          playlistUrn.toString,
          userUrn.toString
        )
      )
      rollout.isActive(any) returns Future.value(true)
      Await.result(likesService.createPlaylistLike(session, playlistUrn)) ==== CreateLikeResponse().good
    }

    "returns an OkCreatedCreateResponse when Lieblings successfully creates a like, rollout flag is off" in new CreatePlaylistLike {
      lieblingClient.createPlaylistLike(session, playlistUrn) returns Future.value(CreateLikeResponse().good)
      rollout.isActive(any) returns Future.value(false)

      Await.result(likesService.createPlaylistLike(session, playlistUrn)) ==== CreateLikeResponse().good
    }

    "returns NotFoundCreateResponse when playlists responds with NotFound" in new CreatePlaylistLike {
      playlistTwinagleClient.likePlaylist(request) returns Future.exception(
        TwinagleException(ErrorCode.NotFound, "Resource not found")
      )
      rollout.isActive(any) returns Future.value(true)

      Await.result(likesService.createPlaylistLike(session, playlistUrn)) ==== NotFound().bad
    }

    "returns NotAuthorizedCreateResponse when playlists responds with PermissionDenied" in new CreatePlaylistLike {
      playlistTwinagleClient.likePlaylist(request) returns Future.exception(
        TwinagleException(ErrorCode.PermissionDenied, "Operation not allowed")
      )
      rollout.isActive(any) returns Future.value(true)

      Await.result(likesService.createPlaylistLike(session, playlistUrn)) ==== NotAllowed().bad
    }

    "returns SpamBlockedCreateResponse when playlists responds with ResourceExhausted" in new CreatePlaylistLike {
      playlistTwinagleClient.likePlaylist(request) returns Future.exception(
        TwinagleException(ErrorCode.ResourceExhausted, "Spam alert")
      )
      rollout.isActive(any) returns Future.value(true)

      Await.result(likesService.createPlaylistLike(session, playlistUrn)) ==== HttpServiceError(
        HttpResponseFields(Status.TooManyRequests.code)
      ).bad

    }
  }

  trait LikesByUserContext extends Context {
    def buildLikesRequest(collections: Seq[likes.Collection]) = likes.GetLikesByUserChronoRequest(
      userUrn = trackOwnerUrn.toString,
      chronoParams = Some(
        likes.ChronoParams(
          direction = likes.ChronoDirection.DESC,
          limit = Some(pagination.pageSize),
          cursor = pagination.cursor
        )
      ),
      collections = collections
    )

    val access = AccessParams.defaultAccess
    val playlist = new PlaylistBuilder().build
    val playlistUrn = Urn("soundcloud", "playlists", "1001")

    lazy val tracksCollection = Await.result(likesService.userTracksLikes(session, trackOwnerUrn, access, pagination))
    lazy val playlistsCollection = Await.result(likesService.userPlaylistsLikes(session, trackOwnerUrn, pagination))
  }

  "#userTracksLikes" >> {
    "when all data is available" in new LikesByUserContext {
      val likesRequest = buildLikesRequest(Seq(likes.Collection.TRACKS))
      val likesPage = likes.GetLikesByUserChronoResponse(
        items = Seq(
          likes.GetLikesChronoResponseItem(
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

    "when there is no next href" in new LikesByUserContext {
      val likesRequest = buildLikesRequest(Seq(likes.Collection.TRACKS))
      val likesPage = likes.GetLikesByUserChronoResponse()

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
      val likesRequest = buildLikesRequest(Seq(likes.Collection.PLAYLISTS))
      val likesPage = likes.GetLikesByUserChronoResponse(
        items = Seq(
          likes.GetLikesChronoResponseItem(
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
      val likesRequest = buildLikesRequest(Seq(likes.Collection.PLAYLISTS))
      val likesPage = likes.GetLikesByUserChronoResponse()

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
