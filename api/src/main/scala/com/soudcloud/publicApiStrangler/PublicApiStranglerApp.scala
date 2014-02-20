package com.soudcloud.publicApiStrangler

import com.soundcloud.scalakit.framework.FinagleBasedServer
import com.soundcloud.jvmkit.config.BazookaConfig
import com.soundcloud.scalakit.finagle.http.{MonitoringFilter, TracingHttp, HttpServer}
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.util.TimeConversions._
import com.soundcloud.scalakit.finagle.zipkin.ZipkinTracer
import com.twitter.finagle.http.{Request, Response}

object PublicApiStranglerApp extends FinagleBasedServer {
  override def config = new BazookaConfig

  override def createRoutes(httpServer: HttpServer): Unit = {
    val mothershipClient = {
      val svcName = "public-api"
      val httpClient = ClientBuilder()
        .codec(TracingHttp())
        .daemon(true)
        .hostConnectionCoresize(10)
        .hostConnectionIdleTime(5.seconds)
        .hostConnectionLimit(100)
        .hostConnectionMaxIdleTime(5.seconds)
        .hostConnectionMaxLifeTime(30.seconds)
        .hosts("api-lb.r.int.s-cloud.net:80")
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

    val dispatchToPublicApiHandler = new DispatchToMothershipHandler(mothershipClient)

    httpServer.register("/banana", dispatchToPublicApiHandler)
    httpServer.registerFallback(dispatchToPublicApiHandler)
  }
}
