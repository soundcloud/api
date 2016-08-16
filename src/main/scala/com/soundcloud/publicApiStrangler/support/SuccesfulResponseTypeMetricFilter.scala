package com.soundcloud.publicApiStrangler.support

import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.soundcloud.scalakit.json.Json
import com.twitter.finagle.http._
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Try

class SuccesfulResponseTypeMetricFilter(telemetry: Telemetry) extends SimpleFilter[Request, RouterResponse] {

  val successfulResponseTypeCounter = telemetry.counter(
    "successful_response_type_counter",
    "Counter for successful responses",
    "type")

  override def apply(request: Request, next: Service[Request, RouterResponse]) = {
    next(request).map {
      case response@RouterResponse(underlying, _) =>
        if (underlying.status == Status.Ok) {
          if (isXmlResponse(underlying)) {
            successfulResponseTypeCounter.labels("xml").inc()
          } else if (isJsonpResponse(underlying)) {
            successfulResponseTypeCounter.labels("jsonp").inc()
          } else if (isJsonResponse(underlying)) {
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
    Try(Json.fromString(underlying.contentString)).toOption.isDefined
  }
}
