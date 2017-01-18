package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.client.RichOkidokiClient
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.mediaservice.MediaServiceUrlGenClient
import com.soundcloud.publicApiStrangler.client.playlists.PlaylistsClient
import com.soundcloud.publicApiStrangler.client.pubmese.PubmeseClient
import com.soundcloud.publicApiStrangler.client.quota.UserQuotaClient
import com.soundcloud.publicApiStrangler.client.stitch.StitchClient
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.controller.PublicApiPaginationParams
import com.soundcloud.publicApiStrangler.test.util.TrackMetadataTrackBuilder
import com.soundcloud.scalakit.test.UnitSpecification
import com.soundcloud.service.response.representation.User
import com.twitter.util.{Await, Future}

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

    def trackmetadataTrack(urn: Urn, ownerUrn: Urn, labelId: Option[Int]) =
      TrackMetadataTrackBuilder(urn = urn, user_urn = ownerUrn, label_id = labelId).build

    lazy val result: TracksResult = Await.result(repository.tracksByUser(session, userUrn, paginationParams))
  }


  "#tracksByUser" >> {
    "when all goes well" >> {
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
          track3 -> false
        )
        trackAccessibilityService.areTracksAccessible(session, trackmetadataTracks).returns(Future.value(accessibilityChecks))
      }

      "returns the tracks" in new AllGoesWell {
        result ==== TracksResult(
          trackmetadataTracks.tail, // track3 was removed for not being accessible
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
