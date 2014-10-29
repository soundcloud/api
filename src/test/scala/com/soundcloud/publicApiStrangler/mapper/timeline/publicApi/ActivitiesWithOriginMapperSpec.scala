package com.soundcloud.publicApiStrangler.mapper.timeline.publicApi

import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.bff.test.fixtures.Fixtures
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper}
import com.soundcloud.publicApiStrangler.mapping.timeline.publicApi.{TimelineWithOrigin, TimelineItemWithOrigin}
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.scalakit.{Urn, UserSession}
import com.soundcloud.service.client.TimelineClient
import com.twitter.util.{Await, Future}
import play.api.libs.json.JsObject

class ActivitiesWithOriginMapperSpec extends UnitSpecification with Fixtures {

  trait Context extends VerifiedMocks {
    val timelineClient = mock[TimelineClient]
    val entityMapper = mock[EntityMapper]
    val entitySummaryMapper = mock[EntitySummaryMapper]
    val mapper = new ActivitiesWithOriginMapper(timelineClient, entityMapper, entitySummaryMapper)
    val session = mock[UserSession]
    val urn = Urn("soundcloud:users:1")
    val gokuCursor = "4743688807709163520A00000000000000000000"
    val uuid = "41d4f7d6-6480-4000-8000-000000000000"

    def page: CursorBasedPage[Urn]

    def result = Await.result(mapper.materialize(session, page)).get.asInstanceOf[TimelineWithOrigin]
  }

  "with a regular cursor" >> {
    trait Cursor extends Context {
      val page = CursorBasedPage(urn, "http://foo.com", "/something", Map(), Some(uuid), 100)

      override def before = {
        when(timelineClient.stream(session, Some(gokuCursor), 100)).thenReturn(Future(timelineActivities.as[JsObject]))
      }
    }

    "builds a collection" in new Cursor {
      result.collection.size mustEqual 10
      result.collection.head must beAnInstanceOf[TimelineItemWithOrigin]
    }

    "builds a nextHref" in new Cursor {
      result.nextHref mustEqual Some(s"http://foo.com/something?limit=100&cursor=$uuid")
    }

    "builds a futureHref" in new Cursor {
      result.futureHref mustEqual "http://foo.com/something?uuid%5Bto%5D=41d4f7d6-6480-4000-8000-000000000000&limit=100"
    }

  }
}