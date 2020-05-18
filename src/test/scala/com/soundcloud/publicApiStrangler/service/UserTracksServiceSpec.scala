package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.{UserSession, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentationsService,
  TrackRepresentationsServiceSpec,
  TracksCollection
}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.Await

class UserTracksServiceSpec extends UnitSpecification {

  trait Context extends TrackRepresentationsServiceSpec {

    val trackVisibilityService = mock[TrackVisibilityService]
    val trackRepresentationsService = mock[TrackRepresentationsService]
    val trackmetadataClient = mock[TrackmetadataClient]
    val trackPagination = mock[TrackPagination]

    val userTracksService = new UserTracksService(
      trackVisibilityService,
      trackRepresentationsService,
      trackmetadataClient
    )

    val requestingUserUrn = Urn("soundcloud", "users", "112")
    val trackOwnerUrn = Urn("soundcloud", "users", "3000")
    val trackUrn = Urn("soundcloud", "tracks", "987")
    val session: UserSession = new UserSessionBuilder().setUser(requestingUserUrn).build()

    "#userTracks" >> {
      "when all data is available" in new Context {
        val track = trackvisibilityTrack()
        setUpMocksForMultipleExistingTracks(track, session)

        val tracksCollection = Await.result(userTracksService.userTracks(session, trackOwnerUrn, trackPagination))

        tracksCollection match {
          case rep =>
            rep must beAnInstanceOf[TracksCollection]
        }
      }
    }
  }
}
