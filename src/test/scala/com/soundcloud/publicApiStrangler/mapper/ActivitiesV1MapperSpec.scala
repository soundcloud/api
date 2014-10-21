package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.bff.test.fixtures.Fixtures
import com.soundcloud.publicApiStrangler.mapping.{TimelineItemWithOrigin, TimelineWithUuids, TrackTimelineItem}
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.scalakit.{Urn, UserSession}
import com.soundcloud.service.client.TimelineClient
import com.twitter.util.{Await, Future}
import play.api.libs.json.JsObject

class ActivitiesV1MapperSpec extends UnitSpecification with Fixtures {

  trait Context extends VerifiedMocks {
    val timelineClient = mock[TimelineClient]
    val entityMapper = mock[EntityMapper]
    val entitySummaryMapper = mock[EntitySummaryMapper]
    val mapper = new ActivitiesWithOriginMapper(timelineClient, entityMapper, entitySummaryMapper)
    val session = mock[UserSession]
    val urn = Urn("soundcloud:users:1")

    def page: CursorBasedPage[Urn]

    def result = Await.result(mapper.materialize(session, page)).get
  }

  "with a regular cursor" >> {
    trait Cursor extends Context {
      val page = CursorBasedPage(urn, "foo.com", "/something", Map(), Some("2"), 100)

      override def before = {
        when(timelineClient.stream(session, Some("2"), 100)).thenReturn(Future(timelineActivities.as[JsObject]))
      }
    }

    "builds a collection" in new Cursor {
      result.collection.size mustEqual 10
      result.collection.head must beAnInstanceOf[TimelineItemWithOrigin]
    }

    "builds a nextHref" in new Cursor {
      result.nextHref mustEqual Some("https://foo.com/something?limit=100&cursor=4743688807709147136AYpG--SCewY49t7-FBA%3D%3D")
    }
  }

}
