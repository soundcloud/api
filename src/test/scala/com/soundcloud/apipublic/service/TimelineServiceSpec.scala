package com.soundcloud.apipublic.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.{LoggedInUserSession, UserSession}
import com.soundcloud.apipublic.client.TimelineJsonClient
import com.soundcloud.apipublic.client.tracks.TrackRequest
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.pagination.CursorBasedPagination
import com.soundcloud.apipublic.service.playlists.{PlaylistBuilder, PlaylistRequest}
import com.soundcloud.apipublic.service.timeline.{PlaylistTimelineItem, Timeline, TrackTimelineItem}
import com.soundcloud.apipublic.service.trackrepresentation.{
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
    val playlistUrn1 = Urn("soundcloud", "playlists", "88")
    val playlistUrn2 = Urn("soundcloud", "playlists", "99")
    val playlist1 = new PlaylistBuilder().setId(playlistUrn1.identifier.toLong).build
    val playlist2 = new PlaylistBuilder().setId(playlistUrn2.identifier.toLong).build

    val access = AccessParams.defaultAccess

    val mockTrackRepresentation = createTrackRepresentation
    val timelineStreamMock: JsObject = Json.obj(
      "events" -> Json.arr(
        Json.obj(
          "type" -> JsString("playlist"),
          "timestamp" -> JsString("2020/06/11 00:01:18 +0000"),
          "urn" -> JsString(playlistUrn1.toString),
          "actor" -> JsString(requestingUserUrn.toString),
          "cursor" -> JsString("000001723-8888-0a50-fffg-ffff8eecj774"),
          "unique_id" -> JsString("000001723-8888-0a50-fffg-ffff8eecj774")
        ),
        Json.obj(
          "type" -> JsString("playlist:repost"),
          "timestamp" -> JsString("2020/06/11 00:00:18 +0000"),
          "urn" -> JsString(playlistUrn2.toString),
          "actor" -> JsString(requestingUserUrn.toString),
          "cursor" -> JsString("000001723-8888-0a50-eeee-ffff8eecj775"),
          "unique_id" -> JsString("000001723-8888-0a50-eeee-ffff8eecj775")
        ),
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
    val pagination =
      CursorBasedPagination("https://api.soundcloud.com", "me/activities/*", ParamMap(), Some("2"), 3)

    val timelineClient = mock[TimelineJsonClient]
    val trackService = mock[TrackRepresentationsService]
    val playlistsService = mock[PlaylistsService]

    val timelineService = new TimelineService(timelineClient, trackService, playlistsService)
  }

  "#fetchTimelineTracksForUser" >> {
    trait SuccessCase extends Context {
      def setupMocksForTimelineResponse(session: UserSession) = {
        when(timelineClient.stream(session, None, 10, reverseCursor = false))
          .thenReturn(Future.value(timelineStreamMock))

        when(trackService.tracks(session, List(TrackRequest(trackUrn, None)), access))
          .thenReturn(Future.value(List(mockTrackRepresentation)))
      }
    }

    trait FailureCase extends Context {
      def setupMocksForTimelineResponse(session: UserSession) = {
        when(timelineClient.stream(session, None, 10, reverseCursor = false))
          .thenReturn(Future.value(emptyTimelineStreamMock))

        when(trackService.tracks(session, List.empty, access))
          .thenReturn(Future.value(List.empty))
      }
    }
    "returns data on successful request" in new SuccessCase {
      setupMocksForTimelineResponse(session)

      val response = Await.result(
        timelineService.fetchTimelineTracksForUser(
          session.asInstanceOf[LoggedInUserSession],
          access,
          None,
          reverseCursor = false,
          10,
          pagination
        )
      )

      response must beAnInstanceOf[Timeline]
      response.timelineItems.length === 1
      response.timelineItems.head must beAnInstanceOf[TrackTimelineItem]

      response.metaInfo.nextPageCursor === Some("00000172-9b87-0a50-ffff-ffff8eec7ee8")
      response.metaInfo.previousPageCursor === Some("00000172-9b87-0a50-ffff-ffff8eec7ee8")
    }

    "returns empty timeline items list if no tracks found" in new FailureCase {
      setupMocksForTimelineResponse(session)

      val response = Await.result(
        timelineService.fetchTimelineTracksForUser(
          session.asInstanceOf[LoggedInUserSession],
          access,
          None,
          reverseCursor = false,
          10,
          pagination
        )
      )

      response must beAnInstanceOf[Timeline]
      response.timelineItems.length === 0
    }
  }

  "#fetchTimelineForUser" >> {
    trait SuccessCase extends Context {
      def setupMocksForTimelineResponse(session: UserSession) = {
        when(timelineClient.stream(session, None, 10, reverseCursor = false))
          .thenReturn(Future.value(timelineStreamMock))

        when(trackService.tracks(session, List(TrackRequest(trackUrn, None)), access))
          .thenReturn(Future.value(List(mockTrackRepresentation)))

        when(
          playlistsService.fetchPlaylistsMetadataOnly(
            session,
            List(PlaylistRequest(playlistUrn1, None), PlaylistRequest(playlistUrn2, None))
          )
        ).thenReturn(Future.value(List(playlist1, playlist2)))
      }
    }

    trait FailureCase extends Context {
      def setupMocksForTimelineResponse(session: UserSession) = {
        when(timelineClient.stream(session, None, 10, reverseCursor = false))
          .thenReturn(Future.value(emptyTimelineStreamMock))

        when(trackService.tracks(session, List.empty, access))
          .thenReturn(Future.value(List.empty))

        when(playlistsService.fetchPlaylistsMetadataOnly(session, List.empty))
          .thenReturn(Future.value(List.empty))
      }
    }

    "returns data on successful request" in new SuccessCase {
      setupMocksForTimelineResponse(session)

      val response = Await.result(
        timelineService.fetchTimelineForUser(
          session.asInstanceOf[LoggedInUserSession],
          access,
          None,
          reverseCursor = false,
          10,
          pagination
        )
      )

      response must beAnInstanceOf[Timeline]
      response.timelineItems.length === 3

      response.timelineItems.head must beAnInstanceOf[PlaylistTimelineItem]
      response.timelineItems(1) must beAnInstanceOf[PlaylistTimelineItem]
      response.timelineItems(2) must beAnInstanceOf[TrackTimelineItem]

      response.metaInfo.nextPageCursor === Some("00000172-9b87-0a50-ffff-ffff8eec7ee8")
      response.metaInfo.previousPageCursor === Some("00000172-9b87-0a50-ffff-ffff8eec7ee8")
    }

    "returns empty timeline items list if no activities found" in new FailureCase {
      setupMocksForTimelineResponse(session)

      val response = Await.result(
        timelineService.fetchTimelineForUser(
          session.asInstanceOf[LoggedInUserSession],
          access,
          None,
          reverseCursor = false,
          10,
          pagination
        )
      )

      response must beAnInstanceOf[Timeline]
      response.timelineItems.length === 0

      response.metaInfo.nextPageCursor === None
      response.metaInfo.previousPageCursor === None
    }
  }

  "#fetchFollowingTracksForUser" >> {
    trait SuccessCase extends Context {
      def setupMocksForTimelineResponse(session: UserSession) = {
        when(timelineClient.followingsTracks(session, None, 10, reverseCursor = false))
          .thenReturn(Future.value(timelineFollowingTracksMock))

        when(trackService.tracks(session, List(TrackRequest(trackUrn, None)), access))
          .thenReturn(Future.value(List(mockTrackRepresentation)))
      }
    }

    trait FailureCase extends Context {
      def setupMocksForTimelineResponse(session: UserSession) = {
        when(timelineClient.followingsTracks(session, None, 10, reverseCursor = false))
          .thenReturn(Future.value(emptyTimelineStreamMock))

        when(trackService.tracks(session, List.empty, access))
          .thenReturn(Future.value(List.empty))
      }
    }

    "returns data on successful request" in new SuccessCase {
      setupMocksForTimelineResponse(session)

      val response = Await.result(
        timelineService.fetchFollowingTracksForUser(
          session.asInstanceOf[LoggedInUserSession],
          access,
          None,
          reverseCursor = false,
          10
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
          access,
          None,
          reverseCursor = false,
          10
        )
      )

      response must beAnInstanceOf[List[TrackRepresentation]]
      response.length === 0
    }
  }
}
