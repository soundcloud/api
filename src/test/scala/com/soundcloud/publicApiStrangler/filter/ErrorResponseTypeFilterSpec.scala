package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.jvmkit.module.http.server.{JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.Service
import com.twitter.finagle.http._
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import play.api.libs.json.Json

class ErrorResponseTypeFilterSpec extends UnitSpecification {
  trait Context extends Scope {
    val next = mock[Service[Request, Response]]
    val telemetry = Telemetry.createIsolatedInstance
    val filter = new ErrorResponseTypeFilter(telemetry)
  }

  "leaves 2xx responses unchanged" in new Context {
    val request = Request()
    val responseFromNextService = ResponseBuilder.ok()
    when(next.apply(request)).thenReturn(Future.value(responseFromNextService))

    Await.result(filter(request, next)) mustEqual responseFromNextService
  }

  "leaves response with body unchanged" in new Context {
    val request = Request()
    val content = """{"kind":"track","id":278030262,"user_id":165217281}"""
    val responseFromNextService = JsonResponseBuilder.ok(content)
    when(next.apply(request)).thenReturn(Future.value(responseFromNextService))

    Await.result(filter(request, next)) mustEqual responseFromNextService
  }

  "adds body in case of failed response" in new Context {
    val request = Request("/some-path")
    val responseFromNextService = JsonResponseBuilder.badRequest()
    when(next.apply(request)).thenReturn(Future.value(responseFromNextService))

    val response = Json.parse(Await.result(filter(request, next)).contentString)
    (response \ "code").as[Int] mustEqual 400
    filter.emptyBodyErrorResponseCounter.labels("/some-path", "400").get === 1
  }
}
