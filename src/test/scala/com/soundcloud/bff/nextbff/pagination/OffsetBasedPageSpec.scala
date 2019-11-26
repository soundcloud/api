package com.soundcloud.bff.nextbff.pagination

import com.soundcloud.jvmkit.module.http.client.StringParam
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.jvmkit.module.util.Urn
import com.twitter.finagle.http.Request

class OffsetBasedPageSpec extends UnitSpecification {
  trait Context extends Scope {
    val param = Urn("soundcloud", "tracks", "2")
    val baseUrl = "http://www.foo.bar"
    val path = "/some/path:123:456"
    val offset = 2
    val limit = 4
  }

  "apply" >> {
    trait ApplyContext extends Context {
      val uri = "/some/path:123:456?foo=bar&q=mordor&frodo=evil"

      def request = Request(uri)
    }

    "base apply" in new ApplyContext {
      OffsetBasedPage(request, baseUrl)(param) ==== OffsetBasedPage(param, baseUrl, path, Map(), 0, 10)
    }
  }

  "class" >> {
    trait PageContext extends Context {
      def request = Request("")

      val extraParams = Map("foo" -> "bar")

      def page = OffsetBasedPage(param, baseUrl, path, extraParams, offset, limit)
    }

    "returns the pagination params" in new PageContext {
      page.params mustEqual
        Map(
          "offset" -> StringParam(offset.toString),
          "limit" -> StringParam(limit.toString)
        )
    }

    "returns the next page with the new offset" in new PageContext {
      page.next ==== OffsetBasedPage(param, baseUrl, path, extraParams, 6, limit)
    }

    "returns the href url" in new PageContext {
      page.href ==== "http://www.foo.bar/some/path%3A123%3A456?foo=bar&limit=4&offset=2"
    }
  }
}
