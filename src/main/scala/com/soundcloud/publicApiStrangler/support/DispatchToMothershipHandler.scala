package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.scalakit.finagle.http.{HandlerRequest, HttpHandler}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse, HttpResponseStatus}
import scala.collection.JavaConversions._

class DispatchToMothershipHandler(mothershipClient: Service[HttpRequest, HttpResponse]) extends HttpHandler {

  override def defaultHandling(handlerRequest: HandlerRequest): Future[Response] = {
    dispatchToMothership(handlerRequest.request)
  }

  def dispatch(request:Request) : Future[ResponseBuilder] = {
    dispatchToMothership(request).map(toResponseBuilder(_))
  }

  private def dispatchToMothership(request:Request) : Future[Response] = {
    request.host = "api.soundcloud.com"
    mothershipClient(ForwardedRequest(request)).map(Response.apply).handle {
      case exception: Exception =>
        logger.debug("Bad response from mothership", exception)
        val response = Response()
        response.status = HttpResponseStatus.INTERNAL_SERVER_ERROR
        response
    }
  }

  private def toResponseBuilder(response : Response) : ResponseBuilder = {
    val headerMap = response.headers().entries().map(entry => (entry.getKey, entry.getValue)).toMap
    new ResponseBuilder()
      .status(response.getStatus.getCode)
        .body(response.getContentString())
        .headers(headerMap)
  }

}
