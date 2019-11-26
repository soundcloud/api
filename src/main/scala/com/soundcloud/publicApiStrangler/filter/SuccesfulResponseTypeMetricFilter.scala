package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.twitter.finagle.http._
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Try
import play.api.libs.json.Json

class SuccesfulResponseTypeMetricFilter(telemetry: Telemetry) extends SimpleFilter[Request, Response] {
  val successfulResponseTypeCounter =
    telemetry.counter("successful_response_type_counter", "Counter for successful responses", "type")

  override def apply(request: Request, next: Service[Request, Response]) = {
    next(request).map { response =>
      if (response.status == Status.Ok) {
        if (isXmlResponse(response)) {
          successfulResponseTypeCounter.labels("xml").inc()
        } else if (isJsonpResponse(response)) {
          successfulResponseTypeCounter.labels("jsonp").inc()
        } else if (isJsonResponse(response)) {
          successfulResponseTypeCounter.labels("json").inc()
        } else {
          successfulResponseTypeCounter.labels("undefined").inc()
        }
      }
      response
    }
  }

  private def isXmlResponse(underlying: Response): Boolean = {
    underlying.contentString.startsWith("<?xml ")
  }

  private def isJsonpResponse(underlying: Response): Boolean = {
    underlying.contentString.startsWith("""/**/""")
  }

  private def isJsonResponse(underlying: Response): Boolean = {
    Try(Json.parse(underlying.contentString)).toOption.isDefined
  }
}
