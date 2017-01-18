package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.TrackRepresentationsService
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.mediaservice.{MediaServiceUrlGenClient, WaveformUrl}
import com.soundcloud.publicApiStrangler.client.playlists.PlaylistsClient
import com.soundcloud.publicApiStrangler.client.pubmese.{Isrc, PubmeseClient}
import com.soundcloud.publicApiStrangler.client.quota.UserQuotaClient
import com.soundcloud.publicApiStrangler.client.stitch.{StitchClient, StitchCounts}
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.client.{DomainLocking, RichOkidokiClient, TrackAudioMetadata}
import com.soundcloud.publicApiStrangler.controller.PublicApiPaginationParams
import com.soundcloud.publicApiStrangler.test.util.TrackMetadataTrackBuilder
import com.soundcloud.scalakit.test.UnitSpecification
import com.soundcloud.service.response.representation.User
import com.twitter.util.{Await, Future}

class TrackRepresentationsServiceForMultipleTracksSpec extends UnitSpecification {

  trait Context extends Scope {
    val playlistsClient = mock[PlaylistsClient]
    val trackRepository = mock[TrackRepository]

    val service = new TrackRepresentationsService(
      trackRepository,
      mock[TrackmetadataClient],
      mock[RichOkidokiClient],
      mock[PubmeseClient],
      mock[StitchClient],
      mock[LieblingClient],
      mock[MediaServiceUrlGenClient],
      mock[UserQuotaClient],
      mock[TrackAccessibilityService])

    val session = anonymousSession
    val userUrn: Urn = Urn("soundcloud:users:1")
    val paginationParams = mock[PublicApiPaginationParams]

    val track = TrackMetadataTrackBuilder().build
    val tracks = List(track)
    val user = User(userUrn, "permalink", "username", "avatar_url", "permalink_url", None, None, 0, None, None, false, None, None)
    val isLiked = Map(track.urn -> true)
    val isrc = Isrc("isrc1")
    val isrcs = Map(track.urn -> isrc)
    val geoblockings = List("DE", "BR")
    val geoblockingsMap = Map(track.urn -> geoblockings)
    val domainLockings = List(DomainLocking("domain1", Urn("soundcloud:domain-lockings:1"), track.urn))
    val domainLockingsMap = Map(track.urn -> domainLockings)
    val audioMetadata = TrackAudioMetadata("state1", Some("original_format"), Some(1))
    val audios = Map(track.urn -> audioMetadata)
    val count = StitchCounts(1, 2, 3, 4, 5)
    val counts = Map(track.urn -> count)
    val labelUser = user
    val labels = Map(track.urn -> labelUser)
    val waveformUrls = Seq(WaveformUrl("label1_1", "json1_1", "png1_1"), WaveformUrl("label1_2", "json1_2", "png1_2"))
    val waveformUrlsMap = Map(track.uid.get -> waveformUrls)
    val downloadsPerTrack = Map(track.urn -> Some(10))
    val accessibilityCheck = Map(track.urn -> true)

    val completeTrackResult = TracksResult(
      tracks, user, isLiked, isrcs, geoblockingsMap, domainLockingsMap, audios, counts, labels, waveformUrlsMap, downloadsPerTrack, accessibilityCheck)

    def tracksResult: TracksResult

    def result = Await.result(service.tracks(session, userUrn, paginationParams))

    trackRepository.tracks(session, userUrn, paginationParams).returns(Future.value(tracksResult))
  }

  "#tracks" >> {
    "when all data is available" >> {
      trait AllData extends Context {
        override def tracksResult = completeTrackResult
      }

      "it maps the tracks" in new AllData {
        result ==== List(new TrackRepresentationBuilder().build(
          Option(session.getUser), track, user, Some(isrc), count, Some(labelUser), Some(geoblockings), domainLockings,
          audioMetadata, true, waveformUrls, None, Some(10)))
      }
    }

    "when the track does not have a uid it maps to nothing" in new Context {
      override def tracksResult = completeTrackResult.copy(tracks = List(TrackMetadataTrackBuilder(uid = None).build))

      result ==== List()
    }

    "when the track's audio is not available it maps to nothing" in new Context {
      override def tracksResult = completeTrackResult.copy(audios = Map.empty)

      result ==== List()
    }

    "when the track's waveform is not available it maps to nothing" in new Context {
      override def tracksResult = completeTrackResult.copy(waveformUrls = Map.empty)

      result ==== List()
    }

    "when the track's waveform is not available it maps to nothing" in new Context {
      override def tracksResult = completeTrackResult.copy(waveformUrls = Map.empty)

      result ==== List()
    }

    "when the track's stitch counts are not available it maps zero" in new Context {
      override def tracksResult = completeTrackResult.copy(counts = Map.empty)

      result ==== List(new TrackRepresentationBuilder().build(
        Option(session.getUser), track, user, Some(isrc),
        StitchCounts(0, 0, 0, 0, 0), // relevant bit
        Some(labelUser), Some(geoblockings), domainLockings, audioMetadata, true, waveformUrls, None, Some(10)))
    }

    "when the track's domain lockings are not available it maps empty" in new Context {
      override def tracksResult = completeTrackResult.copy(domainLockings = Map.empty)

      result ==== List(new TrackRepresentationBuilder().build(
        Option(session.getUser), track, user, Some(isrc), count, Some(labelUser), Some(geoblockings),
        List.empty, // relevant bit
        audioMetadata, true, waveformUrls, None, Some(10)))
    }

    "when the track's liked status is not available it maps to false" in new Context {
      override def tracksResult = completeTrackResult.copy(isLiked = Map.empty)

      result ==== List(new TrackRepresentationBuilder().build(
        Option(session.getUser), track, user, Some(isrc), count, Some(labelUser), Some(geoblockings), domainLockings, audioMetadata,
        false, // relevant bit
        waveformUrls, None, Some(10))) }

    "when the track's downloads is not available it maps to None" in new Context {
      override def tracksResult = completeTrackResult.copy(downloadsPerTrack = Map.empty)

      result ==== List(new TrackRepresentationBuilder().build(
        Option(session.getUser), track, user, Some(isrc), count, Some(labelUser), Some(geoblockings), domainLockings, audioMetadata, true, waveformUrls,
        None, // relevant bit
        None))
    }
  }
}
