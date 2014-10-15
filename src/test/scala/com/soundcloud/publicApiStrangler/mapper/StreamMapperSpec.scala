package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.bff.test.fixtures.Fixtures
import com.soundcloud.publicApiStrangler.mapping.TrackTimelineItem
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
    val mapper = new StreamMapper(timelineClient, entityMapper)
    val session = mock[UserSession]
    val urn = Urn("soundcloud:users:1")
    val page = CursorBasedPage(urn, "foo.com", "/something", Map(), Some("2"), 100)

    override def before = {
      when(timelineClient.stream(session, Some("2"), 100)).thenReturn(Future(timelineStream.as[JsObject]))
    }

    def result = Await.result(mapper.materialize(session, page)).get
  }

  "builds a collection" in new Context {
    result.collection.size mustEqual 5
    result.collection.head must beAnInstanceOf[TrackTimelineItem]
  }

  "builds a nextHref" in new Context {
    result.nextHref mustEqual "https://foo.com/something?limit=100&cursor=15"
  }

  "builds a futureHref" in new Context {
    result.futureHref mustEqual Some("https://foo.com/something?uuid%5Bto%5D=deadead&limit=100")
  }

}
