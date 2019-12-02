package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future

import scala.util.control.NonFatal

class DispatchToMothershipHandler(mothershipClient: Service[Request, Response]) {
  val logger = SoundCloudLoggerFactory.getLogger(getClass)

  def dispatch(request: HandlerRequest): Future[Response] = {
    mothershipClient(ForwardedRequest(request.request)).handle {
      case NonFatal(exception: Exception) =>
        logger.debug("Bad response from mothership", exception)
        Response(Status.InternalServerError)
    }
  }
}

object ForwardedRequest {
  val mandatoryHeaders = Map("X-Forwarded-Proto" -> "https", "Host" -> "api.soundcloud.com")

  def apply(originalRequest: Request) = {
    originalRequest.host = "api.soundcloud.com"
    addMandatoryHeaders(originalRequest)
    originalRequest
  }

  private def addMandatoryHeaders(newRequest: Request) = {
    mandatoryHeaders.foreach {
      case (k, v) => newRequest.headerMap.put(k, v)
    }
  }
}
