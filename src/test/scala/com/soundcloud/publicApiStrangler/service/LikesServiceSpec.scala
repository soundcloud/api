package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.{UserSession, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentationsService,
  TrackRepresentationsServiceSpec,
  TracksCollection
}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._

class LikesServiceSpec extends UnitSpecification {

  trait Context extends TrackRepresentationsServiceSpec {

    val trackRepresentationsService = mock[TrackRepresentationsService]
    val lieblingClient = mock[LieblingClient]
    val trackPagination = mock[TrackPagination]

    val likesService = new LikesService(
      trackRepresentationsService,
      lieblingClient
    )

    val requestingUserUrn = Urn("soundcloud", "users", "112")
    val trackOwnerUrn = Urn("soundcloud", "users", "3000")
    val trackUrn = Urn("soundcloud", "tracks", "987")
    val session: UserSession = new UserSessionBuilder().setUser(requestingUserUrn).build()

    "#userTracksLikes" >> {
      "when all data is available" in new Context {
        val track = trackvisibilityTrack()
        setUpMocksForMultipleExistingTracks(track, session)
        when(lieblingClient.userTracksLikes(session, trackOwnerUrn)).thenReturn(Future.value(List(trackUrn)))

        val tracksCollection = Await.result(likesService.userTracksLikes(session, trackOwnerUrn, trackPagination))

        tracksCollection match {
          case rep =>
            rep must beAnInstanceOf[TracksCollection]
        }
      }
    }
  }
}
