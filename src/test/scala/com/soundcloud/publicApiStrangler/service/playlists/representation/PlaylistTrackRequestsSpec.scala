package com.soundcloud.publicApiStrangler.service.playlists.representation

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.ParamMap
import proto.soundcloud.playlists.api.{PlaylistPagination, TrackRequest => ProtoTrackRequest}

class PlaylistTrackRequestsSpec extends UnitSpecification {
  trait Context extends Scope {
    val trackUrn1 = Urn("soundcloud", "tracks", "1")
    val trackSecret1 = Some("S3cret")
    val trackUrn2 = Urn("soundcloud", "tracks", "2")

    val currentPagination = OffsetBasedPagination(
      baseUrl = "http://api.soundcloud.com",
      path = "/playlists",
      extraParams = ParamMap(),
      offset = None,
      limit = 2
    )

    val nextPagination = PlaylistPagination(limit = 2, cursor = Some("2"))

    val protoTrackRequests =
      List(
        ProtoTrackRequest(urn = trackUrn1.toString, secretToken = trackSecret1),
        ProtoTrackRequest(urn = trackUrn2.toString, secretToken = None)
      )

    val expectedTrackRequests = List(
      TrackRequest(urn = trackUrn1, secretToken = trackSecret1),
      TrackRequest(urn = trackUrn2, secretToken = None)
    )
  }
  "build" >> {
    "can build PlaylistTrackRequests with pagination" in new Context {
      val playlistTrackRequests =
        PlaylistTrackRequests.build(
          trackRequests = protoTrackRequests,
          currentPagination = Some(currentPagination),
          nextPagination = Some(nextPagination)
        )

      val expectedPagination = currentPagination.copy(offset = Some(2))

      playlistTrackRequests.requests ==== expectedTrackRequests
      playlistTrackRequests.pagination ==== Some(expectedPagination)
    }

    "omits pagination when no next pagination given" in new Context {
      val playlistTrackRequests =
        PlaylistTrackRequests.build(
          trackRequests = protoTrackRequests,
          currentPagination = Some(currentPagination),
          nextPagination = None
        )

      playlistTrackRequests.requests ==== expectedTrackRequests
      playlistTrackRequests.pagination must beEmpty
    }

    "omits pagination when no next or current pagination given" in new Context {
      val playlistTrackRequests =
        PlaylistTrackRequests.build(
          trackRequests = protoTrackRequests,
          currentPagination = None,
          nextPagination = None
        )

      playlistTrackRequests.requests ==== expectedTrackRequests
      playlistTrackRequests.pagination must beEmpty
    }
  }
}
