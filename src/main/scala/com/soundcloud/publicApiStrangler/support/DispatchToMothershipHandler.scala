package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.scalakit.finagle.http.{HandlerRequest, HttpHandler}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonRequest, JsonResponse}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future

import scala.collection.JavaConversions._

class DispatchToMothershipHandler(mothershipClient: Service[Request, Response]) extends HttpHandler {

  override def defaultHandling(handlerRequest: HandlerRequest): Future[Response] = {
    dispatchToMothership(handlerRequest.request)
  }

  def dispatch(request:Request) : Future[ResponseBuilder] = {
    dispatchToMothership(request).map(toResponseBuilder)
  }

  def dispatchToMothership(request:Request) : Future[Response] = {
    request.host = "api.soundcloud.com"
    mothershipClient(ForwardedRequest(request)).handle {
      case exception: Exception =>
        logger.debug("Bad response from mothership", exception)
        val response = Response()
        response.status = Status.InternalServerError
        response
    }
  }

  private def toResponseBuilder(response: Response) : ResponseBuilder = {
    val headerMap = response.headerMap.entrySet().map(entry => (entry.getKey, entry.getValue)).toMap
    new ResponseBuilder()
      .status(response.status.code)
        .body(response.getContentString())
        .headers(headerMap)
  }

}
