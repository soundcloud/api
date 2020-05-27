package com.soundcloud.publicApiStrangler.service

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

class LikesServiceSpec extends TrackRepresentationsSpecificationContext {

  trait Context extends TrackRepresentationsContext {

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
