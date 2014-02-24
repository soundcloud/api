package com.soudcloud.publicApiStrangler

import com.soundcloud.scalakit.framework.{StatsComponent, ScAppComponent}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Response, Request}
import com.twitter.finagle.builder.ClientBuilder
import com.soundcloud.scalakit.finagle.http.{MonitoringFilter, TracingHttp}
import com.soundcloud.scalakit.finagle.zipkin.ZipkinTracer
import com.twitter.util.TimeConversions._

trait PublicApiClientComponent {
  self: ScAppComponent with StatsComponent =>

  val publicApiClient: Service[Request, Response] = {
    val svcName = "public-api"
    val httpClient = ClientBuilder()
      .codec(TracingHttp())
      .daemon(true)
      .hostConnectionCoresize(10)
      .hostConnectionIdleTime(5.seconds)
      .hostConnectionLimit(100)
      .hostConnectionMaxIdleTime(5.seconds)
      .hostConnectionMaxLifeTime(30.seconds)
      .hosts(config.get("MOTHERSHIP_API_SERVER"))
      .keepAlive(true)
      .failFast(false)
      .tracer(ZipkinTracer(config))
      .retries(3)
      .name(svcName)
      .requestTimeout(5.seconds)
      .tcpConnectTimeout(5.seconds)
      .build()

    new MonitoringFilter[Request, Response](svcName, metrics) andThen httpClient
  }
}
