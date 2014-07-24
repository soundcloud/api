package com.soudcloud.publicApiStrangler

import com.soundcloud.scalakit.finagle.http.{HandlerRequest, HttpHandler}
import com.twitter.finagle.http.Response
import com.twitter.util.Future

class HealthCheckHandler extends  HttpHandler {
  override def defaultHandling(handlerRequest: HandlerRequest): Future[Response] = {
    render.futureJson("OK")
  }
}




