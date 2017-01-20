package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.Urn
import com.soundcloud.jvmkit.module.util.{Bad, Error, Good, Result}
import com.soundcloud.publicApiStrangler.client.{DomainLocking, RichOkidokiClient, TrackAudioMetadata}
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.mediaservice.{MediaServiceUrlGenClient, WaveformUrl}
import com.soundcloud.publicApiStrangler.client.playlists.PlaylistsClient
import com.soundcloud.publicApiStrangler.client.pubmese.{Isrc, PubmeseClient}
import com.soundcloud.publicApiStrangler.client.quota.UserQuotaClient
import com.soundcloud.publicApiStrangler.client.stitch.{StitchClient, StitchCounts}
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.test.util.TrackMetadataTrackBuilder
import com.soundcloud.scalakit.test.UnitSpecification
import com.soundcloud.service.response.representation.{Geoblockings, User}
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

    def userUrn = Urn("soundcloud:users:9218371")
    def paginationParams: TrackPagination

    def trackmetadataTrack(urn: Urn, ownerUrn: Urn, labelId: Option[Int]) =
      TrackMetadataTrackBuilder(urn = urn, user_urn = ownerUrn, label_id = labelId).build

    def fetchUserObjectsResponse = Future.value(List(tracksOwner))
    def urnsByUserResponse = Future.value(trackUrns.toList)
    def isrcsForTracksResponse = Future.value(Map.empty[Urn, Isrc])
    def fetchTrackGeoblockingsResponse = Future.value(Map.empty[Urn, Geoblockings])
    def fetchTracksDomainLockingsResponse = Future.value(Map.empty[Urn, List[DomainLocking]])
    def fetchTracksAudioMetadataResponse = Future.value(Map.empty[Urn, TrackAudioMetadata])
    def countsForTracksByUserResponse = Future.value(Map.empty[Urn, StitchCounts])
    def tracksResponse = Future.value(trackmetadataTracks)
    def userLikedTracksResponse = Future.value(Map.empty[Urn, Boolean])
    def Response = Future.value(Map.empty)
    def waveformUrlsResponse = Future.value(Map.empty[String, Seq[WaveformUrl]])
    def fetchUsersMapResponse = Future.value(Map.empty[Urn, User])
    def downloadsPerTrackResponse = Future.value(Map.empty[Urn, Option[Int]])
    def areTracksAccessibleResponse = Future.value(accessibilityChecks)

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
    richOkidokiClient.fetchUserObjects(session, Set(userUrn)).returns(fetchUserObjectsResponse)
    trackmetadataClient.urnsByUser(session, userUrn).returns(urnsByUserResponse)

    pubmeseClient.isrcsForTracks(session, trackUrns).returns(isrcsForTracksResponse)
    richOkidokiClient.fetchTrackGeoblockings(session, trackUrns).returns(fetchTrackGeoblockingsResponse)
    richOkidokiClient.fetchTracksDomainLockings(session, trackUrns).returns(fetchTracksDomainLockingsResponse)
    stitchClient.countsForTracksByUser(session, userUrn, trackUrns).returns(countsForTracksByUserResponse)
    lieblingClient.userLikedTracks(session, trackUrns, sessionUser).returns(userLikedTracksResponse)
    userQuotaClient.downloadsPerTrack(session, Set(userUrn)).returns(downloadsPerTrackResponse)

    trackmetadataClient.tracks(session, trackUrns).returns(tracksResponse)
    richOkidokiClient.fetchTracksAudioMetadata(session, trackUrns).returns(fetchTracksAudioMetadataResponse)
    mediaServiceUrlGenClient.waveformUrls(session, trackmetadataTracks.flatMap(_.uid)).returns(waveformUrlsResponse)

    val userUrnsFromLabelIds = trackmetadataTracks.flatMap(_.label_id).map(id => new Urn("soundcloud", "users", id.toString)).toSet
    richOkidokiClient.fetchUsersMap(session, userUrnsFromLabelIds).returns(fetchUsersMapResponse)

    val accessibilityChecks = Map(
      track1 -> true,
      track2 -> true,
      track3 -> false
    )
    trackAccessibilityService.areTracksAccessible(session, trackmetadataTracks).returns(areTracksAccessibleResponse)

    def exception = new RuntimeException("nooo")
    def badFuture = Future.exception(exception)

    lazy val result: Result[TracksResult] = Await.result(repository.tracksByUser(session, userUrn, paginationParams).value)
  }


  "#tracksByUser" >> {
    "with no pagination params" >> {
      trait NoPaginationParams extends Context {
        override def paginationParams = new TrackPagination(None, None, false, None, None)

        val goodTracksResult = Good(TracksResult(
          trackmetadataTracks.tail, // track3 was removed for not being accessible
          tracksOwner, Map.empty, Map.empty, Map.empty, Map.empty, Map.empty, Map.empty, Map.empty, Map.empty, Map.empty))
      }

      "when loading the tracks' owner fails, it fails" in new NoPaginationParams {
        override def fetchUserObjectsResponse = badFuture
        result match {
          case Bad(Error(message, _)) => message ==== "Could not load the tracks' owner"
          case _ => failure
        }
      }

      "when loading the tracks' urns fails, it fails" in new NoPaginationParams {
        override def urnsByUserResponse = badFuture
        result match {
          case Bad(Error(message, _)) => message ==== "Could not load the tracks' urns"
          case _ => failure
        }
      }

      "when loading the tracks from trackmetadata fails, it fails" in new NoPaginationParams {
        override def tracksResponse = badFuture
        result match {
          case Bad(Error(message, _)) => message ==== "Could not load tracks from trackmetadata"
          case _ => failure
        }
      }

      "when loading the tracks' audios fails, it fails" in new NoPaginationParams {
        override def fetchTracksAudioMetadataResponse = badFuture
        result match {
          case Bad(Error(message, _)) => message ==== "Could not load the audio information"
          case _ => failure
        }
      }

      "when loading the tracks' waveforms fails, it fails" in new NoPaginationParams {
        override def waveformUrlsResponse = badFuture
        result match {
          case Bad(Error(message, _)) => message ==== "Could not load the tracks' waveforms"
          case _ => failure
        }
      }

      "it returns the tracks" >> {
        "when all goes well" in new NoPaginationParams {
          result ==== goodTracksResult
        }

        "when loading the isrcs fails" in new NoPaginationParams {
          override def isrcsForTracksResponse = badFuture
          result ==== goodTracksResult
        }

        "when loading the geoblockings fails" in new NoPaginationParams {
          override def isrcsForTracksResponse = badFuture
          result ==== goodTracksResult
        }

        "when loading the domainlockings fails" in new NoPaginationParams {
          override def isrcsForTracksResponse = badFuture
          result ==== goodTracksResult
        }

        "when loading the counts fails" in new NoPaginationParams {
          override def countsForTracksByUserResponse = badFuture
          result ==== goodTracksResult
        }

        "when loading the downloads per track fails" in new NoPaginationParams {
          override def downloadsPerTrackResponse = badFuture
          result ==== goodTracksResult
        }

        "when loading the user liked tracks fails" in new NoPaginationParams {
          override def userLikedTracksResponse = badFuture
          result ==== goodTracksResult
        }

        "when loading the user representations for the `label` field fails" in new NoPaginationParams {
          override def fetchUsersMapResponse = badFuture
          result ==== goodTracksResult
        }
      }

      "when loading the tracks availability fails it returns no tracks" in new NoPaginationParams {
        override def areTracksAccessibleResponse = badFuture
        result ==== Good(TracksResult(
          List.empty,
          tracksOwner, Map.empty, Map.empty, Map.empty, Map.empty, Map.empty, Map.empty, Map.empty, Map.empty, Map.empty))
      }
    }
  }
}
