package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.session.{UserSession, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.client.pubmese.Isrc
import com.soundcloud.publicApiStrangler.client.stitch.StitchCounts
import com.soundcloud.publicApiStrangler.client.trackmetadata.{Track, TrackmetadataClient}
import com.soundcloud.publicApiStrangler.client.tracks.VisibleTrack
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

    val session: UserSession = new UserSessionBuilder().setUser(requestingUserUrn).build()
    val builder = new TrackRepresentationBuilder

    def trackRepresentationLike: TrackRepresentationLike =
      builder.build(
        sessionUser = session.user,
        track = Track.fromVisibleTrack(trackvisibilityTrack()),
        user = trackOwner,
        isrc = Some(Isrc("US-S1Z-99-00001")),
        counts = StitchCounts(111, 222, 333, 444, 555),
        label = None,
        geoblockings = geoblockingsList,
        domainLockings = domainLockingsList,
        trackAudioMetadata = trackAudioMetadataList,
        isLiked = true,
        waveformUrl = waveformUrl(trackUrn.identifier),
        downloadsPerTrack = Some(0)
      )

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
  }
}
