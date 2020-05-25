package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.client.tracks.{TrackRequest, VisibleTrack}
import com.soundcloud.publicApiStrangler.service.trackrepresentation._
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when

class UserTracksServiceSpec extends TrackRepresentationsSpecificationContext {

  trait Context extends TrackRepresentationsContext {

    val trackVisibilityService = mock[TrackVisibilityService]
    val trackRepresentationsService = mock[TrackRepresentationsService]
    val trackmetadataClient = mock[TrackmetadataClient]
    val trackPagination = mock[TrackPagination]

    val userTracksService = new UserTracksService(
      trackVisibilityService,
      trackRepresentationsService,
      trackmetadataClient
    )

    val builder = new TrackRepresentationBuilder

    def setUpMocksForMultipleExistingTracks(
        track: VisibleTrack,
        session: UserSession
    ) = {
      when(trackVisibilityService.tracks(session, List(trackRequest)))
        .thenReturn(Future.value(List(track)))
      when(trackRepresentationsService.enrichTracks(session, List(track)))
        .thenReturn(Future.value(List(trackRepresentationLike)))
      when(trackmetadataClient.urnsByUser(session, trackOwnerUrn)).thenReturn(Future.value(List(trackUrn)))
      when(trackPagination.calculateTrackUrnPage(List(trackUrn))).thenReturn(Set(trackUrn))
      when(trackPagination.calculateFinalPage(List(track))).thenReturn(List(track))
    }

    def setUpMocksForExistingTrack(
        track: VisibleTrack,
        session: UserSession,
        secretToken: Option[String]
    ) = {
      when(trackVisibilityService.tracks(session, List(TrackRequest(trackUrn, secretToken))))
        .thenReturn(Future.value(List(track)))
      when(trackRepresentationsService.enrichTracks(session, List(track)))
        .thenReturn(Future.value(List(trackRepresentationLike)))
      when(trackRepresentationsService.enrichTracks(session, List.empty))
        .thenReturn(Future.value(List.empty))
    }
  }

  "#userTracks" >> {
    "when all data is available" in new Context {
      val track = trackvisibilityTrack()
      setUpMocksForMultipleExistingTracks(track, session)

      val tracksCollection = Await.result(userTracksService.userTracks(session, trackOwnerUrn, trackPagination))

      tracksCollection match {
        case rep =>
          rep must beAnInstanceOf[TracksCollection]
      }
    }

    "#userTrack" >> {
      "when all data is available" in new Context {
        val track = trackvisibilityTrack()
        setUpMocksForExistingTrack(track, session, Some("secr3t-Token"))

        val trackRepresentation =
          Await.result(userTracksService.userTrack(trackUrn, session, trackOwnerUrn.identifier, track.secretToken))

        trackRepresentation match {
          case Some(rep) => rep must beAnInstanceOf[TrackRepresentationLike]
          case _ => false
        }

      }

      "when data is not available" in new Context {
        val track = trackvisibilityTrack()
        setUpMocksForExistingTrack(track, session, Some("secr3t-Token"))

        val trackRepresentation =
          Await.result(userTracksService.userTrack(trackUrn, session, "123", track.secretToken))

        trackRepresentation.isEmpty
      }
    }
  }
}
