package com.soundcloud.apipublic.filter

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, HandlerRouter}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

class RequestTelemetryFilter(userAuthentication: UserAuthentication, telemetry: Telemetry, router: HandlerRouter)
    extends SimpleFilter[Request, Response] {

  private val contentTypeCounter = telemetry.counter(
    "incoming_http_requests_content_type_total",
    "Number of incoming HTTP requests by content-type",
    "method",
    "path",
    "content_type"
  )

  private val accessParamCounter = telemetry.counter(
    "incoming_http_requests_with_access_param_total",
    "Number of incoming HTTP requests with access parameter present by application id",
    "path",
    "appid"
  )

  override def apply(request: Request, next: Service[Request, Response]): Future[Response] = {
    applyContentTypeFilter(request)

    if (isAccessPresent(request)) {
      userAuthentication.withUserSession(HandlerRequest(request)) { userSession =>
        val clientAppId = Option(userSession.getAgent).map(_.identifier).getOrElse("unknown")

        accessParamCounter.labels(path(request), clientAppId).inc()
        next(request)
      }
    }
    next(request)
  }

  private def applyContentTypeFilter(request: Request): Unit = {
    contentTypeCounter
      .labels(
        request.method.name,
        path(request),
        request.contentType.map(_.split(';').head).getOrElse("(none)")
      )
      .inc()
  }

  private def path(request: Request): String = router.pathMatching(request).rawPattern

  private def isAccessPresent(request: Request): Boolean = request.params.keys.exists(_.contains("access"))
}
