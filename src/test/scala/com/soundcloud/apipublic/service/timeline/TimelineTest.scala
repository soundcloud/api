package com.soundcloud.apipublic.service.timeline

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.handler.TimeLineHandlerTestData
import com.soundcloud.apipublic.service.pagination.CursorBasedPagination
import com.soundcloud.apipublic.service.playlists.PlaylistBuilder
import com.twitter.finagle.http.Request
import org.specs2.matcher.Scope
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class TimelineTest extends Specification with Mockito {

  trait Context extends Scope with TimeLineHandlerTestData {

    val mockRequest = Request("/me/activities/track?limit=10")
    val pagination = CursorBasedPagination.build("https://localhost", mockRequest, Seq("linked_partitioning"))
    val mockTimelineItems = List(
      new TrackTimelineItem(createdAt = "2021-03-11T15:21:48.060+01:00", "track", mockTrackRepresentation)
    )
  }

  "TrackTimelineItem.getRepresentation" >> {
    val reposterUrn = Urn("soundcloud", "users", "42")
    "includes reposter when reposterUrn is set" in new Context {
      val item = new TrackTimelineItem(
        createdAt = "2021-03-11T15:21:48.060+01:00",
        "track:repost",
        mockTrackRepresentation,
        reposterUrn = Some(reposterUrn)
      )
      val json = item.getRepresentation()
      (json \ "reposter").asOpt[String] === Some(reposterUrn.toString)
    }
    "omits reposter when reposterUrn is None" in new Context {
      val item = new TrackTimelineItem(
        createdAt = "2021-03-11T15:21:48.060+01:00",
        "track",
        mockTrackRepresentation,
        reposterUrn = None
      )
      val json = item.getRepresentation()
      (json \ "reposter").asOpt[String] === None
    }
  }

  "PlaylistTimelineItem.getRepresentation" >> {
    val reposterUrn = Urn("soundcloud", "users", "42")
    val mockPlaylist = new PlaylistBuilder().build
    "includes reposter when reposterUrn is set" in new Context {
      val item = new PlaylistTimelineItem(
        createdAt = "2021-03-11T15:21:48.060+01:00",
        "playlist:repost",
        mockPlaylist,
        reposterUrn = Some(reposterUrn)
      )
      val json = item.getRepresentation()
      (json \ "reposter").asOpt[String] === Some(reposterUrn.toString)
    }
    "omits reposter when reposterUrn is None" in new Context {
      val item = new PlaylistTimelineItem(
        createdAt = "2021-03-11T15:21:48.060+01:00",
        "playlist",
        mockPlaylist,
        reposterUrn = None
      )
      val json = item.getRepresentation()
      (json \ "reposter").asOpt[String] === None
    }
  }

  trait FilledCursorContext extends Context {
    val mockTimelineMeta =
      TimelineMeta(Some("00000172-9b87-0a50-ffff-ffff8eec7ee9"), Some("00000172-9b87-0a50-ffff-ffff8eec7ee8"))

    val timelineResponse = Timeline(mockTimelineItems, mockTimelineMeta, pagination)
  }

  trait EmptyCursorContext extends Context {
    val mockTimelineMeta = TimelineMeta(None, None)
    val timelineResponse = Timeline(mockTimelineItems, mockTimelineMeta, pagination)
  }

  "returns response with complete href (cursor included)" in new FilledCursorContext {
    val result = timelineResponse.getRepresentation()

    result.contains(
      "\"next_href\":\"https://localhost/me/activities/track?limit=10&cursor=00000172-9b87-0a50-ffff-ffff8eec7ee8\""
    ) === true
    result.contains(
      "\"future_href\":\"https://localhost/me/activities/track?limit=10&cursor=00000172-9b87-0a50-ffff-ffff8eec7ee9\""
    ) === true
  }

  "returns response with incomplete href (cursor excluded)" in new EmptyCursorContext {
    val result = timelineResponse.getRepresentation()

    result.contains("\"next_href\":\"https://localhost/me/activities/track?limit=10\"") === true
    result.contains("\"next_href\":https://localhost/me/activities/track?limit=10&cursor") === false

  }
}
