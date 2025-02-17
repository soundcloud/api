package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.apipublic.Routing
import com.soundcloud.apipublic.client.TimelineJsonClient
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.TimelineService
import com.soundcloud.apipublic.service.pagination.CursorBasedPagination
import com.soundcloud.apipublic.service.playlists.PlaylistBuilder
import com.soundcloud.apipublic.service.timeline.{PlaylistTimelineItem, Timeline, TimelineMeta, TrackTimelineItem}
import com.soundcloud.apipublic.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.Request
import com.twitter.util.Future
import org.joda.time.DateTime
import org.mockito.Mockito._
import play.api.libs.json.Json

class TimelineHandlerSpec extends UnitSpecification {

  trait Context extends HandlerSpecificationScope with TimeLineHandlerTestData {
    val timelineClient = mock[TimelineJsonClient]
    val timelineService = mock[TimelineService]
    val session = loggedInSession(usrUrn)

    val handler = new TimelineHandler(
      new FakeUserAuthentication(session),
      timelineService,
      baseUrl
    )

    val access = AccessParams.defaultAccess

    override def routingDefinitions = Routing.forTimelineHandler(handler)
  }

  "render track stream" >> {
    trait SuccessfulCase extends Context {
      val mockTimelineItems = List(
        new TrackTimelineItem(createdAt = new DateTime().toString, "track", mockTrackRepresentation)
      )
      val mockTimelineMeta =
        TimelineMeta(Some("00000172-9b87-0a50-ffff-ffff8eec7ee8"), Some("00000172-9b87-0a50-ffff-ffff8eec7ee8"))
    }

    trait FailureCase extends Context {
      val mockTimelineItems = List.empty
      val mockTimelineMeta = TimelineMeta(None, None)
    }

    "returns successful response with valid request" in new SuccessfulCase {
      val queryParams = "?limit=10"
      Seq(
        "/me/activities/tracks"
      ).foreach { endpoint =>
        val path = s"$endpoint$queryParams"

        val mockRequest = Request(path)
        val pagination =
          CursorBasedPagination.build("http://api.soundcloud.com", mockRequest, Seq("linked_partitioning"))

        val mockTimelineResponse = Timeline(mockTimelineItems, mockTimelineMeta, pagination)
        when(timelineService.fetchTimelineTracksForUser(session, access, None, false, 10, pagination))
          .thenReturn(Future.value(mockTimelineResponse))

        val result = get(path)

        result.statusCode === 200
        result.contentString = mockTimelineResponse.getRepresentation()
      }
    }

    "return Timeline with empty tracks if no track events found" in new FailureCase {
      val queryParams = "?limit=10"

      Seq(
        "/me/activities/tracks"
      ).foreach { endpoint =>
        val path = s"$endpoint$queryParams"

        val mockRequest = Request(path)
        val pagination =
          CursorBasedPagination.build("http://api.soundcloud.com", mockRequest, Seq("linked_partitioning"))

        val mockTimelineResponse = Timeline(mockTimelineItems, mockTimelineMeta, pagination)
        when(timelineService.fetchTimelineTracksForUser(session, access, None, false, 10, pagination))
          .thenReturn(Future.value(mockTimelineResponse))

        val result = get(path)
        result.statusCode === 200
        result.contentString === mockTimelineResponse.getRepresentation()
      }
    }

    "returns 404 if not Timeline returned from service" in new Context {
      val queryParams = "?limit=10"

      Seq(
        "/me/activities/tracks"
      ).foreach { endpoint =>
        val path = s"$endpoint$queryParams"

        val mockRequest = Request(path)
        val pagination =
          CursorBasedPagination.build("http://api.soundcloud.com", mockRequest, Seq("linked_partitioning"))

        when(timelineService.fetchTimelineTracksForUser(session, access, None, false, 10, pagination))
          .thenReturn(Future.value(null))

        val result = get(path)
        result.statusCode === 404
      }
    }
  }

