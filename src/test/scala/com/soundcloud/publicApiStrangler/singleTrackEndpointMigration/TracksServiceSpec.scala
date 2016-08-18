package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.jvmkit.UserSessionBuilder
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

    val createdAt = new LocalDateTime(2016, 5, 19, 18, 3, 4)

    def trackmetadataTrack(
                            disabledAt: Option[LocalDateTime] = None,
                            isPublic: Boolean = true,
                            secretToken: String = "secr3t-Token"
                          ) =
      Track(
        urn = trackUrn,
        user_urn = Urn("soundcloud:users:112"),
        commentable = false,
        description = None,
        created_at = createdAt,
        disabled_at = disabledAt,
        downloadable = false,
        duration = 0,
        genre = None,
        last_modified = null,
        permalink = null,
        permalink_url = None,
        public = isPublic,
        secret_token = secretToken,
        user_tags = List.empty,
        machine_tags = List.empty,
        title = null,
        uid = None,
        api_streamable = None,
        streamable = false,
        reveal_comments = false,
        reveal_stats = false,
        label_name = None,
        license = null,
        embeddable = None,
        release_year = None,
        release_month = None,
        release_day = None,
        embeddableBy = EmbeddingPermission.None,
        releaseDate = None,
        artwork = Artwork(None),
        published_at = None)

    val session = anonymousSession
  }

  "Returns 200 for public tracks" in new Context {
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(trackmetadataTrack())))

    val expectedResponseString = "{\"kind\":\"track\",\"id\":987,\"user_id\":112,\"duration\":0,\"created_at\":\"2016-05-19T18:03:04.000\",\"commentable\":false,\"embeddable_by\":\"none\",\"tag_list\":\"\",\"artwork_url\":null}"
    val response = Await.result(tracksService.track(session, trackUrn, None))

    response.status ==== Status.Ok
    response.contentString ==== expectedResponseString
  }

  "Returns a response with the right headers" in new Context {
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(trackmetadataTrack())))

    val response = Await.result(tracksService.track(session, trackUrn, None))

    response.headerMap.get("Content-Length") must beSome("167")
    response.headerMap.get("Content-Type") must beSome("application/json; charset=utf-8")
  }

  "Returns 404 for non existing tracks" in new Context {
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.None)

    val response = Await.result(tracksService.track(session, trackUrn, None))
    response.status ==== Status.NotFound
    response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
  }

  "Returns 404 when track is disabled" in new Context {
    val disabledAt = Some(LocalDateTime.now())
    val track = trackmetadataTrack(disabledAt)
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(track)))

    val response = Await.result(tracksService.track(session, trackUrn, None))
    response.status ==== Status.NotFound
    response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
  }

  "Returns 404 when track is not public" in new Context {
    val track = trackmetadataTrack(isPublic = false)
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(track)))

    val response = Await.result(tracksService.track(session, trackUrn, None))
    response.status ==== Status.NotFound
    response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
  }

  "Returns 200 for private tracks if the owner is requesting" in new Context {
    val ownerUrn = Urn("soundcloud:users:112")
    val ownerSession = new UserSessionBuilder().setUser(ownerUrn).build
    when(trackmetadataClient.track(ownerSession, trackUrn)).thenReturn(Future.value(Some(trackmetadataTrack())))

    val response = Await.result(tracksService.track(ownerSession, trackUrn, None))
    response.status ==== Status.Ok
  }

  "Returns 404 for private tracks if there is an incorrect secret token" in new Context {
    val wrongSecretToken = "secr3tTokenWRONG"
    val track = trackmetadataTrack(isPublic = false)
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(track)))

    val response = Await.result(tracksService.track(session, trackUrn, Some(wrongSecretToken)))
    response.status ==== Status.NotFound
    response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
  }

  "Returns 200 for private tracks if there is a correct secret token" in new Context {
    val correctSecretToken = "aSecre_t"
    val track = trackmetadataTrack(isPublic = false, secretToken = correctSecretToken)
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(track)))

    val response = Await.result(tracksService.track(session, trackUrn, Some(correctSecretToken)))
    response.status ==== Status.Ok
  }
}
