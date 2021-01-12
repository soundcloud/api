package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.jvmkit.module.http.server.HandlerRouter
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.support.ErrorResponse
import com.twitter.finagle.http._
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

class ErrorResponseTypeFilter(telemetry: Telemetry, router: HandlerRouter) extends SimpleFilter[Request, Response] {
  val emptyBodyErrorResponseCounter =
    telemetry.counter(
      "empty_body_error_response_counter",
      "Counter for all error responses that lacked a body, before reaching our filters",
      "path",
      "statusCode"
    )

  def isEmpty(body: String) = body.isEmpty || body == "{}"

  override def apply(request: Request, next: Service[Request, Response]): Future[Response] = {
    val path = router.pathMatching(request).rawPattern
    next(request)
      .map { response =>
        if (response.statusCode >= 400 && isEmpty(response.contentString)) {
          emptyBodyErrorResponseCounter.labels(path, response.statusCode.toString).inc()
          ErrorResponse(response.status)
        } else response
      }
  }
}