  "get followings tracks" >> {
    trait SuccessfulCase extends Context {
      val mockTimelineResponse = List(mockTrackRepresentation)
    }

    trait FailureCase extends Context {
      val emptyTimelineResponse = List.empty
      val noTimelineResponse = null
    }

    "successfully returns followings tracks" in new SuccessfulCase {
      val queryParams = "?limit=10"
      val path = s"/me/followings/tracks$queryParams"

      when(timelineService.fetchFollowingTracksForUser(session, access, None, false, 10))
        .thenReturn(Future.value(mockTimelineResponse))

      val result = get(path)
      result.statusCode === 200
      result.contentString === Json.stringify(Json.toJson(List(mockTrackRepresentation)))
    }

    "returns an empty array if now followings tracks found" in new FailureCase {
      val queryParams = "?limit=10"
      val path = s"/me/followings/tracks$queryParams"

      when(timelineService.fetchFollowingTracksForUser(session, access, None, false, 10))
        .thenReturn(Future.value(emptyTimelineResponse))

      val result = get(path)
      result.statusCode === 200
      result.contentString === "[]"
    }

    "returns a 404 if timeline service doesn't return tracks" in new FailureCase {
      val queryParams = "?limit=10"
      val path = s"/me/followings/tracks$queryParams"

      when(timelineService.fetchFollowingTracksForUser(session, access, None, false, 10))
        .thenReturn(Future.value(noTimelineResponse))

      val result = get(path)
      result.statusCode === 404
    }
  }

  "get all stream items" >> {
    trait SuccessCase extends Context {
      val mockPlaylistRepresentation = new PlaylistBuilder().build
      val mockTimelineItems = List(
        new TrackTimelineItem(createdAt = new DateTime().toString, "track", mockTrackRepresentation),
        new PlaylistTimelineItem(createdAt = new DateTime().toString(), "playlist", mockPlaylistRepresentation)
      )
      val mockTimelineMeta =
        TimelineMeta(Some("00000172-9b87-0a50-ffff-ffff8eec7ee8"), Some("00000172-9b87-0a50-ffff-ffff8eec7ee8"))
    }

    trait FailureCase extends Context {
      val mockTimelineItems = List.empty
      val mockTimelineMeta = TimelineMeta(None, None)
    }

    "returns a 200 if timeline returns a success response" in new SuccessCase {
      val queryParams = "?limit=10"

      Seq(
        "/me/activities",
        "/me/activities/all/own"
      ).foreach { endpoint =>
        val path = s"$endpoint$queryParams"

        val mockRequest = Request(path)
        val pagination =
          CursorBasedPagination.build("http://api.soundcloud.com", mockRequest, Seq("linked_partitioning"))

        val mockTimelineResponse = Timeline(mockTimelineItems, mockTimelineMeta, pagination)
        when(timelineService.fetchTimelineForUser(session, access, None, false, 10, pagination))
          .thenReturn(Future.value(mockTimelineResponse))

        val result = get(path)
        result.statusCode === 200
        result.contentString === mockTimelineResponse.getRepresentation()
      }
    }

    "returns an empty array if now followings tracks found" in new FailureCase {
      "return Timeline with empty tracks if no events found" in new FailureCase {
        val queryParams = "?limit=10"

        Seq(
          "/me/activities",
          "/me/activities/all/own"
        ).foreach { endpoint =>
          val path = s"$endpoint$queryParams"

          val mockRequest = Request(path)
          val pagination =
            CursorBasedPagination.build("http://api.soundcloud.com", mockRequest, Seq("linked_partitioning"))

          val mockTimelineResponse = Timeline(mockTimelineItems, mockTimelineMeta, pagination)
          when(timelineService.fetchTimelineForUser(session, access, None, false, 10, pagination))
            .thenReturn(Future.value(mockTimelineResponse))

          val result = get(path)
          result.statusCode === 200
          result.contentString === mockTimelineResponse.getRepresentation()
        }
      }
    }
  }
}
