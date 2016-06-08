package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.scalakit.finagle.http.{HandlerRequest, HttpHandler}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Status, Request, Response}
import com.twitter.util.Future
import scala.collection.JavaConversions._

class SpecificStranglingHandler(whereToDispatch: HttpHandler, pathsPatternsToDispatch: List[String]) extends HttpHandler {
  override def defaultHandling(handlerRequest: HandlerRequest): Future[Response] =
    dispatchIfRecognizedPattern(handlerRequest, whereToDispatch.defaultHandling)

  private def dispatchIfRecognizedPattern(request: HandlerRequest, dispatchFun: HandlerRequest => Future[Response]): Future[Response] =
    if(pathsPatternsToDispatch.exists(request.request.path.matches)) dispatchFun(request)
    else Future.value(error)

  private lazy val error = {
    val response = Response()
    response.status = Status.InternalServerError
    response
  }
}
