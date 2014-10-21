package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.bff.test.fixtures.Fixtures
import com.soundcloud.publicApiStrangler.mapping.{TimelineWithUuids, TrackTimelineItem}
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.scalakit.finagle.jsonservice.{Params, IntParam, StringParam}
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

    def page: CursorBasedPage[Urn]

    def result = Await.result(mapper.materialize(session, page)).get.asInstanceOf[TimelineWithUuids]
  }

  "without a cursor" >> {
    trait NoCursor extends Context {
      val page = CursorBasedPage(urn, "foo.com", "/something", Map(), None, 100)

      override def before = {
        when(timelineClient.stream(session, None, 100)).thenReturn(Future(timelineStream.as[JsObject]))
      }
    }

    "builds a nextHref" in new NoCursor {
      result.nextHref mustEqual Some("https://foo.com/something?limit=100&cursor=15")
    }

    "builds a futureHref" in new NoCursor {
      result.futureHref mustEqual Some("https://foo.com/something?uuid%5Bto%5D=deadead&limit=100")
    }
  }

  "with a regular cursor" >> {
    trait Cursor extends Context {
      val page = CursorBasedPage(urn, "foo.com", "/something", Map(), Some("2"), 100)

      override def before = {
        when(timelineClient.stream(session, Some("2"), 100)).thenReturn(Future(timelineStream.as[JsObject]))
      }
    }

    "builds a collection" in new Cursor {
      result.collection.size mustEqual 4
      result.collection.head must beAnInstanceOf[TrackTimelineItem]
    }

    "filters-out invalid content" in new Cursor {
      result.collection.map(i => (i.json \ "type").as[String]).contains("promoted:stream") mustEqual false
    }

    "builds a nextHref" in new Cursor {
      result.nextHref mustEqual Some("https://foo.com/something?limit=100&cursor=15")
    }

    "doesn't build a futureHref" in new Cursor {
      result.futureHref mustEqual None
    }
  }

  "with a reverse cursor" >> {
    trait ReverseCursor extends Context {
      val page = CursorBasedPage(urn, "foo.com", "/something", Map("uuid[to]" -> "deadbeef"), None, 100)

      override def before = {
        when(timelineClient.stream(session, Some("deadbeef"), 100, true)).thenReturn(Future(timelineStream.as[JsObject]))
      }
    }

    "builds a nextHref" in new ReverseCursor {
      result.nextHref mustEqual Some("https://foo.com/something?limit=100&cursor=15")
    }

    "builds a futureHref" in new ReverseCursor {
      result.futureHref mustEqual Some("https://foo.com/something?uuid%5Bto%5D=deadead&limit=100")
    }
  }

}
