package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.bff.nextbff.mapper.{EmbeddedItem, Mapper}
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.client.TimelineJsonClient
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCounts
import com.soundcloud.publicApiStrangler.mapper.timeline.publicApi.ActivitiesWithOriginMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.representation.{Playlist, Track, User}
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper}
import com.soundcloud.publicApiStrangler.service.TimelineService
import com.soundcloud.publicApiStrangler.service.pagination.Pagination
import com.soundcloud.publicApiStrangler.service.timeline.{Timeline, TimelineMeta, TrackTimelineItem}
import com.soundcloud.publicApiStrangler.support.CursorPagination
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.Request
import com.twitter.util.Future
import org.joda.time.DateTime
import org.mockito.Mockito._
import play.api.libs.json.Json

class TimeLineHandlerSpec extends UnitSpecification {
  trait Context extends HandlerSpecificationScope with TimeLineHandlerTestData {
    val entityMapper = mock[EntityMapper]
    val entitySummaryMapper = mock[EntitySummaryMapper]
    val timelineClient = mock[TimelineJsonClient]
    val timelineService = mock[TimelineService]

    val session = loggedInSession(usrUrn)
    val context = new MappingContext(session)

    // Mocking out the EntityMapper and EntitySummaryMapper is necessary because of the calls to external services
    // Howver the mocking is very hard due to the next-bff stuff that lacks proper types and has mutable state
    val user = new User(userJson, baseUrl, Some(FollowCounts(usrUrn, 42, 23)), Some(33))(context)
    val userItem = EmbeddedItem(entityMapper.asInstanceOf[Mapper[Any, JsonMapping]], usrUrn)
    userItem.materialize(Map(usrUrn -> user))

    entitySummaryMapper.embed(===(usrUrn))(any[MappingContext]) returns userItem

    val track =
      new Track(testTrackJson, Map(trackUrn -> 1234), Map(trackUrn -> 2345), baseUrl, entitySummaryMapper)(context)
    val playlist =
      new Playlist(playlistJson, Map(playlistUrn -> 34), Map(playlistUrn -> 84), baseUrl, entitySummaryMapper)(context)

    entityMapper.map(any[UserSession], any[Set[Urn]])(any[MappingContext]) returns Future.value(
      Map(
        usrUrn -> user,
        trackUrn -> track,
        playlistUrn -> playlist
      )
    )

    val trackItem = EmbeddedItem(entityMapper.asInstanceOf[Mapper[Any, JsonMapping]], trackUrn)
    trackItem.materialize(Map(trackUrn -> track))
    val playlistItem = EmbeddedItem(entityMapper.asInstanceOf[Mapper[Any, JsonMapping]], playlistUrn)
    playlistItem.materialize(Map(playlistUrn -> playlist))

    entityMapper.embed(===(trackUrn))(any[MappingContext]) returns trackItem
    entityMapper.embed(===(playlistUrn))(any[MappingContext]) returns playlistItem

    // With the entity mappers returning json objects, let the TimeLineHandler fiddle them together and assert the results
    val handler = new TimelineHandler(
      new FakeUserAuthentication(session),
      new ActivitiesWithOriginMapper(timelineClient, entityMapper, entitySummaryMapper),
      new CursorPagination(baseUrl),
      timelineService
    )

    override def routingDefinitions = Routing.forTimelineHandler(handler)

    timelineClient
      .stream(any[UserSession], any[Option[String]], any[Int], any[Boolean], any[Option[String]])
      .returns(Future.value(timeline))
    timelineClient
      .followingsTracks(any[UserSession], any[Option[String]], any[Int], any[Boolean], any[Option[String]])
      .returns(Future.value(onlyTracksTimeline))
  }

