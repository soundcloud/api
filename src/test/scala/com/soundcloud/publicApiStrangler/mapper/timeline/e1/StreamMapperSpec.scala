package com.soundcloud.publicApiStrangler.mapper.timeline.e1

import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.bff.test.fixtures.Fixtures
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper}
import com.soundcloud.publicApiStrangler.mapping.timeline.e1.{TimelineWithUuids, TrackTimelineItem}
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.scalakit.{Urn, UserSession}
import com.soundcloud.service.client.TimelineClient
import com.twitter.util.{Await, Future}
import play.api.libs.json.JsObject

class StreamMapperSpec extends UnitSpecification with Fixtures {

  trait Context extends VerifiedMocks {
    val timelineClient = mock[TimelineClient]
    val entityMapper = mock[EntityMapper]
    val entitySummaryMapper = mock[EntitySummaryMapper]
    val mapper = new StreamMapper(timelineClient, entityMapper, entitySummaryMapper)
    val session = mock[UserSession]
    val urn = Urn("soundcloud:users:1")
    val gokuCursor = "4743688807709163520A00000000000000000000"
    val uuid = "41d4f7d6-6480-4000-8000-000000000000"

    def page: CursorBasedPage[Urn]

    def result = Await.result(mapper.materialize(session, page)).get.asInstanceOf[TimelineWithUuids]
  }

  "without a cursor" >> {
    trait NoCursor extends Context {
      val page = CursorBasedPage(urn, "https://foo.com", "/something", Map(), None, 100)

      override def before = {
        when(timelineClient.stream(session, None, 100)).thenReturn(Future(timelineStream.as[JsObject]))
      }
    }

    "builds a nextHref" in new NoCursor {
      result.nextHref mustEqual Some("https://foo.com/something?limit=100&cursor=00000000-0000-400f-8000-000000000000")
    }

    "builds a futureHref" in new NoCursor {
      result.futureHref mustEqual s"https://foo.com/something?uuid%5Bto%5D=41d51366-f940-4000-8000-000000000000&limit=100"
    }
  }

  "with a regular cursor" >> {
    trait Cursor extends Context {
      val page = CursorBasedPage(urn, "https://foo.com", "/something", Map(), Some(uuid), 100)

      override def before = {
        when(timelineClient.stream(session, Some(gokuCursor), 100)).thenReturn(Future(timelineStream.as[JsObject]))
      }
    }

    "builds a collection" in new Cursor {
      result.collection.size mustEqual 4
      result.collection.head must beAnInstanceOf[TrackTimelineItem]
    }

    "filters-out invalid content" in new Cursor {
      result.collection.map(_.`type`).contains("promoted:stream") mustEqual false
    }

    "builds a nextHref" in new Cursor {
      result.nextHref mustEqual Some("https://foo.com/something?limit=100&cursor=00000000-0000-400f-8000-000000000000")
    }

    "doesn't build a futureHref" in new Cursor {
      result.futureHref mustEqual "https://foo.com/something?uuid%5Bto%5D=41d51366-f940-4000-8000-000000000000&limit=100"
    }
  }

  "with a reverse cursor" >> {
    trait ReverseCursor extends Context {
      val page = CursorBasedPage(urn, "https://foo.com", "/something", Map("uuid[to]" -> uuid.toString), None, 100)

      override def before = {
        when(timelineClient.stream(session, Some(gokuCursor), 100, true)).thenReturn(Future(timelineStream.as[JsObject]))
      }
    }

    "builds a nextHref" in new ReverseCursor {
      result.nextHref mustEqual Some("https://foo.com/something?limit=100&cursor=00000000-0000-400f-8000-000000000000")
    }

    "builds a futureHref" in new ReverseCursor {
      result.futureHref mustEqual s"https://foo.com/something?uuid%5Bto%5D=41d51366-f940-4000-8000-000000000000&limit=100"
    }
  }

}
