package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.jvmkit.module.http.server.HandlerRouter
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.twitter.finagle.http._
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

class ContentTypeTelemetryFilter(telemetry: Telemetry, router: HandlerRouter) extends SimpleFilter[Request, Response] {
  private val counter = telemetry.counter(
    "incoming_http_requests_content_type_total",
    "Number of incoming HTTP requests by content-type",
    "method",
    "path",
    "content_type"
  )

  override def apply(request: Request, next: Service[Request, Response]): Future[Response] = {
    val path = router.pathMatching(request).rawPattern

    counter
      .labels(
        request.method.name,
        path,
        request.contentType.map(_.split(';').head).getOrElse("(none)")
      )
      .inc()

    next(request)
  }
}
