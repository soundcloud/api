package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.bff.test.fixtures.Fixtures
import com.soundcloud.publicApiStrangler.mapping.TrackTimelineItem
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.scalakit.finagle.jsonservice.{IntParam, StringParam}
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
    val page = mock[CursorBasedPage[Urn]]
    val nextPage = mock[CursorBasedPage[Urn]]

    override def before = {
      when(nextPage.href).thenReturn("foo.com?cursor=deadbeef")

      when(page.cursor).thenReturn(Some("15"))
      when(page.limit).thenReturn(100)
      when(page.next(Some("15"))).thenReturn(nextPage)
      when(timelineClient.stream(session, Some("15"), 100)).thenReturn(Future(timelineStream.as[JsObject]))
    }

    def result = Await.result(mapper.materialize(session, page)).get
  }

  "builds a collection" in new Context {
    result.collection.size mustEqual 5
    result.collection.head must beAnInstanceOf[TrackTimelineItem]
  }

  "builds a nextHref" in new Context {
    result.nextHref mustEqual "foo.com?cursor=deadbeef"
  }

  "builds a futureHref" in new Context {
    pending
//    result.futureHref mustEqual "foo.com/somewhere/only/we/know?cursor=deadbeef&limit=100&foo=bar"
  }

}
