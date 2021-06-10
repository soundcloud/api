package com.soundcloud.publicApiStrangler.service

import com.google.protobuf.timestamp.Timestamp
import com.soundcloud.jvmkit.module.outcome.{GoodOps, NotAllowed, NotFound, UnexpectedError}
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.liebling._
import com.soundcloud.publicApiStrangler.handler.support.requestParser.AccessParams
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackRepresentation,
  TrackRepresentationsService,
  TrackRepresentationsSpecificationContext
}
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.finagle.http.ParamMap
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import org.specs2.mutable.BeforeAfter
import proto.soundcloud.tracks.api.{LikeTrackRequest, LikeTrackResponse, LikesClientProtobuf}

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

  "#userTracksLikes" >> {
    "when all data is available" in new Context {
      val track = trackVisibilityTrack()
      val likesPage = LikesPage(
        likes = List(Like(requestingUserUrn, trackUrn, createdAt.toDateTime(), None)),
        meta = LikesPageMeta(
          cursor = LikesPageCursor(
            next_params = None,
            next_href = None
          )
        )
      )
      val access = AccessParams.defaultAccess
      when(trackRepresentationsService.tracks(session, List(trackRequest), access))
        .thenReturn(Future.value(List(createTrackRepresentation)))
      when(lieblingClient.userTracksLikes(session, trackOwnerUrn, pagination.cursor, pagination.pageSize))
        .thenReturn(Future.value(likesPage))

      val tracksCollection = Await.result(likesService.userTracksLikes(session, trackOwnerUrn, access, pagination))

      tracksCollection match {
        case rep =>
          rep must beAnInstanceOf[Collection[TrackRepresentation]]
      }
    }
  }
}
