package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.ConfigComponent
import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.config.ConfigConvention
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.scalakit.finagle.http.{OutgoingHttpRequestMonitoringFilter, TracingHttp}
import com.soundcloud.scalakit.finagle.zipkin.ZipkinTracer
import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.util.TimeConversions._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

trait PublicApiClientComponent {
  this: ConfigComponent =>

  lazy val publicApiClient: Service[HttpRequest, HttpResponse] = {
    val svcName = "public-api"
    val client =
      ClientBuilder()
        .codec(TracingHttp())
        .daemon(true)
        .hostConnectionCoresize(10)
        .hostConnectionIdleTime(5.seconds)
        .hostConnectionLimit(100)
        .hostConnectionMaxIdleTime(5.seconds)
        .hostConnectionMaxLifeTime(30.seconds)
        .dest(config.get(ResourceName("MOTHERSHIP_API_SERVER"), ConfigConvention.SRV_RECORD))
        .keepAlive(true)
        .failFast(true)
        .tracer(ZipkinTracer(config))
        .retries(3)
        .name(svcName)
        .requestTimeout(30.seconds)
        .tcpConnectTimeout(5.seconds)
        .build()
    val requestLatencyBuckets = config.get(ResourceName("DEFAULT"), ConfigConvention.REQUEST_LATENCY_BUCKETS,
      Telemetry.DEFAULT_REQUEST_LATENCY_BUCKETS_SECONDS).split(",").map(_.toDouble)
    val filter =
      new OutgoingHttpRequestMonitoringFilter[HttpRequest, HttpResponse](ResourceName(svcName), new Telemetry(config),
        requestLatencyBuckets)

    filter andThen client
  }
}
