package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.jvmkit.UserSessionBuilder
import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.{Artwork, EmbeddingPermission, Track, TrackmetadataClient}
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._

class TracksServiceSpec extends UnitSpecification {

  trait Context extends Scope {
    val trackmetadataClient = mock[TrackmetadataClient]
    val tracksService = new TracksService(trackmetadataClient)
    val trackUrn = Urn("soundcloud:tracks:987")

    val session = anonymousSession
  }

  "Returns 200 for public tracks" in new Context {
    val isPublic = true
    val trackmetadataTrack = Track(trackUrn, Urn("soundcloud:users:112"), false, None, null, None, false, 0, None, null, null, None,
      isPublic, null, List.empty, List.empty, null, None, None, false, false, false, None, null, None, None, None, None,
      EmbeddingPermission.None, None, Artwork(None), None)

    when(trackmetadataClient.track(session, trackUrn, None)).thenReturn(Future.value(Some(trackmetadataTrack)))

    val expectedResponseString = "{\"kind\":\"track\",\"id\":987,\"user_id\":112}"
    val response = Await.result(tracksService.track(session, trackUrn, None))

    response.status ==== Status.Ok
    response.contentString ==== expectedResponseString
  }

  "Returns 404 for non existing tracks" in new Context {
    when(trackmetadataClient.track(session, trackUrn, None)).thenReturn(Future.None)

    val response = Await.result(tracksService.track(session, trackUrn, None))
    response.status ==== Status.NotFound
  }

  "Returns 404 when track is not public" in new Context {
    val isPublic = false
    val trackmetadataTrack = Track(trackUrn, Urn("soundcloud:users:112"), false, None, null, None, false, 0, None, null, null, None,
      isPublic, null, List.empty, List.empty, null, None, None, false, false, false, None, null, None, None, None, None,
      EmbeddingPermission.None, None, Artwork(None), None)

    when(trackmetadataClient.track(session, trackUrn, None)).thenReturn(Future.value(Some(trackmetadataTrack)))

    val response = Await.result(tracksService.track(session, trackUrn, None))
    response.status ==== Status.NotFound
  }

  "Returns 200 for private tracks if the owner is requesting" in new Context {
    val ownerUrn = Urn("soundcloud:users:112")
    val ownerSession = new UserSessionBuilder().setUser(ownerUrn).build
    val isPublic = false
    val trackmetadataTrack = Track(trackUrn, Urn("soundcloud:users:112"), false, None, null, None, false, 0, None, null, null, None,
      isPublic, null, List.empty, List.empty, null, None, None, false, false, false, None, null, None, None, None, None,
      EmbeddingPermission.None, None, Artwork(None), None)

    when(trackmetadataClient.track(ownerSession, trackUrn, None)).thenReturn(Future.value(Some(trackmetadataTrack)))

    val response = Await.result(tracksService.track(ownerSession, trackUrn, None))
    response.status ==== Status.Ok
  }

  "Returns 404 for private tracks if there is an incorrect secret token" in new Context {
    val isPublic = false
    val secretToken = "secr3t-Token"
    val wrongSecretToken = "secr3tTokenWRONG"
    val trackmetadataTrack = Track(trackUrn, Urn("soundcloud:users:112"), false, None, null, None, false, 0, None, null, null, None,
      isPublic, secretToken, List.empty, List.empty, null, None, None, false, false, false, None, null, None, None, None, None,
      EmbeddingPermission.None, None, Artwork(None), None)

    when(trackmetadataClient.track(session, trackUrn, None)).thenReturn(Future.value(Some(trackmetadataTrack)))

    val response = Await.result(tracksService.track(session, trackUrn, Some(wrongSecretToken)))
    response.status ==== Status.NotFound
  }

  "Returns 200 for private tracks if there is a correct secret token" in new Context {
    val isPublic = false
    val secretToken = "secr3t-Token"
    val trackmetadataTrack = Track(trackUrn, Urn("soundcloud:users:112"), false, None, null, None, false, 0, None, null, null, None,
      isPublic, secretToken, List.empty, List.empty, null, None, None, false, false, false, None, null, None, None, None, None,
      EmbeddingPermission.None, None, Artwork(None), None)

    when(trackmetadataClient.track(session, trackUrn, None)).thenReturn(Future.value(Some(trackmetadataTrack)))

    val response = Await.result(tracksService.track(session, trackUrn, Some(secretToken)))
    response.status ==== Status.Ok
  }
}
