package com.soundcloud.publicApiStrangler.mapper.timeline

import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.client.TimelineJsonClient
import com.soundcloud.publicApiStrangler.mapper.timeline.representation.e1.{TimelineWithUuids, TrackTimelineItem}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import org.specs2.mutable.Before
import play.api.libs.json.JsObject

class FollowingsTracksMapperSpec extends UnitSpecification {

  trait Context extends Scope with Before {
    val timelineClient = mock[TimelineJsonClient]
    val entityMapper = mock[EntityMapper]
    val entitySummaryMapper = mock[EntitySummaryMapper]
    val mapper = new FollowingsTracksMapper(timelineClient, entityMapper, entitySummaryMapper)
    val session = new UserSessionBuilder().build()
    val urn = Urn("soundcloud", "users", "1")
    val uuid = "41d4f7d6-6480-0000-6291-bef9209ec18e"
    val nextUuid = "41d5160c-5ac0-0000-61dc-d68a12e0c3c7"
    val futureUuid = "41d51624-6140-0000-6191-c98a01a7ddc9"

    def page: CursorBasedPage[Urn]

    def result = Await.result(mapper.materialize(session, page)).get.asInstanceOf[TimelineWithUuids]
  }

  "without a cursor" >> {
    trait NoCursor extends Context {
      val page = CursorBasedPage(urn, "https://foo.com", "/something", Map(), None, 100)

      override def before: Any = {
        when(timelineClient.followingsTracks(session, None, 100, false, Some("uuid"))).thenReturn(Future(timelineFollowingsTracks.as[JsObject]))
      }
    }

    "builds a nextHref" in new NoCursor {
      result.nextHref mustEqual Some(s"https://foo.com/something?limit=100&cursor=$nextUuid")
    }

    "builds a futureHref" in new NoCursor {
      result.futureHref mustEqual Some(s"https://foo.com/something?uuid%5Bto%5D=$futureUuid&limit=100")
    }
  }

  "with a regular cursor" >> {
    trait Cursor extends Context {
      val page = CursorBasedPage(urn, "https://foo.com", "/something", Map(), Some(uuid), 100)

      override def before: Any = {
        when(timelineClient.followingsTracks(session, Some(uuid), 100, false, Some("uuid"))).thenReturn(Future(timelineFollowingsTracks.as[JsObject]))
      }
    }

    "builds a collection" in new Cursor {
      result.collection.size mustEqual 2
      result.collection.head must beAnInstanceOf[TrackTimelineItem]
    }

    "builds a nextHref" in new Cursor {
      result.nextHref mustEqual Some(s"https://foo.com/something?limit=100&cursor=$nextUuid")
    }

    "doesn't build a futureHref" in new Cursor {
      result.futureHref mustEqual None
    }
  }

  "with a reverse cursor" >> {
    trait ReverseCursor extends Context {
      val page = CursorBasedPage(urn, "https://foo.com", "/something", Map("uuid[to]" -> uuid.toString), None, 100)

      override def before: Any = {
        when(timelineClient.followingsTracks(session, Some(uuid), 100, true, Some("uuid"))).thenReturn(Future(timelineFollowingsTracks.as[JsObject]))
      }
    }

    "builds a nextHref" in new ReverseCursor {
      result.nextHref mustEqual Some(s"https://foo.com/something?limit=100&cursor=$nextUuid")
    }

    "builds a futureHref" in new ReverseCursor {
      result.futureHref mustEqual Some(s"https://foo.com/something?uuid%5Bto%5D=$futureUuid&limit=100")
    }
  }
}
