package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.scalakit.finagle.http.{HandlerRequest, HttpHandler}
import com.soundcloud.scalakit.Urn
import com.twitter.finagle.Service
import com.twitter.finagle.http.Response
import com.twitter.util.Future
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.scalakit.notifier.AirbrakeNotifier

class SpecificStranglingHandler(whereToDispatch: HttpHandler, pathsPatternsToDispatch: List[String], officialSoundCloudApps: List[Urn], telemetry: Telemetry) extends HttpHandler {

  private val fallthroughCounter = telemetry.counter(
    "fallthrough_strangled_by",
    "Fallthrough requests by the path pattern that strangles them",
    "path_pattern",
    "agent_urn"
  )

  override def defaultHandling(handlerRequest: HandlerRequest): Future[Response] =
    dispatchIfRecognizedPattern(handlerRequest, whereToDispatch.defaultHandling)

  private def dispatchIfRecognizedPattern(request: HandlerRequest, dispatchFun: HandlerRequest => Future[Response]): Future[Response] = {
    val strangledBy = pathsPatternsToDispatch.collectFirst {
      case path if request.request.path.matches(path) => path
    }
    val agent = Option(request.userSession.getAgent)

    logFallthroughRequest(strangledBy, agent, request.request.path)
    dispatchFun(request)
  }

  private def logFallthroughRequest(strangledBy: Option[String], agent: Option[Urn], path: String): Unit = {
    val label = strangledBy.getOrElse("NOT_STRANGLED")
    val agentUrn = agent.flatMap { agent =>
      officialSoundCloudApps.collectFirst { case app if app == agent => app }
    }.getOrElse(Urn("soundcloud:applications:external"))

    fallthroughCounter.labels(label, agentUrn.getString).inc()

    if(strangledBy.isEmpty)
      AirbrakeNotifier.notify(s"Got a fallthrough request to an unstrangled endpoint $path from ${agentUrn.getString}")
  }
}
