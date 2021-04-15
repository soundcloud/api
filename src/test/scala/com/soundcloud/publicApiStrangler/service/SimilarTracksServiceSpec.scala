package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.SystemPlaylistsClient
import com.soundcloud.publicApiStrangler.client.tracks.{TrackRequest, Transcoding, VisibleTrackBuilder}
import com.soundcloud.publicApiStrangler.handler.support.requestParser.AccessParams
import com.soundcloud.publicApiStrangler.mapper.similarsounds.{SimilarSounds, SimilarSoundsMeta}
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentation,
  TrackRepresentationSpecContext,
  TrackRepresentationsService
}
import com.soundcloud.publicApiStrangler.service.users.UserBuilder
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when

import java.net.URL

class SimilarTracksServiceSpec extends UnitSpecification {

  trait Context extends TrackRepresentationSpecContext with Scope {
    val loggedInUser = Urn("soundcloud", "users", "1")

    val session = loggedInSession(loggedInUser)

    val track = Urn("soundcloud", "tracks", "1")
    val similarTrack = Urn("soundcloud", "tracks", "2")
    val similarTrackOwner = Urn("soundcloud", "users", "2")

    def similarTrackOwnerUser = new UserBuilder().setUrn(similarTrackOwner).build

    val systemPlaylistsClient = mock[SystemPlaylistsClient]
    val trackVisibilityService = mock[TrackVisibilityService]
    val trackRepresentationService = mock[TrackRepresentationsService]

    val similarSoundsMock = SimilarSounds(
      Seq(similarTrack),
      SimilarSoundsMeta(50, "variant", "source", Urn("soundcloud", "systems", "123"))
    )

    val similarSoundsMockUrns = similarSoundsMock.similarTracks.toList

    val transcodings = List(
      Transcoding("mp3-uuid", "preset", "audio/mpeg", List("progressive"), None, "sq", 180000, None)
    )

    val visibleTrackMock =
      (new VisibleTrackBuilder)
        .setUrn(similarTrack)
        .setUserUrn(similarTrackOwner)
        .setDisabledAt(None)
        .setTranscodings(transcodings)
        .setSecretToken(Some("secret"))
        .build

    val trackRepresentationMock =
      createTrackRepresentation(visibleTrack = visibleTrackMock, user = similarTrackOwnerUser)

    val similarTracksService =
      new SimilarTracksService(trackRepresentationService, systemPlaylistsClient)

    val trackPagination = TrackPagination(
      Some(1),
      Some(0),
      false,
      None,
      None,
      new URL("https://api.soundcloud.com/tracks/1/related?limit=1&offset=2")
    )
  }

  "#similarTracks" >> {
    "returns similar tracks when they exist" in new Context {
      when(systemPlaylistsClient.fetchSimilar(session, track)).thenReturn(Future(Some(similarSoundsMock)))
      when(
        trackRepresentationService
          .tracks(session, similarSoundsMockUrns.map(TrackRequest(_, None)), AccessParams.defaultAccess)
      ).thenReturn(Future(List(trackRepresentationMock)))

      val similarTracks = Await.result(similarTracksService.similarTracks(session, track, trackPagination))

      similarTracks match {
        case Some(res) => res must beAnInstanceOf[Collection[TrackRepresentation]]
        case _ => failure
      }
    }

    "returns None when no track recommendations" in new Context {
      when(systemPlaylistsClient.fetchSimilar(session, track)).thenReturn(Future(None))
      when(trackRepresentationService.tracks(session, List.empty, AccessParams.defaultAccess))
        .thenReturn(Future(List.empty))

      val similarTracks = Await.result(similarTracksService.similarTracks(session, track, trackPagination))

      similarTracks must beEmpty
    }
  }
}
