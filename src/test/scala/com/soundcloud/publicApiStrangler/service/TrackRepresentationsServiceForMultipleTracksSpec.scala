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

class TrackRepresentationsServiceForMultipleTracksSpec extends UnitSpecification {

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

    val service = new TrackRepresentationsService(
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

    lazy val tracks: List[TrackRepresentationLike] = Await.result(service.tracks(session, userUrn, paginationParams))
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
          trackmetadataTrack(track1, userUrn, Some(1)),
          trackmetadataTrack(track2, userUrn, Some(2)),
          trackmetadataTrack(track3, userUrn, None))

        val tracksOwner = User(userUrn, "permalink", "username", "avatar_url", "permalink_url", None, None, 0, None, None, false, None, None)
        richOkidokiClient.fetchUserObjects(session, Set(userUrn)).returns(Future.value(List(tracksOwner)))

        trackmetadataClient.urnsByUser(session, userUrn).returns(Future.value(trackUrns.toList))

        pubmeseClient.isrcsForTracks(session, trackUrns).returns(Future.value(Map(
          track1 -> Isrc("isrc1"),
          track2 -> Isrc("isrc2"))))

        richOkidokiClient.fetchTrackGeoblockings(session, trackUrns).returns(Future.value(Map(
          track1 -> List("DE", "BR"),
          track2 -> List.empty)))

        val domainLockingsForFirstTrack = List(
          DomainLocking("domain1", Urn("soundcloud:domain-lockings:1"), track1),
          DomainLocking("domain2", Urn("soundcloud:domain-lockings:2"), track2))
        richOkidokiClient.fetchTracksDomainLockings(session, trackUrns).returns(Future.value(Map(
          track1 -> domainLockingsForFirstTrack,
          track2 -> List.empty)))

        val trackAudioMetadatas = List(
          TrackAudioMetadata("state1", Some("original_format"), Some(1)),
          TrackAudioMetadata("state2", None, None),
          TrackAudioMetadata("state3", None, None))
        richOkidokiClient.fetchTracksAudioMetadata(session, trackUrns).returns(Future.value(Map(
          track1 -> trackAudioMetadatas(0),
          track2 -> trackAudioMetadatas(1),
          track3 -> trackAudioMetadatas(2))))

        stitchClient.countsForTracksByUser(session, userUrn, trackUrns).returns(Future.value(Map(
          track1 -> StitchCounts(1, 2, 3, 4, 5),
          track2 -> StitchCounts(0, 0, 0, 0, 0))))

        trackmetadataClient.tracks(session, trackUrns).returns(Future.value(trackmetadataTracks))

        lieblingClient.userLikedTracks(session, trackUrns, sessionUser).returns(Future.value(Map(
          track1 -> true,
          track2 -> false)))

        val waveformUrls = List(
          Seq(WaveformUrl("label1_1", "json1_1", "png1_1"), WaveformUrl("label1_2", "json1_2", "png1_2")),
          Seq(WaveformUrl("label2", "json2", "png2")),
          Seq.empty)
        mediaServiceUrlGenClient.waveformUrls(session, trackmetadataTracks.flatMap(_.uid)).returns(Future.value(Some(Map(
          trackmetadataTracks(0).uid.get -> waveformUrls(0),
          trackmetadataTracks(1).uid.get -> waveformUrls(1),
          trackmetadataTracks(2).uid.get -> waveformUrls(2)))))

        val userUrnsFromLabelIds = Set(Urn("soundcloud:users:1"), Urn("soundcloud:users:2"))
        val userForLabelId = User(userUrnsFromLabelIds.head, "permalink1", "username1", "avatar_url1", "permalink_url1", None, None, 0, None, None, false, None, None)
        richOkidokiClient.fetchUsersMap(session, userUrnsFromLabelIds).returns(Future.value(Map(
          track1 -> userForLabelId)))

        userQuotaClient.downloadsPerTrack(session, Set(userUrn)).returns(Future.value(Map(
          track1 -> Some(10),
          track2 -> None)))

        trackAccessibilityService.areTracksAccessible(session, trackmetadataTracks).returns(Future.value(Map(
          track1 -> true,
          track2 -> true,
          track3 -> true)))
      }

      "returns the tracks" in new AllGoesWell {
        tracks.size ==== 3
        tracks(0) ==== new TrackRepresentationBuilder().build(
          Some(sessionUser),
          trackmetadataTracks(2),
          tracksOwner,
          None,
          StitchCounts(0, 0, 0, 0, 0),
          None,
          None,
          Seq.empty,
          trackAudioMetadatas(2),
          false,
          waveformUrls(2),
          None,
          None)

        tracks(1) ==== new TrackRepresentationBuilder().build(
          Some(sessionUser),
          trackmetadataTracks(1),
          tracksOwner,
          Some(Isrc("isrc2")),
          StitchCounts(0, 0, 0, 0, 0),
          None,
          Some(List.empty),
          Seq.empty,
          trackAudioMetadatas(1),
          false,
          waveformUrls(1),
          None,
          None)

        tracks(2) ==== new TrackRepresentationBuilder().build(
          Some(sessionUser),
          trackmetadataTracks(0),
          tracksOwner,
          Some(Isrc("isrc1")),
          StitchCounts(1, 2, 3, 4, 5),
          Some(userForLabelId),
          Some(List("DE", "BR")),
          domainLockingsForFirstTrack,
          trackAudioMetadatas(0),
          true,
          waveformUrls(0),
          None,
          Some(10))
      }
    }
  }
}
