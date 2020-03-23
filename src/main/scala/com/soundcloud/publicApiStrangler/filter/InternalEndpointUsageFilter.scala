package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, HandlerRouter}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

/**
  * Instrument usage of internal and experimental endpoints
  */
class InternalEndpointUsageFilter(userAuthentication: UserAuthentication, telemetry: Telemetry, router: HandlerRouter)
    extends SimpleFilter[Request, Response] {
  private val internalRequestCounter = telemetry.counter(
    "internal_endpoint_requests_total",
    "Number of requests to internal endpoints by client application",
    "appid",
    "path",
    "method"
  )

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val path = router.pathMatching(request).rawPattern

    if (path.startsWith("/e1/") || path.startsWith("/i1/")) {
      userAuthentication.withUserSession(HandlerRequest(request)) { userSession =>
        val clientAppId = Option(userSession.getAgent).map(_.identifier).getOrElse("unknown")
        val method = request.method.toString

        internalRequestCounter.labels(clientAppId, path, method).inc()
        service(request)
      }
    } else {
      service(request)
    }
  }
}
