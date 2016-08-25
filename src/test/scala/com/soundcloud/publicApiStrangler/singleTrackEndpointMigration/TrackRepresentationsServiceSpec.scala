package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.jvmkit.UserSessionBuilder
import com.soundcloud.publicApiStrangler.client.pubmese.{Isrc, PubmeseClient}
import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.{Artwork, EmbeddingPermission, Track, TrackmetadataClient}
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.json.Json
import com.soundcloud.scalakit.test.UnitSpecification
import com.soundcloud.service.client.OkidokiClient
import com.soundcloud.service.response.representation.User
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}
import org.joda.time.LocalDateTime
import org.mockito.Mockito._

class TrackRepresentationsServiceSpec extends UnitSpecification {

  trait Context extends Scope {
    val trackmetadataClient = mock[TrackmetadataClient]
    val okidokiClient = mock[OkidokiClient]
    val pubmeseClient = mock[PubmeseClient]

    val tracksService = new TrackRepresentationsService(
      trackmetadataClient,
      okidokiClient,
      pubmeseClient
    )

    val userUrn = Urn("soundcloud:users:112")

    def user =
      User(
        urn = userUrn,
        permalink = "giraffe",
        username = "Dr. G. Raffe",
        avatar_url = "http://example.com/giraffe.jpg",
        permalink_url = "http://soundcloud.com/denis",
        city = None,
        country = None,
        tracks_count = 1,
        followers_count = Some(20000),
        followings_count = Some(20),
        verified = false,
        description = Some("I am a nice person"))

    val trackUrn = Urn("soundcloud:tracks:987")
    val createdAt = new LocalDateTime(2016, 5, 19, 18, 3, 4)
    val lastModified = new LocalDateTime(2016, 5, 20, 18, 3, 4)

    def trackmetadataTrack(
                            disabledAt: Option[LocalDateTime] = None,
                            isPublic: Boolean = true,
                            secretToken: String = "secr3t-Token"
                          ) =
      Track(
        urn = trackUrn,
        user_urn = userUrn,
        commentable = false,
        description = None,
        created_at = createdAt,
        disabled_at = disabledAt,
        downloadable = false,
        duration = 0,
        genre = None,
        last_modified = lastModified,
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

    def isrc(wrapped: String = "US-S1Z-99-00001"): Option[Isrc] =
      Some(Isrc(wrapped))

    val session = anonymousSession
  }

  "Returns 200 for public tracks" in new Context {
    val track = trackmetadataTrack()
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(track)))
    when(okidokiClient.fetchUserObjects(session, Set(userUrn))).thenReturn(Future.value(List(user)))
    when(pubmeseClient.isrcForTrack(session, trackUrn)).thenReturn(Future.value(isrc()))

    val expectedTrackRepresentation = TrackRepresentation(track, user, isrc())
    val expectedResponseString = Json.stringify(expectedTrackRepresentation)

    val response = Await.result(tracksService.track(session, trackUrn, None, None))

