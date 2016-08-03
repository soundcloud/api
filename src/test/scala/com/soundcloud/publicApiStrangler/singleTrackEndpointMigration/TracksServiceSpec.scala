package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.{Artwork, EmbeddingPermission, Track, TrackmetadataClient}
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when

class TracksServiceSpec extends UnitSpecification {

  trait Context extends Scope {
    val trackmetadataClient = mock[TrackmetadataClient]
    val tracksService = new TracksService(trackmetadataClient)
    val trackUrn = Urn("soundcloud:tracks:999")

    val session = anonymousSession
  }

  "Returns None when trackmetadata doesn't exist" in new Context {
    when(trackmetadataClient.track(session, trackUrn, None)).thenReturn(Future.None)
    Await.result(tracksService.track(session, trackUrn)) must beNone
  }

  "Returns a track representation when trackmetadata exists" in new Context {
    val expectedRepresentation = new SingleTrackPublicApiRepresentation("track", 999L, 123L)
    val userUrn = Urn("soundcloud:tracks:123")
    val trackmetadataTrack = Track(trackUrn, userUrn, false, None, null, None, false, 0, None, null, null, None,
      false, null, List.empty, List.empty, null, None, None, false, false, false, None, null, None, None, None, None,
      EmbeddingPermission.None, None, Artwork(None), None)

    when(trackmetadataClient.track(session, trackUrn, None)).thenReturn(Future.value(Some(trackmetadataTrack)))
    Await.result(tracksService.track(session, trackUrn)) must beSome(expectedRepresentation)
  }
}
