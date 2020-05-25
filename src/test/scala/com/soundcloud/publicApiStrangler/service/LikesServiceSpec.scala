package com.soundcloud.publicApiStrangler.service

import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentationWaveformUrlDecorator,
  TrackRepresentationsService,
  TrackRepresentationsSpecificationContext
}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._

class LikesServiceSpec extends TrackRepresentationsSpecificationContext {

  trait Context extends TrackRepresentationsContext {

    val trackRepresentationsService = mock[TrackRepresentationsService]
    val lieblingClient = mock[LieblingClient]
    val trackPagination = mock[TrackPagination]

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
}
