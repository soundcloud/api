package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.scalakit.finagle.http.{HandlerRequest, HttpHandler}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Status, Request, Response}
import com.twitter.util.Future
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.scalakit.notifier.AirbrakeNotifier

class SpecificStranglingHandler(whereToDispatch: HttpHandler, pathsPatternsToDispatch: List[String], telemetry: Telemetry) extends HttpHandler {

  private val fallthroughCounter = telemetry.counter(
    "fallthrough_strangled_by",
    "Fallthrough requests by the path pattern that strangles them",
    "path_pattern"
  )

  override def defaultHandling(handlerRequest: HandlerRequest): Future[Response] =
    dispatchIfRecognizedPattern(handlerRequest, whereToDispatch.defaultHandling)

  private def dispatchIfRecognizedPattern(request: HandlerRequest, dispatchFun: HandlerRequest => Future[Response]): Future[Response] = {
    val strangledBy = pathsPatternsToDispatch.collectFirst {
      case path if request.request.path.matches(path) => path
    }

    logFallthroughRequest(strangledBy, request.request.path)
    strangledBy.map(_ => dispatchFun(request)).getOrElse(Future.value(error))
  }

  private lazy val error = {
    val response = Response()
    response.status = Status.InternalServerError
    response
  }

  private def logFallthroughRequest(strangledBy: Option[String], path: String): Unit = {
    val label = strangledBy.getOrElse("NOT_STRANGLED")
    fallthroughCounter.labels(label).inc()
    if(strangledBy.isEmpty)
      AirbrakeNotifier.notify(s"Got a fallthrough request to an unstrangled endpoint $path")
  }
}
