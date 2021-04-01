package com.soundcloud.publicApiStrangler.service

import com.google.protobuf.timestamp.Timestamp
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.liebling._
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackRepresentation,
  TrackRepresentationsService,
  TrackRepresentationsSpecificationContext
}
import com.twitter.finagle.http.ParamMap
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import org.specs2.mutable.BeforeAfter
import proto.soundcloud.tracks.api.{LikeTrackRequest, LikeTrackResponse, LikesClientProtobuf}
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}

import java.time.Instant

class LikesServiceSpec extends TrackRepresentationsSpecificationContext {

  trait Context extends TrackRepresentationsContext with BeforeAfter {

    val trackRepresentationsService = mock[TrackRepresentationsService]
    val lieblingClient = mock[LieblingClient]
    val pagination = CursorBasedPagination(
      "https://api.soundcloud.com",
      "/users/1/favorites/",
      ParamMap(),
      Some("2"),
      1
    )
    val tracksTwinagleClient = mock[LikesClientProtobuf]

    val likesService = new LikesService(
      trackRepresentationsService,
      lieblingClient,
      tracksTwinagleClient
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
      result ==== OkCreateResponse
    }

    "returns NotFoundCreateResponse when tracks responds with NotFound" in new CreateTrackLike {
      tracksTwinagleClient.likeTrack(request) returns Future.exception(
        TwinagleException(ErrorCode.NotFound, "Track not found")
      )

      val result = Await.result(likesService.createTrackLike(session, trackUrn))
      result ==== NotFoundCreateResponse
    }

    "returns NotAuthorizedCreateResponse when tracks responds with PermissionDenied" in new CreateTrackLike {
      tracksTwinagleClient.likeTrack(request) returns Future.exception(
        TwinagleException(ErrorCode.PermissionDenied, "User blocked")
      )

      val result = Await.result(likesService.createTrackLike(session, trackUrn))
      result ==== NotAuthorizedCreateResponse
    }

    "returns SpamBlockedCreateResponse when tracks responds with ResourceExhausted" in new CreateTrackLike {
      tracksTwinagleClient.likeTrack(request) returns Future.exception(
        TwinagleException(ErrorCode.ResourceExhausted, "Spam alert")
      )

      val result = Await.result(likesService.createTrackLike(session, trackUrn))
      result ==== SpamBlockedCreateResponse
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
      result ==== LikeDeleted
    }

    "returns LikeNotFound when like was not found" in new DeleteTrackLike {
      tracksTwinagleClient.unlikeTrack(request) returns Future.exception(
        TwinagleException(ErrorCode.NotFound, "Track not found")
      )

      val result = Await.result(likesService.deleteTrackLike(session, trackUrn))
      result ==== LikeNotFound
    }
  }

  "#createPlaylistLike" >> {
    trait CreatePlaylistLike extends Context {
      lazy val itemUrn = Urn("soundcloud", "playlists", "1")
      val lieblingResult: CreateLikeResponse

      lazy val result = Await.result(likesService.createPlaylistLike(session, itemUrn))

      override def before: Any = {
        when(lieblingClient.createPlaylistLike(session, itemUrn)).thenReturn(Future.value(lieblingResult))
      }
    }

    "#when liebling successfully creates a like" >> {
      trait LikeAddedContext extends CreatePlaylistLike {
        override val lieblingResult = LikeCreated
      }

      "returns an OkCreatedCreateResponse" in new LikeAddedContext {
        result ==== OkCreatedCreateResponse
      }
    }

    "#when like already exists" >> {
      trait LikeAddedContext extends CreatePlaylistLike {
        override val lieblingResult = LikeAlreadyExists
      }

      "returns an OkCreateResponse" in new LikeAddedContext {
        result ==== OkCreateResponse
      }
    }

    "#when user is blocked" >> {
      trait LikeAddedContext extends CreatePlaylistLike {
        override val lieblingResult = UserBlocked
      }

      "returns an NotAuthorizedCreateResponse" in new LikeAddedContext {
        result ==== NotAuthorizedCreateResponse
      }
    }

    "#when request is rate limited" >> {
      trait LikeAddedContext extends CreatePlaylistLike {
        override val lieblingResult = UserHasSpamWarning
      }

      "returns an SpamBlockedCreateResponse" in new LikeAddedContext {
        result ==== SpamBlockedCreateResponse
      }
    }

    "#when something went wrong" >> {
      trait LikeAddedContext extends CreatePlaylistLike {
        override val lieblingResult = LikeableNotFound
      }

      "returns an NotAuthorizedCreateResponse" in new LikeAddedContext {
        result ==== NotFoundCreateResponse
      }
    }
  }

  "#deletePlaylistLike" >> {
    trait DeletePlaylistLike extends Context {
      lazy val itemUrn = Urn("soundcloud", "playlists", "1")
      lazy val result = Await.result(likesService.deletePlaylistLike(session, itemUrn))
      val lieblingResult: DeleteLikeResponse

      override def before: Any = {
        super.before

        when(lieblingClient.deletePlaylistLike(session, itemUrn)).thenReturn(Future.value(lieblingResult))
      }
    }

    "#when liebling successfully deletes a like" >> {
      trait LikeAddedContext extends DeletePlaylistLike {
        override val lieblingResult = LikeDeleted
      }

      "returns an LikeDeleted" in new LikeAddedContext {
        result ==== LikeDeleted
      }
    }

    "#when like was not found" >> {
      trait LikeNotFoundContext extends DeletePlaylistLike {
        override val lieblingResult = LikeNotFound
      }

      "returns an LikeNotFound" in new LikeNotFoundContext {
        result ==== LikeNotFound
      }
    }
  }

  "#userTrackLikeForUrn" >> {
    "when all data is available" in new Context {
      val track = trackvisibilityTrack()
      when(trackRepresentationsService.tracks(session, List(trackRequest)))
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

  "#userTracksLikes" >> {
    "when all data is available" in new Context {
      val track = trackvisibilityTrack()
      val likesPage = LikesPage(
        likes = List(Like(requestingUserUrn, trackUrn, createdAt.toDateTime(), None)),
        meta = LikesPageMeta(
          cursor = LikesPageCursor(
            next_params = None,
            next_href = None
          )
        )
      )
      when(trackRepresentationsService.tracks(session, List(trackRequest)))
        .thenReturn(Future.value(List(createTrackRepresentation)))
      when(lieblingClient.userTracksLikes(session, trackOwnerUrn, pagination.cursor, pagination.pageSize))
        .thenReturn(Future.value(likesPage))

      val tracksCollection = Await.result(likesService.userTracksLikes(session, trackOwnerUrn, pagination))

      tracksCollection match {
        case rep =>
          rep must beAnInstanceOf[Collection[TrackRepresentation]]
      }
    }
  }
}
