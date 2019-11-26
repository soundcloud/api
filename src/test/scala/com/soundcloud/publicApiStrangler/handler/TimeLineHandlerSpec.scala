package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.bff.nextbff.mapper.{EmbeddedItem, Mapper}
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.client.TimelineJsonClient
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCounts
import com.soundcloud.publicApiStrangler.mapper.timeline.e1.{ActivitiesMapper, StreamMapper}
import com.soundcloud.publicApiStrangler.mapper.timeline.publicApi.ActivitiesWithOriginMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.representation.{Playlist, Track, User}
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper, FollowingsTracksMapper}
import com.soundcloud.publicApiStrangler.support.CursorPagination
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.util.Future

class TimeLineHandlerSpec extends UnitSpecification {
  trait Context extends HandlerSpecificationScope with TimeLineHandlerTestData {
    val entityMapper = mock[EntityMapper]
    val entitySummaryMapper = mock[EntitySummaryMapper]
    val timelineClient = mock[TimelineJsonClient]

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
      new StreamMapper(timelineClient, entityMapper, entitySummaryMapper),
      new ActivitiesMapper(timelineClient, entityMapper, entitySummaryMapper),
      new ActivitiesWithOriginMapper(timelineClient, entityMapper, entitySummaryMapper),
      new FollowingsTracksMapper(timelineClient, entityMapper, entitySummaryMapper),
      new CursorPagination(baseUrl)
    )

    override def routingDefinitions = Routing.forTimelineHandler(handler)

    timelineClient
      .activities(any[UserSession], any[Option[String]], any[Int], any[Boolean], any[Option[String]])
      .returns(Future.value(timeline))
    timelineClient
      .stream(any[UserSession], any[Option[String]], any[Int], any[Boolean], any[Option[String]])
      .returns(Future.value(timeline))
    timelineClient
      .followingsTracks(any[UserSession], any[Option[String]], any[Int], any[Boolean], any[Option[String]])
      .returns(Future.value(onlyTracksTimeline))
  }

  // private activity endpoints
  Seq(
    "/e1/me/activities",
    "/e1/me/activities.json"
  ).foreach { endpoint =>
    endpoint in new Context {
      val response = get(handler.renderAllActivities, endpoint)
      response.statusCode ==== 200
      response.contentString ==== timelineJsonString(endpoint)

      there was one(timelineClient).activities(
        ===(session),
        any[Option[String]],
        any[Int],
        any[Boolean],
        any[Option[String]]
      )
    }
  }

  // stream endpoints
  Seq(
    "/e1/me/stream",
    "/e1/me/stream.json"
  ).foreach { endpoint =>
    endpoint in new Context {
      val response = get(handler.renderStreamActivities, endpoint)
      response.statusCode ==== 200
      response.contentString ==== streamTimelineJsonString(endpoint)

      there was one(timelineClient).stream(
        ===(session),
        any[Option[String]],
        any[Int],
        any[Boolean],
        any[Option[String]]
      )
    }
  }

  // public activity endpoints
  Seq(
    "/me/activities",
    "/me/activities.json",
    "/me/activities/",
    "/me/activities/track",
    "/me/activities/tracks",
    "/me/activities/tracks/",
    "/me/activities/tracks.json",
    "/me/activities/tracks/sometag",
    "/me/activities/tracks/sometag.json",
    "/me/activities/all",
    "/me/activities/all.json",
    "/me/activities/all/own",
    "/me/activities/all/own.json"
  ).foreach { endpoint =>
    endpoint in new Context {
      val response = get(handler.renderPublicActivities, endpoint)
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

  // IFTTT endpoints
  Seq(
    "/me/followings/tracks",
    "/me/followings/tracks.json"
  ).foreach { endpoint =>
    endpoint in new Context {
      val response = get(handler.renderFollowingsTracks, endpoint)
      response.statusCode ==== 200
      response.contentString ==== tracksOnlyTimelineJsonString()

      there was one(timelineClient).followingsTracks(
        ===(session),
        any[Option[String]],
        any[Int],
        any[Boolean],
        any[Option[String]]
      )
    }
  }
}
