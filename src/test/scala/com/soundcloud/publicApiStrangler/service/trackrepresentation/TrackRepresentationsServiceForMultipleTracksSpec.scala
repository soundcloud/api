package com.soundcloud.publicApiStrangler.service.trackrepresentation

import com.soundcloud.jvmkit.module.util.{Url, Urn}
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.media.{TrackWaveformUrl, WaveformUrlsGenerator}
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.User
import com.soundcloud.publicApiStrangler.client.mothership.{DomainLocking, RichOkidokiClient, TrackAudioMetadata}
import com.soundcloud.publicApiStrangler.client.playlists.PlaylistsClient
import com.soundcloud.publicApiStrangler.client.pubmese.{Isrc, PubmeseClient}
import com.soundcloud.publicApiStrangler.client.quota.UserQuotaClient
import com.soundcloud.publicApiStrangler.client.stitch.{StitchClient, StitchCounts}
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.service.TrackAccessibilityService
import com.soundcloud.publicApiStrangler.support
import com.soundcloud.publicApiStrangler.support.Good
import com.soundcloud.publicApiStrangler.support.ResultF.lift
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.util.TrackMetadataTrackBuilder
import com.twitter.util.Await

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
      mock[WaveformUrlsGenerator],
      mock[UserQuotaClient],
      mock[TrackAccessibilityService])

    val session = anonymousSession
    val userUrn: Urn = Urn("soundcloud", "users", "1")
    val paginationParams = mock[TrackPagination]

    val track = TrackMetadataTrackBuilder().build
    val tracks = List(track)
    val user = User(userUrn, "permalink", "username", "avatar_url", "permalink_url", None, None, 0, None, None, false, None, None)
    val isLiked = Map(track.urn -> true)
    val isrc = Isrc("isrc1")
    val isrcs = Map(track.urn -> isrc)
    val geoblockings = List("DE", "BR")
    val geoblockingsMap = Map(track.urn -> geoblockings)
    val domainLockings = List(DomainLocking("domain1", Urn("soundcloud", "domain-lockings", "1"), track.urn))
    val domainLockingsMap = Map(track.urn -> domainLockings)
    val audioMetadata = TrackAudioMetadata("state1", Some("original_format"), Some(1))
    val audios = Map(track.urn -> audioMetadata)
    val count = StitchCounts(1, 2, 3, 4, 5)
    val counts = Map(track.urn -> count)
    val labelUser = user
    val labels = Map(track.urn -> labelUser)
    val waveformUrl = TrackWaveformUrl("uid_1", Url("http://foo.bar/a.png"))
    val waveformUrlsMap = Map(track.uid.get -> waveformUrl)
    val downloadsPerTrack = Map(track.urn -> Some(10))

    val completeTrackResult = TracksResult(
      tracks, user, isLiked, isrcs, geoblockingsMap, domainLockingsMap, audios, counts, labels, waveformUrlsMap, downloadsPerTrack, None)

    def tracksResult: support.Result[TracksResult]

    def result = Await.result(service.tracks(session, userUrn, paginationParams).value)

    trackRepository.tracksByUser(session, userUrn, paginationParams).returns(lift(tracksResult))
  }

  "#tracks" >> {
    "when all data is available" >> {
      trait AllData extends Context {
        override def tracksResult = Good(completeTrackResult)
      }

      "it maps the tracks" in new AllData {
        result ==== Good(TracksRepresentationResult(List(new TrackRepresentationBuilder().build(
          Option(session.getUser), track, user, Some(isrc), count, Some(labelUser), geoblockings, domainLockings,
          audioMetadata, true, waveformUrl, None, Some(10))), None))
      }
    }

    "when the track does not have a uid it maps to nothing" in new Context {
      override def tracksResult = Good(completeTrackResult.copy(tracks = List(TrackMetadataTrackBuilder(uid = None).build)))

      result ==== Good(TracksRepresentationResult(List.empty, None))
    }

    "when the track's audio is not available it maps to nothing" in new Context {
      override def tracksResult = Good(completeTrackResult.copy(audios = Map.empty))

      result ==== Good(TracksRepresentationResult(List.empty, None))
    }

    "when the track's waveform is not available it maps to nothing" in new Context {
      override def tracksResult = Good(completeTrackResult.copy(waveformUrls = Map.empty))

      result ==== Good(TracksRepresentationResult(List.empty, None))
    }

    "when the track's waveform is not available it maps to nothing" in new Context {
      override def tracksResult = Good(completeTrackResult.copy(waveformUrls = Map.empty))

      result ==== Good(TracksRepresentationResult(List.empty, None))
    }

    "when the track's stitch counts are not available it maps zero" in new Context {
      override def tracksResult = Good(completeTrackResult.copy(counts = Map.empty))

      result ==== Good(TracksRepresentationResult(List(new TrackRepresentationBuilder().build(
        Option(session.getUser), track, user, Some(isrc),
        StitchCounts(0, 0, 0, 0, 0), // relevant bit
        Some(labelUser), geoblockings, domainLockings, audioMetadata, true, waveformUrl, None, Some(10))), None))
    }

    "when the track's domain lockings are not available it maps empty" in new Context {
      override def tracksResult = Good(completeTrackResult.copy(domainLockings = Map.empty))

      result ==== Good(TracksRepresentationResult(List(new TrackRepresentationBuilder().build(
        Option(session.getUser), track, user, Some(isrc), count, Some(labelUser), geoblockings,
        List.empty, // relevant bit
        audioMetadata, true, waveformUrl, None, Some(10))), None))
    }

    "when the track's liked status is not available it maps to false" in new Context {
      override def tracksResult = Good(completeTrackResult.copy(isLiked = Map.empty))

      result ==== Good(TracksRepresentationResult(List(new TrackRepresentationBuilder().build(
        Option(session.getUser), track, user, Some(isrc), count, Some(labelUser), geoblockings, domainLockings, audioMetadata,
        false, // relevant bit
        waveformUrl, None, Some(10))), None))
    }

    "when the track's downloads is not available it maps to None" in new Context {
      override def tracksResult = Good(completeTrackResult.copy(downloadsPerTrack = Map.empty))

      result ==== Good(TracksRepresentationResult(List(new TrackRepresentationBuilder().build(
        Option(session.getUser), track, user, Some(isrc), count, Some(labelUser), geoblockings, domainLockings, audioMetadata, true, waveformUrl,
        None, // relevant bit
        None)), None))
    }
  }
}
