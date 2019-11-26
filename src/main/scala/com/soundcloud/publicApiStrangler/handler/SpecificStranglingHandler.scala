package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest}
import com.soundcloud.jvmkit.module.telemetry.Counter
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.http.HeadersBuilder
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.module.util.session.UserSessionHeadersConverter
import com.soundcloud.publicApiStrangler.handler.SpecificStranglingHandler.externalAppUrn
import com.twitter.finagle.http.Response
import com.twitter.util.Future

import scala.util.matching.Regex

class SpecificStranglingHandler(
    whereToDispatch: Handler,
    pathsPatternsToDispatch: List[Regex],
    officialSoundCloudApps: List[Urn],
    counter: Counter
) extends Handler {
  val logger = SoundCloudLoggerFactory.getLogger(getClass)

  override def apply(request: HandlerRequest): Future[Response] = {
    logFallthroughRequest(request)
    whereToDispatch(request)
  }

  private def patternFor(request: HandlerRequest): Option[Regex] = {
    pathsPatternsToDispatch.collectFirst {
      case path if path.findFirstIn(request.request.path).isDefined => path
    }
  }

  // This won't work yet, because the request isn't populated with a user session.
  private def agentFor(request: HandlerRequest): Urn = {
    val userAgent = userAgentFrom(request)
    Option(userAgent)
      .flatMap { agent =>
        officialSoundCloudApps.collectFirst { case app if app == agent => app }
      }
      .getOrElse(externalAppUrn)
  }

  def userAgentFrom(request: HandlerRequest): Urn = {
    val builder = new HeadersBuilder()
    request.headerMap.foreach {
      case (key, value) => builder.set(key, value)
    }
    UserSessionHeadersConverter.toSession(builder.build()).getAgent
  }

  private def logFallthroughRequest(request: HandlerRequest): Unit = {
    val strangledBy = patternFor(request)
    val pathPattern = strangledBy.map(_.toString).getOrElse("UNKNOWN")
    val agent = agentFor(request)
    counter.labels(request.method.toString, pathPattern, agent.toString).inc()
    if (pathPattern == "UNKNOWN" || pathPattern == ".*") {
      logger.info(
        s"Request for unknown endpoint: pathPattern='$pathPattern', agent='$agent', " +
          s"method='${request.method.toString}', path='${request.request.path}'"
      )
    }
  }
}

object SpecificStranglingHandler {
  private final val externalAppUrn = Urn("soundcloud", "applications", "external")
}
