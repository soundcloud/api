package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.ConfigComponent
import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.config.ConfigConvention
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.scalakit.finagle.{FollowRedirectsFilter, CircuitBreakerFilter}
import com.soundcloud.scalakit.finagle.http.{OutgoingHttpRequestMonitoringFilter}
import com.soundcloud.scalakit.finagle.telemetry.SoundCloudStatsReceiver
import com.soundcloud.scalakit.finagle.zipkin.ZipkinTracer
import com.twitter.finagle.service.FailFastFactory.FailFast
import com.twitter.finagle.stats.NullStatsReceiver
import com.twitter.finagle.{param, Http, Service}
import com.twitter.finagle.client.{Transporter, DefaultPool}
import com.twitter.finagle.http.{Response, Request}
import com.twitter.finagle.param.HighResTimer
import com.twitter.finagle.service._
import com.twitter.finagle.service.RetryPolicy.RetryableWriteException
import com.twitter.finagle.transport.Transport
import com.twitter.finagle.util.DefaultTimer
import com.twitter.util.{Try, Duration, Throw}
import com.twitter.util.TimeConversions._

trait PublicApiClientComponent {
  this: ConfigComponent =>

  lazy val publicApiClient: Service[Request, Response] = {
    val svcName = "public-api"
    val telemetry = new Telemetry(config)
    val tracer = ZipkinTracer(config)
    val timer = DefaultTimer.twitter
    val statsReceiver = new NullStatsReceiver

    val connectionParams =
      DefaultPool.Param(
        low = 10,
        high = 100,
        idleTime = 5.seconds,
        bufferSize = 0,
        maxWaiters = 1000
      )

    val expirationParams =
      ExpiringService.Param(
        idleTime = 5.seconds,
        lifeTime = 30.seconds
      )

    val keepaliveParams =
      Transport.Liveness(
        readTimeout = Duration.Top,
        writeTimeout = Duration.Top,
        keepAlive = Some(true)
      )

    // Retry filter
    val writeExceptions: PartialFunction[(Request, Try[Response]), Boolean] = {
      case (_, Throw(RetryableWriteException(_))) => true
    }
    val highResTimer = HighResTimer.Default
    val retryBudget =
      RetryBudget(
        ttl = 60.seconds,
        minRetriesPerSec = 10,
        percentCanRetry = 0.2
      )
    val retryFilter =
      new RetryFilter[Request, Response](
        RetryPolicy.tries(3, writeExceptions),
        highResTimer,
        statsReceiver,
        retryBudget
      )

    val requestLatencyBuckets = config.get(ResourceName("DEFAULT"), ConfigConvention.REQUEST_LATENCY_BUCKETS,
          Telemetry.DEFAULT_REQUEST_LATENCY_BUCKETS_SECONDS).split(",").map(_.toDouble)

    val client: Service[Request, Response] =
      Http
        .client
        .withStreaming(config.get(ResourceName("MOTHERSHIP_API_STREAMING"), ConfigConvention.ENABLED).toBoolean)
        .configured(param.Label(svcName))
        .configured(param.Timer(timer))
        .configured(param.Stats(statsReceiver))
        .configured(param.Tracer(tracer))
        .configured(connectionParams)
        .configured(expirationParams)
        .configured(keepaliveParams)
        .configured(TimeoutFilter.Param(config.get(ResourceName("MOTHERSHIP_API"), ConfigConvention.REQUEST_TIMEOUT_MILLIS).toInt.milliseconds))
        .configured(Transporter.ConnectTimeout(5.seconds))
        .configured(FailFast(true))
        .filtered(retryFilter)
        .filtered(new OutgoingHttpRequestMonitoringFilter[Request, Response](ResourceName(svcName), telemetry, requestLatencyBuckets))
        .newService(config.get(ResourceName("MOTHERSHIP_API_SERVER"), ConfigConvention.SRV_RECORD))

    client

  }
}
