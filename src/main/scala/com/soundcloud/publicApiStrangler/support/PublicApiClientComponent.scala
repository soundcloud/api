package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.ConfigComponent
import com.soundcloud.scalakit.finagle.http.TracingHttp
import com.soundcloud.scalakit.finagle.zipkin.ZipkinTracer
import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.util.TimeConversions._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

trait PublicApiClientComponent {
  this: ConfigComponent =>

  lazy val publicApiClient: Service[HttpRequest, HttpResponse] = {
    val svcName = "public-api"
    ClientBuilder()
      .codec(TracingHttp())
      .daemon(true)
      .hostConnectionCoresize(10)
      .hostConnectionIdleTime(5.seconds)
      .hostConnectionLimit(100)
      .hostConnectionMaxIdleTime(5.seconds)
      .hostConnectionMaxLifeTime(30.seconds)
      .dest(config.get("MOTHERSHIP_API_SERVER"))
      .keepAlive(true)
      .failFast(true)
      .tracer(ZipkinTracer(config))
      .retries(3)
      .name(svcName)
      .requestTimeout(30.seconds)
      .tcpConnectTimeout(5.seconds)
      .build()
  }
}
