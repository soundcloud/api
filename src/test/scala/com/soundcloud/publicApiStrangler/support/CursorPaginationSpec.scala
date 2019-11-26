package com.soundcloud.publicApiStrangler.support

import com.soundcloud.jvmkit.module.http.server.ResponseBuilder
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.{Await, Future}

class CursorPaginationSpec extends UnitSpecification {
  trait Context extends Scope {
    val pagination = new CursorPagination("http://test")
  }

  "returns the function's result for well-formed pagination params" in new Context {
    val request = Request("/test?limit=11&cursor=test")
    val urn = Urn("soundcloud", "users", "2")
    val response =
      pagination.withPage(request, urn) { page =>
        page.cursor mustEqual Some("test")
        page.limit mustEqual 11
        page.param mustEqual urn
        Future.value(ResponseBuilder.ok())
      }
    Await.result(response).status ==== Status.Ok
  }

  "returns bad request for malformed pagination params" in new Context {
    val request = Request("/test?limit=banana&cursor=test")
    val urn = Urn("soundcloud", "users", "2")
    val response = pagination.withPage(request, urn) { page =>
      Future.value(ResponseBuilder.ok())
    }
    Await.result(response).status ==== Status.BadRequest
  }
}
