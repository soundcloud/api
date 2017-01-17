package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.TrackRepresentationsService
import com.soundcloud.publicApiStrangler.client.RichOkidokiClient
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.mediaservice.MediaServiceUrlGenClient
import com.soundcloud.publicApiStrangler.client.playlists.PlaylistsClient
import com.soundcloud.publicApiStrangler.client.pubmese.PubmeseClient
import com.soundcloud.publicApiStrangler.client.quota.UserQuotaClient
import com.soundcloud.publicApiStrangler.client.stitch.StitchClient
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.controller.PublicApiPaginationParams
import com.soundcloud.scalakit.test.UnitSpecification

class TrackRepresentationsServiceForMultipleTracksSpec extends UnitSpecification {

  trait Context extends Scope {
    val trackmetadataClient = mock[TrackmetadataClient]
    val okidokiClient = mock[RichOkidokiClient]
    val pubmeseClient = mock[PubmeseClient]
    val stitchClient = mock[StitchClient]
    val lieblingClient = mock[LieblingClient]
    val mediaUrlGenClient = mock[MediaServiceUrlGenClient]
    val userQuotaClient = mock[UserQuotaClient]
    val playlistsClient = mock[PlaylistsClient]
    val trackAccessibilityService = mock[TrackAccessibilityService]
    val trackRepresentationBuilder = mock[TrackRepresentationBuilder]

    val service = new TrackRepresentationsService(
      trackmetadataClient,
      okidokiClient,
      pubmeseClient,
      stitchClient,
      lieblingClient,
      mediaUrlGenClient,
      userQuotaClient,
      trackAccessibilityService,
      trackRepresentationBuilder)

    lazy val session = loggedInSession(Urn("soundcloud:users:2398471"))

    def userUrn: Urn
    def paginationParams: PublicApiPaginationParams

    def tracks = service.tracks(session, userUrn, paginationParams)
  }

  "#tracks" >> {
    "when it all goes well" >> {
      trait AllGoesWell extends Context {

        override def userUrn = Urn("soundcloud:users:1231")
      }

      "returns the tracks" in new AllGoesWell {

      }
    }
  }
}
