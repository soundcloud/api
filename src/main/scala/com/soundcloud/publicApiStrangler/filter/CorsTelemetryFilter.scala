package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.jvmkit.module.http.server.HandlerRouter
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util._

/**
  * Adds telemetry for HTTP requests including the Origin HTTP header.
  *
  * This allows us to inspect direct in-browser API usage and make improvements to our CORS policies.
  */
class CorsTelemetryFilter(telemetry: Telemetry, router: HandlerRouter) extends SimpleFilter[Request, Response] {
  private val requestCount = telemetry.counter(
    "incoming_http_requests_cors_origin_total",
    "A counter for the total number of HTTP requests grouped by CORS origin",
    "method",
    "path",
    "status",
    "origin"
  )

  def apply(request: Request, svc: Service[Request, Response]): Future[Response] = {
    svc(request) onSuccess { response =>
      log(request, response)
    }
  }

  private def log(req: Request, resp: Response): Unit = {
    val path = router.pathMatching(req).rawPattern
    val origin = req.headerMap.getOrElse("Origin", "")

    requestCount
      .labels(
        req.method.toString,
        path,
        resp.status.code.toString,
        origin
      )
      .inc()
  }
}
