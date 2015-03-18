package com.soundcloud.publicApiStrangler.support

import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.finagle.http.Request
import com.soundcloud.jvmkit.Urn
import com.soundcloud.bff.finagle.ResponseBuilder
import com.twitter.util.Await

class CursorPaginationSpec extends UnitSpecification {

  trait Context extends Scope {
    val pagination = new CursorPagination("http://test")
  }

  "returns the function's result for well-formed pagination params" in new Context {
    val request = Request("/test?limit=11&cursor=test")
    val urn = new Urn("soundcloud:users:2")
    val response =
      pagination.withPage(request, urn) { page =>
        page.cursor mustEqual Some("test")
        page.limit mustEqual 11
        page.param mustEqual urn
        new ResponseBuilder().ok.toFuture
      }
    Await.result(response).build.getStatusCode mustEqual 200
  }

  "returns bad request for malformed pagination params" in new Context {
    val request = Request("/test?limit=banana&cursor=test")
    val urn = new Urn("soundcloud:users:2")
    val response = pagination.withPage(request, urn){ page =>
        new ResponseBuilder().ok.toFuture
      }
    Await.result(response).build.getStatusCode mustEqual 400
  }
}
