package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.{LoggedInUserSession, UserSession}
import com.soundcloud.publicApiStrangler.client.TimelineJsonClient
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.timeline.{Timeline, TrackTimelineItem}
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackRepresentation,
  TrackRepresentationsService,
  TrackRepresentationsSpecificationContext
}
import com.twitter.finagle.http.ParamMap
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import play.api.libs.json.{JsNull, JsObject, JsString, Json}

class TimelineServiceSpec extends TrackRepresentationsSpecificationContext {
  trait Context extends TrackRepresentationsContext {
    val followingUserUrn = Urn("soundcloud", "users", "2012")
    val mockTrackRepresentation = createTrackRepresentation
    val timelineStreamMock: JsObject = Json.obj(
      "events" -> Json.arr(
        Json.obj(
          "type" -> JsString("track"),
          "timestamp" -> JsString("2020/06/10 00:00:18 +0000"),
          "urn" -> JsString(trackUrn.toString),
          "actor" -> JsString(requestingUserUrn.toString),
          "cursor" -> JsString("00000172-9b87-0a50-ffff-ffff8eec7ee8"),
          "unique_id" -> JsString("00000172-9b87-0a50-ffff-ffff8eec7ee8")
        )
      ),
      "meta" -> Json.obj(
        "next_page_cursor" -> JsString("00000172-9b87-0a50-ffff-ffff8eec7ee8"),
        "previous_page_cursor" -> JsString("00000172-9b87-0a50-ffff-ffff8eec7ee8")
      )
    )

    val timelineFollowingTracksMock: JsObject = Json.obj(
      "events" -> Json.arr(
        Json.obj(
          "type" -> JsString("track"),
          "timestamp" -> JsString("2020/06/10 00:00:18 +0000"),
          "urn" -> JsString(trackUrn.toString),
          "actor" -> JsString(followingUserUrn.toString),
          "cursor" -> JsString("00000172-9b87-0a50-ffff-ffff8eec7ee8"),
          "unique_id" -> JsString("00000172-9b87-0a50-ffff-ffff8eec7ee8")
        )
      ),
      "meta" -> Json.obj(
        "next_page_cursor" -> JsString("00000172-9b87-0a50-ffff-ffff8eec7ee9"),
        "previous_page_cursor" -> JsString("00000172-9b87-0a50-ffff-ffff8eec7ee9")
      )
    )

    val emptyTimelineStreamMock: JsObject = Json.obj(
      "events" -> Json.arr(),
      "meta" -> Json.obj(
        "next_page_cursor" -> JsNull,
        "previous_page_cursor" -> JsNull
      )
    )

    val baseUrl = "https://api.soundcloud.com"
    val tracksPagination =
      CursorBasedPagination("https://api.soundcloud.com", "me/activities/tracks", ParamMap(), Some("2"), 1)

    val timelineClient = mock[TimelineJsonClient]
    val trackService = mock[TrackRepresentationsService]

    val timelineService = new TimelineService(timelineClient, trackService)
  }

  "#fetchTimelineTracksForUser" >> {
    trait SuccessCase extends Context {
      def setupMocksForTimelineResponse(session: UserSession) = {
        when(timelineClient.stream(session, None, 10, false, Some("uuid")))
          .thenReturn(Future.value(timelineStreamMock))

        when(trackService.tracks(session, List(TrackRequest(trackUrn, None))))
          .thenReturn(Future.value(List(mockTrackRepresentation)))
      }
    }

    trait FailureCase extends Context {
      def setupMocksForTimelineResponse(session: UserSession) = {
        when(timelineClient.stream(session, None, 10, false, Some("uuid")))
          .thenReturn(Future.value(emptyTimelineStreamMock))

        when(trackService.tracks(session, List.empty))
          .thenReturn(Future.value(List.empty))
      }
    }
    "returns data on successful request" in new SuccessCase {
      setupMocksForTimelineResponse(session)

      val response = Await.result(
        timelineService.fetchTimelineTracksForUser(
          session.asInstanceOf[LoggedInUserSession],
          None,
          false,
          10,
          Some("uuid"),
          tracksPagination
        )
      )

      response must beAnInstanceOf[Timeline]
      response.timelineItems.length === 1
      response.timelineItems(0) must beAnInstanceOf[TrackTimelineItem]

      response.metaInfo.nextPageCursor === Some("00000172-9b87-0a50-ffff-ffff8eec7ee8")
      response.metaInfo.previousPageCursor === Some("00000172-9b87-0a50-ffff-ffff8eec7ee8")
    }

    "returns empty timeline items list if no tracks found" in new FailureCase {
      setupMocksForTimelineResponse(session)

      val response = Await.result(
        timelineService.fetchTimelineTracksForUser(
          session.asInstanceOf[LoggedInUserSession],
          None,
          false,
          10,
          Some("uuid"),
          tracksPagination
        )
      )

      response must beAnInstanceOf[Timeline]
      response.timelineItems.length === 0
    }
  }

  "#fetchFollowingTracksForUser" >> {
    trait SuccessCase extends Context {
      def setupMocksForTimelineResponse(session: UserSession) = {
        when(timelineClient.followingsTracks(session, None, 10, false, Some("uuid")))
          .thenReturn(Future.value(timelineFollowingTracksMock))

        when(trackService.tracks(session, List(TrackRequest(trackUrn, None))))
          .thenReturn(Future.value(List(mockTrackRepresentation)))
      }
    }

    trait FailureCase extends Context {
      def setupMocksForTimelineResponse(session: UserSession) = {
        when(timelineClient.followingsTracks(session, None, 10, false, Some("uuid")))
          .thenReturn(Future.value(emptyTimelineStreamMock))

        when(trackService.tracks(session, List.empty))
          .thenReturn(Future.value(List.empty))
      }
    }

    "returns data on successful request" in new SuccessCase {
      setupMocksForTimelineResponse(session)

      val response = Await.result(
        timelineService.fetchFollowingTracksForUser(
          session.asInstanceOf[LoggedInUserSession],
          None,
          false,
          10,
          Some("uuid")
        )
      )

      response must beAnInstanceOf[List[TrackRepresentation]]
      response.length === 1
    }

    "returns empty tracks list if no tracks found" in new FailureCase {
      setupMocksForTimelineResponse(session)

      val response = Await.result(
        timelineService.fetchFollowingTracksForUser(
          session.asInstanceOf[LoggedInUserSession],
          None,
          false,
          10,
          Some("uuid")
        )
      )

      response must beAnInstanceOf[List[TrackRepresentation]]
      response.length === 0
    }
  }
}
