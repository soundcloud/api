package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.jvmkit.UserSessionBuilder
import com.soundcloud.jvmkit.policies.{ContentAuthorization, ContentPolicy, MonetizationModel, Reason}
import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.{Artwork, EmbeddingPermission, Track, TrackmetadataClient}
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}
import org.joda.time.LocalDateTime
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
    val contentAuth = new ContentAuthorization(trackUrn, ContentPolicy.ALLOW, Reason.DEFAULT, MonetizationModel.AD_SUPPORTED)

    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(trackmetadataTrack)))

    val expectedResponseString = "{\"kind\":\"track\",\"id\":987,\"user_id\":112}"
    val response = Await.result(tracksService.track(session, trackUrn, None))

    response.status ==== Status.Ok
    response.contentString ==== expectedResponseString
  }

  "Returns a response with the right headers" in new Context {
    val isPublic = true
    val trackmetadataTrack = Track(trackUrn, Urn("soundcloud:users:112"), false, None, null, None, false, 0, None, null, null, None,
      isPublic, null, List.empty, List.empty, null, None, None, false, false, false, None, null, None, None, None, None,
      EmbeddingPermission.None, None, Artwork(None), None)
    val contentAuth = new ContentAuthorization(trackUrn, ContentPolicy.ALLOW, Reason.DEFAULT, MonetizationModel.AD_SUPPORTED)

    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(trackmetadataTrack)))

    val expectedResponseString = "{\"kind\":\"track\",\"id\":987,\"user_id\":112}"
    val response = Await.result(tracksService.track(session, trackUrn, None))

    response.headerMap.get("Content-Length") ==== Some("39")
    response.headerMap.get("Content-Type") ==== Some("application/json")
  }

  "Returns 404 for non existing tracks" in new Context {
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.None)

    val response = Await.result(tracksService.track(session, trackUrn, None))
    response.status ==== Status.NotFound
    response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
  }

  "Returns 404 when track is disabled" in new Context {
    val isPublic = true
    val disabledAt = Some(LocalDateTime.now())
    val trackmetadataTrack = Track(trackUrn, Urn("soundcloud:users:112"), false, None, null, disabledAt, false, 0, None, null, null, None,
      isPublic, null, List.empty, List.empty, null, None, None, false, false, false, None, null, None, None, None, None,
      EmbeddingPermission.None, None, Artwork(None), None)

    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(trackmetadataTrack)))

    val response = Await.result(tracksService.track(session, trackUrn, None))
    response.status ==== Status.NotFound
    response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
  }

  "Returns 404 when track is not public" in new Context {
    val isPublic = false
    val trackmetadataTrack = Track(trackUrn, Urn("soundcloud:users:112"), false, None, null, None, false, 0, None, null, null, None,
      isPublic, null, List.empty, List.empty, null, None, None, false, false, false, None, null, None, None, None, None,
      EmbeddingPermission.None, None, Artwork(None), None)

    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(trackmetadataTrack)))

    val response = Await.result(tracksService.track(session, trackUrn, None))
    response.status ==== Status.NotFound
    response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
  }

  "Returns 200 for private tracks if the owner is requesting" in new Context {
    val ownerUrn = Urn("soundcloud:users:112")
    val ownerSession = new UserSessionBuilder().setUser(ownerUrn).build
    val isPublic = false
    val trackmetadataTrack = Track(trackUrn, Urn("soundcloud:users:112"), false, None, null, None, false, 0, None, null, null, None,
      isPublic, null, List.empty, List.empty, null, None, None, false, false, false, None, null, None, None, None, None,
      EmbeddingPermission.None, None, Artwork(None), None)
    val contentAuth = new ContentAuthorization(trackUrn, ContentPolicy.ALLOW, Reason.DEFAULT, MonetizationModel.AD_SUPPORTED)

    when(trackmetadataClient.track(ownerSession, trackUrn)).thenReturn(Future.value(Some(trackmetadataTrack)))

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

    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(trackmetadataTrack)))

    val response = Await.result(tracksService.track(session, trackUrn, Some(wrongSecretToken)))
    response.status ==== Status.NotFound
    response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
  }

  "Returns 200 for private tracks if there is a correct secret token" in new Context {
    val isPublic = false
    val secretToken = "secr3t-Token"
    val trackmetadataTrack = Track(trackUrn, Urn("soundcloud:users:112"), false, None, null, None, false, 0, None, null, null, None,
      isPublic, secretToken, List.empty, List.empty, null, None, None, false, false, false, None, null, None, None, None, None,
      EmbeddingPermission.None, None, Artwork(None), None)
    val contentAuth = new ContentAuthorization(trackUrn, ContentPolicy.ALLOW, Reason.DEFAULT, MonetizationModel.AD_SUPPORTED)

    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(trackmetadataTrack)))

    val response = Await.result(tracksService.track(session, trackUrn, Some(secretToken)))
    response.status ==== Status.Ok
  }
}
