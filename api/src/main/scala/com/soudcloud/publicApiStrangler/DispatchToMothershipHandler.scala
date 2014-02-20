package com.soudcloud.publicApiStrangler

import com.soundcloud.scalakit.finagle.http.{HandlerRequest, HttpHandler}
import com.twitter.util.Future
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.Service
import org.jboss.netty.handler.codec.http.DefaultHttpRequest
import scala.collection.JavaConversions._

class DispatchToMothershipHandler(mothershipClient: Service[Request, Response]) extends HttpHandler {
  override def defaultHandling(handlerRequest: HandlerRequest): Future[Response] = {
    mothershipClient(ForwardedRequest(handlerRequest.request))
  }
}
