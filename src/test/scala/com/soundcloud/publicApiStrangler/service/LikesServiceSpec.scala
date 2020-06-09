package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.liebling._
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackRepresentationWaveformUrlDecorator,
  TrackRepresentationsService,
  TrackRepresentationsSpecificationContext,
  TracksCollection
}
import com.twitter.finagle.http.ParamMap
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import org.specs2.mutable.BeforeAfter

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

    val likesService = new LikesService(
      trackRepresentationsService,
      lieblingClient
    )

    override def before: Any = {}

    override def after: Any = {}
  }

  "#createTrackLike" >> {
    trait CreateTrackLike extends Context {
      lazy val itemUrn = Urn("soundcloud", "tracks", "1")
      val lieblingResult: CreateLikeResponse

      lazy val result = Await.result(likesService.createTrackLike(session, itemUrn))

      override def before: Any = {
        when(lieblingClient.createTrackLike(session, itemUrn)).thenReturn(Future.value(lieblingResult))
      }
    }

    "#when liebling successfully creates a like" >> {
      trait LikeAddedContext extends CreateTrackLike {
        override val lieblingResult = LikeCreated
      }

      "returns an OkCreatedCreateResponse" in new LikeAddedContext {
        result ==== OkCreatedCreateResponse
      }
    }

    "#when like already exists" >> {
      trait LikeAddedContext extends CreateTrackLike {
        override val lieblingResult = LikeAlreadyExists
      }

      "returns an OkCreateResponse" in new LikeAddedContext {
        result ==== OkCreateResponse
      }
    }

    "#when user is blocked" >> {
      trait LikeAddedContext extends CreateTrackLike {
        override val lieblingResult = UserBlocked
      }

      "returns an NotAuthorizedCreateResponse" in new LikeAddedContext {
        result ==== NotAuthorizedCreateResponse
      }
    }

    "#when request is rate limited" >> {
      trait LikeAddedContext extends CreateTrackLike {
        override val lieblingResult = UserHasSpamWarning
      }

      "returns an SpamBlockedCreateResponse" in new LikeAddedContext {
        result ==== SpamBlockedCreateResponse
      }
    }

    "#when something went wrong" >> {
      trait LikeAddedContext extends CreateTrackLike {
        override val lieblingResult = LikeableNotFound
      }

      "returns an NotAuthorizedCreateResponse" in new LikeAddedContext {
        result ==== NotFoundCreateResponse
      }
    }
  }

  "#userTrackLikeForUrn" >> {
    "when all data is available" in new Context {
      val track = trackvisibilityTrack()
      when(trackRepresentationsService.tracks(session, List(trackRequest)))
        .thenReturn(Future.value(List(trackRepresentationLike)))
      when(lieblingClient.userTracksLikesForUrns(session, trackOwnerUrn, List(track.urn)))
        .thenReturn(Future.value(List(trackUrn)))

      val tracksCollection =
        Await.result(likesService.userTrackLikeForUrn(session, trackOwnerUrn, track.urn))

      tracksCollection match {
        case rep =>
          rep must beAnInstanceOf[Option[TrackRepresentationWaveformUrlDecorator]]
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
        .thenReturn(Future.value(List(trackRepresentationLike)))
      when(lieblingClient.userTracksLikes(session, trackOwnerUrn, pagination.cursor, pagination.pageSize))
        .thenReturn(Future.value(likesPage))

      val tracksCollection = Await.result(likesService.userTracksLikes(session, trackOwnerUrn, pagination))

      tracksCollection match {
        case rep =>
          rep must beAnInstanceOf[TracksCollection]
      }
    }
  }
}
