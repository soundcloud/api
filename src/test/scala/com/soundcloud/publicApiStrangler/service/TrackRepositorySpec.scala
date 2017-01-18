package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.TrackRepresentationsService
import com.soundcloud.publicApiStrangler.client.{DomainLocking, RichOkidokiClient, TrackAudioMetadata}
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.mediaservice.{MediaServiceUrlGenClient, WaveformUrl}
import com.soundcloud.publicApiStrangler.client.playlists.PlaylistsClient
import com.soundcloud.publicApiStrangler.client.pubmese.{Isrc, PubmeseClient}
import com.soundcloud.publicApiStrangler.client.quota.UserQuotaClient
import com.soundcloud.publicApiStrangler.client.stitch.{StitchClient, StitchCounts}
import com.soundcloud.publicApiStrangler.client.trackmetadata.{Artwork, EmbeddingPermission, Track, TrackmetadataClient}
import com.soundcloud.publicApiStrangler.controller.PublicApiPaginationParams
import com.soundcloud.publicApiStrangler.representation.TrackRepresentationLike
import com.soundcloud.scalakit.test.UnitSpecification
import com.soundcloud.service.response.representation.User
import com.twitter.util.{Await, Future}
import org.joda.time.LocalDateTime

class TrackRepositorySpec extends UnitSpecification {
  trait Context extends Scope {
    val trackmetadataClient = mock[TrackmetadataClient]
    val richOkidokiClient = mock[RichOkidokiClient]
    val pubmeseClient = mock[PubmeseClient]
    val stitchClient = mock[StitchClient]
    val lieblingClient = mock[LieblingClient]
    val mediaServiceUrlGenClient = mock[MediaServiceUrlGenClient]
    val userQuotaClient = mock[UserQuotaClient]
    val playlistsClient = mock[PlaylistsClient]
    val trackAccessibilityService = mock[TrackAccessibilityService]

    val repository = new TrackRepository(
      trackmetadataClient,
      richOkidokiClient,
      pubmeseClient,
      stitchClient,
      lieblingClient,
      mediaServiceUrlGenClient,
      userQuotaClient,
      trackAccessibilityService)

    val sessionUser = Urn("soundcloud:users:2398471")
    lazy val session = loggedInSession(sessionUser)

    def userUrn: Urn
    def paginationParams: PublicApiPaginationParams

    def trackmetadataTrack(urn: Urn, ownerUrn: Urn, labelId: Option[Int]) = {
      val index = urn.getIdentifier
      val now = LocalDateTime.now()

      Track(
        urn = urn,
        user_urn = ownerUrn,
        commentable = false,
        description = Some(s"description$index"),
        created_at = now,
        disabled_at = None,
        downloadable = None,
        duration = index.toInt,
        genre = None,
        last_modified = now,
        permalink = s"permalink$index",
        permalink_url = Some(s"permalink_url$index"),
        public = true,
        secret_token = s"secret_token$index",
        user_tags = List(s"tag$index", s"another_tag$index"),
        machine_tags = List.empty,
        title = s"title$index",
        uid = Some(s"uid$index"),
        api_streamable = None,
        streamable = None,
        reveal_comments = false,
        reveal_stats = false,
        label_name = Some(s"label_name$index"),
        license = null,
        embeddable = None,
        release_year = None,
        release_month = None,
        release_day = None,
        embeddableBy = EmbeddingPermission.None,
        releaseDate = None,
        artwork = Artwork(Some(s"artwork$index")),
        published_at = None,
        purchase_url = Some(s"purchase_url$index"),
        purchase_title = Some(s"purchase_title$index"),
        bpm = Some(index.toInt),
        track_type = Some(s"track_type$index"),
        release = Some(s"release$index"),
        key_signature = Some(s"key_signature$index"),
        video_url = Some(s"video_url$index"),
        label_id = labelId)
    }

    lazy val result: TracksResult = Await.result(repository.tracks(session, userUrn, paginationParams))
  }


  "#tracks" >> {
    "when requesting tracks from a different user" >> {
      trait AllGoesWell extends Context {

        override def userUrn = Urn("soundcloud:users:9218371")
        override def paginationParams = new PublicApiPaginationParams(None, None, false, None, None)

        def trackUrn(id: Int) = Urn(s"soundcloud:tracks:$id")

        val track1 = trackUrn(1)
        val track2 = trackUrn(2)
        val track3 = trackUrn(3)
        val trackUrns = Set(track1, track2, track3)

        val trackmetadataTracks = List(
          trackmetadataTrack(track3, userUrn, None),
          trackmetadataTrack(track2, userUrn, Some(2)),
          trackmetadataTrack(track1, userUrn, Some(1)))

        val tracksOwner = User(userUrn, "permalink", "username", "avatar_url", "permalink_url", None, None, 0, None, None, false, None, None)
        richOkidokiClient.fetchUserObjects(session, Set(userUrn)).returns(Future.value(List(tracksOwner)))

        trackmetadataClient.urnsByUser(session, userUrn).returns(Future.value(trackUrns.toList))

        pubmeseClient.isrcsForTracks(session, trackUrns).returns(Future.value(Map.empty))

        richOkidokiClient.fetchTrackGeoblockings(session, trackUrns).returns(Future.value(Map.empty))

        richOkidokiClient.fetchTracksDomainLockings(session, trackUrns).returns(Future.value(Map.empty))

        richOkidokiClient.fetchTracksAudioMetadata(session, trackUrns).returns(Future.value(Map.empty))

        stitchClient.countsForTracksByUser(session, userUrn, trackUrns).returns(Future.value(Map.empty))

        trackmetadataClient.tracks(session, trackUrns).returns(Future.value(trackmetadataTracks))

        lieblingClient.userLikedTracks(session, trackUrns, sessionUser).returns(Future.value(Map.empty))

        mediaServiceUrlGenClient.waveformUrls(session, trackmetadataTracks.flatMap(_.uid)).returns(Future.value(Some(Map.empty)))

        val userUrnsFromLabelIds = trackmetadataTracks.flatMap(_.label_id).map(id => new Urn("soundcloud", "users", id.toString)).toSet
        richOkidokiClient.fetchUsersMap(session, userUrnsFromLabelIds).returns(Future.value(Map.empty))

        userQuotaClient.downloadsPerTrack(session, Set(userUrn)).returns(Future.value(Map.empty))

        val accessibilityChecks = Map(
          track1 -> true,
          track2 -> true,
          track3 -> true
        )
        trackAccessibilityService.areTracksAccessible(session, trackmetadataTracks).returns(Future.value(accessibilityChecks))
      }

      "returns the tracks" in new AllGoesWell {
        result ==== TracksResult(
          trackmetadataTracks,
          tracksOwner,
          Map.empty,
          Map.empty,
          Map.empty,
          Map.empty,
          Map.empty,
          Map.empty,
          Map.empty,
          Map.empty,
          Map.empty,
          accessibilityChecks
        )
      }
    }
  }

}
