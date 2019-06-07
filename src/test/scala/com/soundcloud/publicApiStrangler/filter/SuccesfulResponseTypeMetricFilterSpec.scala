package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.jvmkit.module.http.server.{JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.Service
import com.twitter.finagle.http._
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when

class SuccesfulResponseTypeMetricFilterSpec extends UnitSpecification {

  trait Context extends Scope {
    val next = mock[Service[Request, Response]]
    val telemetry = Telemetry.createIsolatedInstance
    val filter = new SuccesfulResponseTypeMetricFilter(telemetry)

    def xmlCount = telemetry.getSampleValue(
      "successful_response_type_counter",
      Array("type"),
      Array("xml"))

    def jsonCount = telemetry.getSampleValue(
      "successful_response_type_counter",
      Array("type"),
      Array("json"))

    def jsonpCount = telemetry.getSampleValue(
      "successful_response_type_counter",
      Array("type"),
      Array("jsonp"))

    def undefinedCount = telemetry.getSampleValue(
      "successful_response_type_counter",
      Array("type"),
      Array("undefined"))
  }

  "Doesn't produce any metrics for non 2XX responses" in new Context {
    val request = Request()
    val responseFromNextService = ResponseBuilder.notFound()

    when(next.apply(request)).thenReturn(Future.value(responseFromNextService))
    Await.result(filter(request, next)) mustEqual responseFromNextService

    xmlCount must beNone
    jsonCount must beNone
    jsonpCount must beNone
    undefinedCount must beNone
  }

  "Increases xml counter on xml responses" in new Context {
    val request = Request()
    val content = """<?xml version="1.0" encoding="UTF-8"?><track><kind>track</kind></track>"""
    val responseFromNextService = JsonResponseBuilder.ok(content)

    when(next.apply(request)).thenReturn(Future.value(responseFromNextService))
    Await.result(filter(request, next)) mustEqual responseFromNextService

    xmlCount ==== Some(1d)
    jsonCount must beNone
    jsonpCount must beNone
    undefinedCount must beNone
  }

  "Increases json counter on json responses" in new Context {
    val request = Request()
    val content = """{"kind":"track","id":278030262,"user_id":165217281}"""
    val responseFromNextService = JsonResponseBuilder.ok(content)

    when(next.apply(request)).thenReturn(Future.value(responseFromNextService))
    Await.result(filter(request, next)) mustEqual responseFromNextService

    xmlCount must beNone
    jsonCount ==== Some(1d)
    jsonpCount must beNone
    undefinedCount must beNone
  }

  "Increases jsonp counter on jsonp responses" in new Context {
    val request = Request()
    val content = """/**/__jp6({"kind":"track","id":240934948});"""
    val responseFromNextService = JsonResponseBuilder.ok(content)

    when(next.apply(request)).thenReturn(Future.value(responseFromNextService))
    Await.result(filter(request, next)) mustEqual responseFromNextService

    xmlCount must beNone
    jsonCount must beNone
    jsonpCount ==== Some(1d)
    undefinedCount must beNone
  }

  "Increases undefined counter when response can not be identified" in new Context {
    val request = Request()
    val content = """neither"""
    val responseFromNextService = JsonResponseBuilder.ok(content)

    when(next.apply(request)).thenReturn(Future.value(responseFromNextService))
    Await.result(filter(request, next)) mustEqual responseFromNextService

    xmlCount must beNone
    jsonCount must beNone
    jsonpCount must beNone
    undefinedCount ==== Some(1d)
  }
}