  "render track activities" >> {
    trait SuccessfulCase extends Context {
      val mockTimelineItems = List(
        new TrackTimelineItem(createdAt = new DateTime().toString, List.empty, mockTrackRepresentation)
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
        "/me/activities/tracks",
        "/me/activities/tracks/sometag"
      ).foreach { endpoint =>
        val path = s"${endpoint}${queryParams}"

        val mockRequest = Request(path)
        mockRequest.host = "localhost"
        val pagination = Pagination.buildCursorBasedPagination(mockRequest, Seq("linked_partitioning"))

        val mockTimelineResponse = Timeline(mockTimelineItems, mockTimelineMeta, pagination)
        when(timelineService.fetchTimelineTracksForUser(session, None, false, 10, Some("uuid"), pagination))
          .thenReturn(Future.value(mockTimelineResponse))

        val result = get(path)

        result.statusCode === 200
        result.contentString = mockTimelineResponse.getRepresentation()
      }
    }

    "return Timeline with empty tracks if no track events found" in new FailureCase {
      val queryParams = "?limit=10"

      Seq(
        "/me/activities/tracks",
        "/me/activities/tracks/sometag"
      ).foreach { endpoint =>
        val path = s"${endpoint}${queryParams}"

        val mockRequest = Request(path)
        mockRequest.host = "localhost"
        val pagination = Pagination.buildCursorBasedPagination(mockRequest, Seq("linked_partitioning"))

        val mockTimelineResponse = Timeline(mockTimelineItems, mockTimelineMeta, pagination)
        when(timelineService.fetchTimelineTracksForUser(session, None, false, 10, Some("uuid"), pagination))
          .thenReturn(Future.value(mockTimelineResponse))

        val result = get(path)
        result.statusCode === 200
        result.contentString === mockTimelineResponse.getRepresentation()
      }
    }

    "returns 404 if not Timeline returned from service" in new Context {
      val queryParams = "?limit=10"

      Seq(
        "/me/activities/tracks",
        "/me/activities/tracks/sometag"
      ).foreach { endpoint =>
        val path = s"${endpoint}${queryParams}"

        val mockRequest = Request(path)
        mockRequest.host = "localhost"
        val pagination = Pagination.buildCursorBasedPagination(mockRequest, Seq("linked_partitioning"))

        when(timelineService.fetchTimelineTracksForUser(session, None, false, 10, Some("uuid"), pagination))
          .thenReturn(Future.value(null))

        val result = get(path)
        result.statusCode === 404
        result.contentString === "{\"errors\":[{\"error_message\":\"404 - Not Found\"}]}"
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
      val path = s"/me/followings/tracks${queryParams}"

      when(timelineService.fetchFollowingTracksForUser(session, None, false, 10, Some("uuid")))
        .thenReturn(Future.value(mockTimelineResponse))

      val result = get(path)
      result.statusCode === 200
      result.contentString === Json.stringify(Json.toJson(List(mockTrackRepresentation)))
    }

    "returns an empty array if now followings tracks found" in new FailureCase {
      val queryParams = "?limit=10"
      val path = s"/me/followings/tracks${queryParams}"

      when(timelineService.fetchFollowingTracksForUser(session, None, false, 10, Some("uuid")))
        .thenReturn(Future.value(emptyTimelineResponse))

      val result = get(path)
      result.statusCode === 200
      result.contentString === "[]"
    }

    "returns a 404 if timeline service doesn't return tracks" in new FailureCase {
      val queryParams = "?limit=10"
      val path = s"/me/followings/tracks${queryParams}"

      when(timelineService.fetchFollowingTracksForUser(session, None, false, 10, Some("uuid")))
        .thenReturn(Future.value(noTimelineResponse))

      val result = get(path)
      result.statusCode === 404
      result.contentString === "{\"errors\":[{\"error_message\":\"404 - Not Found\"}]}"
    }
  }

  // public activity endpoints
  Seq(
    "/me/activities",
    "/me/activities/all",
    "/me/activities/all/own"
  ).foreach { endpoint =>
    endpoint in new Context {
      val response = get(endpoint)
      response.statusCode ==== 200
      response.contentString ==== publicCompleteTimelineJsonString(endpoint)

      there was one(timelineClient).stream(
        ===(session),
        any[Option[String]],
        any[Int],
        any[Boolean],
        any[Option[String]]
      )
    }
  }
}
