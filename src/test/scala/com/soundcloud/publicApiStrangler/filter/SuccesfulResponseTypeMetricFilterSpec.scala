package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.jvmkit.module.http.server.{JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.{MetricsRegistryImpl, Telemetry}
import com.soundcloud.jvmkit.module.util.config.InMemoryConfig
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.Service
import com.twitter.finagle.http._
import com.twitter.util.{Await, Future}
import io.prometheus.client.CollectorRegistry
import org.mockito.Mockito.when

class SuccesfulResponseTypeMetricFilterSpec extends UnitSpecification {

  trait Context extends Scope {
    val next = mock[Service[Request, Response]]
    val config = new InMemoryConfig
    val collectorRegistry = new CollectorRegistry
    val telemetry = new Telemetry(config.getApplicationName, new MetricsRegistryImpl(collectorRegistry))
    val filter = new SuccesfulResponseTypeMetricFilter(telemetry)

    def xmlCount = collectorRegistry.getSampleValue(
      "successful_response_type_counter",
      Array("type", "system"),
      Array("xml", "TEST-APP"))

    def jsonCount = collectorRegistry.getSampleValue(
      "successful_response_type_counter",
      Array("type", "system"),
      Array("json", "TEST-APP"))

    def jsonpCount = collectorRegistry.getSampleValue(
      "successful_response_type_counter",
      Array("type", "system"),
      Array("jsonp", "TEST-APP"))

    def undefinedCount = collectorRegistry.getSampleValue(
      "successful_response_type_counter",
      Array("type", "system"),
      Array("undefined", "TEST-APP"))
  }

  "Doesn't produce any metrics for non 2XX responses" in new Context {
    val request = Request()
    val responseFromNextService = ResponseBuilder.notFound()

    when(next.apply(request)).thenReturn(Future.value(responseFromNextService))
    Await.result(filter(request, next)) mustEqual responseFromNextService

    xmlCount must beNull
    jsonCount must beNull
    jsonpCount must beNull
    undefinedCount must beNull
  }

  "Increases xml counter on xml responses" in new Context {
    val request = Request()
    val content = """<?xml version="1.0" encoding="UTF-8"?><track><kind>track</kind></track>"""
    val responseFromNextService = JsonResponseBuilder.ok(content)

    when(next.apply(request)).thenReturn(Future.value(responseFromNextService))
    Await.result(filter(request, next)) mustEqual responseFromNextService

    xmlCount ==== 1d
    jsonCount must beNull
    jsonpCount must beNull
    undefinedCount must beNull
  }

  "Increases json counter on json responses" in new Context {
    val request = Request()
    val content = """{"kind":"track","id":278030262,"user_id":165217281}"""
    val responseFromNextService = JsonResponseBuilder.ok(content)

    when(next.apply(request)).thenReturn(Future.value(responseFromNextService))
    Await.result(filter(request, next)) mustEqual responseFromNextService

    xmlCount must beNull
    jsonCount ==== 1d
    jsonpCount must beNull
    undefinedCount must beNull
  }

  "Increases jsonp counter on jsonp responses" in new Context {
    val request = Request()
    val content = """/**/__jp6({"kind":"track","id":240934948});"""
    val responseFromNextService = JsonResponseBuilder.ok(content)

    when(next.apply(request)).thenReturn(Future.value(responseFromNextService))
    Await.result(filter(request, next)) mustEqual responseFromNextService

    xmlCount must beNull
    jsonCount must beNull
    jsonpCount ==== 1d
    undefinedCount must beNull
  }

  "Increases undefined counter when response can not be identified" in new Context {
    val request = Request()
    val content = """neither"""
    val responseFromNextService = JsonResponseBuilder.ok(content)

    when(next.apply(request)).thenReturn(Future.value(responseFromNextService))
    Await.result(filter(request, next)) mustEqual responseFromNextService

    xmlCount must beNull
    jsonCount must beNull
    jsonpCount must beNull
    undefinedCount ==== 1d
  }
}
