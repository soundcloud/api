package com.soundcloud.bff.nextbff.pagination

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.Urn
import com.soundcloud.scalakit.finagle.jsonservice.StringParam
import com.twitter.finagle.http.Request

class CursorBasedPageSpec extends UnitSpecification {

  trait Context extends Scope {
    val param = new Urn("soundcloud:tracks:2")
    val baseUrl = "http://www.foo.bar"
    val path = "/some/path:123:456"
    val cursor = "someOpaqueCursor"
    val limit = 4
  }

  "apply" >> {
    trait ApplyContext extends Context {
      val uri = "/some/path:123:456?foo=bar&q=mordor&frodo=evil"

      def request = Request(uri)
    }

    "base apply" in new ApplyContext {
      CursorBasedPage(request, baseUrl)(param) ==== CursorBasedPage(param, baseUrl, path, Map(), None, 10)
    }
  }

  "class" >> {
    trait PageContext extends Context {
      def request = Request("")

      val extraParams = Map("foo" -> "bar")

      def page = CursorBasedPage(param, baseUrl, path, extraParams, Some(cursor), limit)
    }

    "returns the pagination params" in new PageContext {
      page.params mustEqual
        Map(
          "cursor" -> StringParam(cursor),
          "limit" -> StringParam(limit.toString)
        )
    }

    "returns the next page with the new cursor" in new PageContext {
      page.next("newCursor") ==== CursorBasedPage(param, baseUrl, path, extraParams, Some("newCursor"), limit)
    }

    "returns the href url" in new PageContext {
      page.href ==== "http://www.foo.bar/some/path%3A123%3A456?foo=bar&limit=4&cursor=someOpaqueCursor"
    }
  }
}
