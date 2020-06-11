package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.client.tracks.{TrackRequest}
import com.soundcloud.publicApiStrangler.service.trackrepresentation._
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when

class UserTracksServiceSpec extends TrackRepresentationsSpecificationContext {

  trait Context extends TrackRepresentationsContext {

    val trackRepresentationsService = mock[TrackRepresentationsService]
    val trackmetadataClient = mock[TrackmetadataClient]
    val trackPagination = mock[TrackPagination]

    val userTracksService = new UserTracksService(
      trackRepresentationsService,
      trackmetadataClient
    )

    val track = createTrackRepresentation

    def setUpMocksForMultipleExistingTracks(
        track: TrackRepresentation,
        session: UserSession
    ) = {
      when(trackRepresentationsService.tracks(session, List(TrackRequest(track.track.urn, None))))
        .thenReturn(Future.value(List(track)))
      when(trackmetadataClient.urnsByUser(session, trackOwnerUrn)).thenReturn(Future.value(List(track.track.urn)))
      when(trackPagination.calculateFinalPage(List(track))).thenReturn(List(track))
      when(trackPagination.calculateTrackUrnPage(List(track.track.urn))).thenReturn(Set(track.track.urn))
    }

    def setUpMocksForExistingTrack(
        track: TrackRepresentation,
        session: UserSession,
        secretToken: Option[String]
    ) = {
      when(trackRepresentationsService.track(session, TrackRequest(track.track.urn, secretToken)))
        .thenReturn(Future.value(Some(track)))
    }
  }

  "#userTracks" >> {
    "when all data is available" in new Context {
      setUpMocksForMultipleExistingTracks(track, session)

      val tracksCollection = Await.result(userTracksService.userTracks(session, trackOwnerUrn, trackPagination))

      tracksCollection match {
        case rep =>
          rep must beAnInstanceOf[TracksCollection]
      }
    }

    "#userTrack" >> {
      "when all data is available" in new Context {
        setUpMocksForExistingTrack(track, session, Some("secr3t-Token"))

        val trackRepresentation =
          Await.result(
            userTracksService.userTrack(track.track.urn, session, track.user.urn.identifier, Some("secr3t-Token"))
          )

        trackRepresentation match {
          case Some(rep) => rep must beAnInstanceOf[TrackRepresentation]
          case _ => false
        }

      }

      "when data is not available" in new Context {
        when(trackRepresentationsService.track(session, TrackRequest(track.track.urn, Some("secr3t-Token"))))
          .thenReturn(Future.value(None))

        val trackRepresentation =
          Await.result(userTracksService.userTrack(track.track.urn, session, "123", Some("secr3t-Token")))

        trackRepresentation.isEmpty
      }
    }
  }
}
