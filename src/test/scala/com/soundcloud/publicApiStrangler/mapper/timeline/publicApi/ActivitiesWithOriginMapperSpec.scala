package com.soundcloud.publicApiStrangler.mapper.timeline.publicApi

import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper}
import com.soundcloud.publicApiStrangler.mapping.timeline.publicApi.{TimelineItemWithOrigin, TimelineWithOrigin}
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.services.timeline.TimelineJsonClient
import com.twitter.util.{Await, Future}
import play.api.libs.json.JsObject
import org.mockito.Mockito.when
import org.specs2.mutable.Before

class ActivitiesWithOriginMapperSpec extends UnitSpecification with Fixtures {

  trait Context extends Scope {
    val timelineClient = mock[TimelineJsonClient]
    val entityMapper = mock[EntityMapper]
    val entitySummaryMapper = mock[EntitySummaryMapper]
    val mapper = new ActivitiesWithOriginMapper(timelineClient, entityMapper, entitySummaryMapper)
    val session = mock[UserSession]
    val urn = new Urn("soundcloud:users:1")
    val uuid = "41d4f7d6-6480-0000-6291-bef9209ec18e"
    val nextUuid = "41d51546-b500-0000-61e0-02e5d7b15300"
    val futureUuid = "41d51624-6140-0000-6191-c98a01a7ddc9"

    def page: CursorBasedPage[Urn]

    def result = Await.result(mapper.materialize(session, page)).get.asInstanceOf[TimelineWithOrigin]
  }

  "with a regular cursor" >> {
    trait Cursor extends Context with Before {
      val page = CursorBasedPage(urn, "http://foo.com", "/something", Map(), Some(uuid), 100)

      override def before: Any = {
        when(timelineClient.stream(session, Some(uuid), 100, false, Some("uuid"))).thenReturn(Future(timelineActivities.as[JsObject]))
      }
    }

    "builds a collection" in new Cursor {
      result.collection.size mustEqual 10
      result.collection.head must beAnInstanceOf[TimelineItemWithOrigin]
    }

    "builds a nextHref" in new Cursor {
      result.nextHref mustEqual Some(s"http://foo.com/something?limit=100&cursor=$nextUuid")
    }

    "builds a futureHref" in new Cursor {
      result.futureHref mustEqual s"http://foo.com/something?uuid%5Bto%5D=$futureUuid&limit=100"
    }

  }
}
