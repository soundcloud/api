package com.soundcloud.publicApiStrangler.support

import com.soundcloud.scalakit.finagle.http.{HandlerRequest, HttpHandler}
import com.twitter.finagle.Service
import com.twitter.finagle.http.Response
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse, HttpResponseStatus}

class DispatchToMothershipHandler(mothershipClient: Service[HttpRequest, HttpResponse]) extends HttpHandler {
  override def defaultHandling(handlerRequest: HandlerRequest): Future[Response] = {
    handlerRequest.request.host = "api.soundcloud.com"
    mothershipClient(ForwardedRequest(handlerRequest.request)).map(Response.apply).handle {
      case exception: Exception =>
        val response = Response()
        response.status = HttpResponseStatus.INTERNAL_SERVER_ERROR
        response
    }
  }
}
