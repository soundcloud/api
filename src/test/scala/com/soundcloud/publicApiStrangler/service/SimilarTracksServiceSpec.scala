package com.soundcloud.publicApiStrangler.service

import java.net.URL

import com.soundcloud.api.partners.clients.tracks.Transcoding
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.SystemPlaylistsClient
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.User
import com.soundcloud.publicApiStrangler.client.trackmetadata.Track
import com.soundcloud.publicApiStrangler.client.tracks.{TrackRequest, VisibleTrackBuilder}
import com.soundcloud.publicApiStrangler.mapper.similarsounds.{SimilarSounds, SimilarSoundsMeta}
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentationLikeSpecContext,
  TrackRepresentationsService,
  TracksCollection
}
import com.twitter.util.{Await, Future}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import org.mockito.Mockito.when

class SimilarTracksServiceSpec extends UnitSpecification {

  trait Context extends TrackRepresentationLikeSpecContext with Scope {
    val loggedInUser = Urn("soundcloud", "users", "1")

    val session = loggedInSession(loggedInUser)

    val track = Urn("soundcloud", "tracks", "1")
    val similarTrack = Urn("soundcloud", "tracks", "2")
    val similarTrackOwner = Urn("soundcloud", "users", "2")

    def similarTrackOwnerUser =
      User(
        urn = similarTrackOwner,
        permalink = "similarUser",
        username = "SimilarUser123",
        avatar_url = "",
        permalink_url = "https://soundcloud.com/SimilarUser123",
        city = None,
        country = None,
        tracks_count = 1,
        followers_count = Some(20000),
        followings_count = Some(20),
        verified = false,
        updated_at = Some("2019/06/27 11:21:36 +0000"),
        description = Some("-")
      )

    val systemPlaylistsClient = mock[SystemPlaylistsClient]
    val trackVisibilityService = mock[TrackVisibilityService]
    val trackRepresentationService = mock[TrackRepresentationsService]

    val similarSoundsMock = SimilarSounds(
      Seq(similarTrack),
      SimilarSoundsMeta(50, "variant", "source", Urn("soundcloud", "systems", "123"))
    )

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

    val trackRepresentationLikeMock =
      createTrackRepresentation(Track.fromVisibleTrack(visibleTrackMock), similarTrackOwnerUser)

    val similarTracksService =
      new SimilarTracksService(trackVisibilityService, trackRepresentationService, systemPlaylistsClient)

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
      when(trackVisibilityService.tracks(session, List(TrackRequest(similarTrack, None))))
        .thenReturn(Future(List(visibleTrackMock)))
      when(trackRepresentationService.enrichTracks(session, List(visibleTrackMock)))
        .thenReturn(Future(List(trackRepresentationLikeMock)))

      val similarTracks = Await.result(similarTracksService.similarTracks(session, track, trackPagination))

      similarTracks match {
        case Some(res) => res must beAnInstanceOf[TracksCollection]
        case _ => failure
      }
    }

    "returns None when no track recommendations" in new Context {
      when(systemPlaylistsClient.fetchSimilar(session, track)).thenReturn(Future(None))
      when(trackVisibilityService.tracks(session, List.empty))
        .thenReturn(Future(List.empty))
      when(trackRepresentationService.enrichTracks(session, List.empty))
        .thenReturn(Future(List.empty))

      val similarTracks = Await.result(similarTracksService.similarTracks(session, track, trackPagination))

      similarTracks must beEmpty
    }
  }
}
