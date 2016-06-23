package com.soundcloud.publicApiStrangler.support

import com.soundcloud.jvmkit.telemetry.Counter
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.finagle.http.{HandlerRequest, HttpHandler}
import com.soundcloud.scalakit.notifier.AirbrakeNotifier
import com.twitter.finagle.http.Response
import com.twitter.finagle.http.Status.{Informational, Redirection, Successful}
import com.twitter.util.Future

import scala.util.matching.Regex

class SpecificStranglingHandler(whereToDispatch: HttpHandler, pathsPatternsToDispatch: List[Regex],
                                officialSoundCloudApps: List[Urn], counter: Counter) extends HttpHandler {

  override def defaultHandling(request: HandlerRequest): Future[Response] = {
    logFallthroughRequest(request)
    whereToDispatch.defaultHandling(request).onSuccess {
      response =>

        // let's see what paths we are serving but not explicitly strangling
        if (patternFor(request).isEmpty) {
          response.status match {
            case Informational(_) | Successful(_) | Redirection(_) =>
              val message = s"Unstrangled working endpoint: ${request.request.path}"
              AirbrakeNotifier.notify(request.request, response.getStatusCode, message)
          }
        }
    }
  }

  private def patternFor(request: HandlerRequest): Option[Regex] = {
    pathsPatternsToDispatch.collectFirst {
      case path if path.findFirstIn(request.request.path).isDefined => path
    }
  }

  // This won't work yet, because the request isn't populated with a user session.
  private def agentFor(request: HandlerRequest): Urn = {
    Option(request.userSession.getAgent).flatMap { agent =>
      officialSoundCloudApps.collectFirst { case app if app == agent => app }
    }.getOrElse(Urn("soundcloud:applications:external"))
  }

  private def logFallthroughRequest(request: HandlerRequest): Unit = {
    val strangledBy = patternFor(request)
    val pathPattern = strangledBy.map(_.toString).getOrElse("UNKNOWN")
    val agent = agentFor(request)
    counter.labels(request.method.toString, pathPattern, agent.getString).inc()
  }
}
