package com.soudcloud.publicApiStrangler

import com.soundcloud.scalakit.finagle.http.{HandlerRequest, HttpHandler}
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}
import com.twitter.finagle.http.Response
import com.twitter.finagle.Service
import org.jboss.netty.handler.codec.http.DefaultHttpRequest
import scala.collection.JavaConversions._

class DispatchToMothershipHandler(mothershipClient: Service[HttpRequest, HttpResponse]) extends HttpHandler {
  override def defaultHandling(handlerRequest: HandlerRequest): Future[Response] = {
    handlerRequest.request.host = "api.soundcloud.com"
    mothershipClient(ForwardedRequest(handlerRequest.request)).map(Response.apply)
  }
}