    response.status ==== Status.Ok
    response.contentString ==== expectedResponseString
  }

  "Wraps track data in jsonp if `callback` param is defined" in new Context {
    val track = trackmetadataTrack()
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(track)))
    when(okidokiClient.fetchUserObjects(session, Set(userUrn))).thenReturn(Future.value(List(user)))
    when(pubmeseClient.isrcForTrack(session, trackUrn)).thenReturn(Future.value(isrc()))

    val expectedTrackRepresentation = TrackRepresentation(track, user, isrc())
    val expectedResponseString = Json.stringify(expectedTrackRepresentation)

    val response = Await.result(tracksService.track(session, trackUrn, None, Some("js_callback_fn")))

    response.status ==== Status.Ok
    response.contentString ==== s"/**/js_callback_fn($expectedResponseString);"
  }

  "Returns a response with the right headers" in new Context {
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(trackmetadataTrack())))
    when(okidokiClient.fetchUserObjects(session, Set(userUrn))).thenReturn(Future.value(List(user)))
    when(pubmeseClient.isrcForTrack(session, trackUrn)).thenReturn(Future.value(isrc()))

    val response = Await.result(tracksService.track(session, trackUrn, None, None))

    response.headerMap.get("Content-Length") must beSome("796")
    response.headerMap.get("Content-Type") must beSome("application/json; charset=utf-8")
  }

  "Returns 404 for non existing tracks" in new Context {
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.None)
    when(pubmeseClient.isrcForTrack(session, trackUrn)).thenReturn(Future.value(isrc()))

    val response = Await.result(tracksService.track(session, trackUrn, None, None))
    response.status ==== Status.NotFound
    response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
  }

  "Wraps error message in jsonp if `callback` param is defined" in new Context {
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.None)
    when(pubmeseClient.isrcForTrack(session, trackUrn)).thenReturn(Future.value(isrc()))

    val response = Await.result(tracksService.track(session, trackUrn, None, Some("js_callback_fn")))
    response.status ==== Status.NotFound
    response.contentString ==== """/**/js_callback_fn({"errors":[{"error_message":"404 - Not Found"}]});"""
  }

  "Returns 404 response with the right headers" in new Context {
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.None)
    when(pubmeseClient.isrcForTrack(session, trackUrn)).thenReturn(Future.value(isrc()))

    val response = Await.result(tracksService.track(session, trackUrn, None, None))

    response.headerMap.get("Content-Length") must beSome("48")
    response.headerMap.get("Content-Type") must beSome("application/json; charset=utf-8")
  }

  "Returns 404 when track is disabled" in new Context {
    val disabledAt = Some(LocalDateTime.now())
    val track = trackmetadataTrack(disabledAt)
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(track)))
    when(pubmeseClient.isrcForTrack(session, trackUrn)).thenReturn(Future.value(isrc()))

    val response = Await.result(tracksService.track(session, trackUrn, None, None))
    response.status ==== Status.NotFound
    response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
  }

  "Returns 404 when track is not public" in new Context {
    val track = trackmetadataTrack(isPublic = false)
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(track)))
    when(pubmeseClient.isrcForTrack(session, trackUrn)).thenReturn(Future.value(isrc()))

    val response = Await.result(tracksService.track(session, trackUrn, None, None))
    response.status ==== Status.NotFound
    response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
  }

  "Returns 200 for private tracks if the owner is requesting" in new Context {
    val ownerSession = new UserSessionBuilder().setUser(userUrn).build
    when(trackmetadataClient.track(ownerSession, trackUrn)).thenReturn(Future.value(Some(trackmetadataTrack())))
    when(okidokiClient.fetchUserObjects(ownerSession, Set(userUrn))).thenReturn(Future.value(List(user)))
    when(pubmeseClient.isrcForTrack(ownerSession, trackUrn)).thenReturn(Future.value(isrc()))

    val response = Await.result(tracksService.track(ownerSession, trackUrn, None, None))
    response.status ==== Status.Ok
  }

  "Returns 404 for private tracks if there is an incorrect secret token" in new Context {
    val wrongSecretToken = "secr3tTokenWRONG"
    val track = trackmetadataTrack(isPublic = false)
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(track)))
    when(pubmeseClient.isrcForTrack(session, trackUrn)).thenReturn(Future.value(isrc()))

    val response = Await.result(tracksService.track(session, trackUrn, Some(wrongSecretToken), None))
    response.status ==== Status.NotFound
    response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
  }

  "Returns 200 for private tracks if there is a correct secret token" in new Context {
    val correctSecretToken = "aSecre_t"
    val track = trackmetadataTrack(isPublic = false, secretToken = correctSecretToken)
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(track)))
    when(okidokiClient.fetchUserObjects(session, Set(userUrn))).thenReturn(Future.value(List(user)))
    when(pubmeseClient.isrcForTrack(session, trackUrn)).thenReturn(Future.value(isrc()))

    val response = Await.result(tracksService.track(session, trackUrn, Some(correctSecretToken), None))
    response.status ==== Status.Ok
  }

  "Returns 404 for public tracks if user does not exist" in new Context {
    val publicTrack = trackmetadataTrack()
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(publicTrack)))
    when(okidokiClient.fetchUserObjects(session, Set(userUrn))).thenReturn(Future.value(List.empty))
    when(pubmeseClient.isrcForTrack(session, trackUrn)).thenReturn(Future.value(isrc()))

    val response = Await.result(tracksService.track(session, trackUrn, None, None))
    response.status ==== Status.NotFound
  }

  "Returns 503 for public tracks if user can not be fetched" in new Context {
    val publicTrack = trackmetadataTrack()
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(publicTrack)))
    when(okidokiClient.fetchUserObjects(session, Set(userUrn))).thenReturn(Future.exception(new Exception("asd")))
    when(pubmeseClient.isrcForTrack(session, trackUrn)).thenReturn(Future.value(isrc()))

    val response = Await.result(tracksService.track(session, trackUrn, None, None))
    response.status ==== Status.ServiceUnavailable
    response.contentString ==== """{"errors":[{"error_message":"503 - Service Unavailable"}]}"""
    response.headerMap.get("Content-Length") must beSome("58")
  }

  "Returns empty ISRC when Pubmese is failing" in new Context {
    val track = trackmetadataTrack()
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(track)))
    when(okidokiClient.fetchUserObjects(session, Set(userUrn))).thenReturn(Future.value(List(user)))
    when(pubmeseClient.isrcForTrack(session, trackUrn)).thenReturn(Future.exception(new RuntimeException("bewm! hahahaaa")))

    val expectedTrackRepresentation = TrackRepresentation(track, user, None)
    val expectedResponseString = Json.stringify(expectedTrackRepresentation)

    val response = Await.result(tracksService.track(session, trackUrn, None, None))

    response.status ==== Status.Ok
    response.contentString ==== expectedResponseString
  }
}
