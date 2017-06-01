package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest}
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future

class DispatchToMothershipHandler(mothershipClient: Service[Request, Response]) extends Handler {

  val logger = SoundCloudLoggerFactory.getLogger(getClass)

  override def apply(request: HandlerRequest): Future[Response] = dispatchToMothership(request)

  def dispatchToMothership(handlerRequest: HandlerRequest): Future[Response] = {
    dispatchToMothership(handlerRequest.request)
  }

  def dispatch(request: Request): Future[Response] = {
    dispatchToMothership(request)
  }

  def dispatchToMothership(request: Request): Future[Response] = {
    request.host = "api.soundcloud.com"
    mothershipClient(ForwardedRequest(request)).handle {
      case exception: Exception =>
        logger.debug("Bad response from mothership", exception)
        val response = Response()
        response.status = Status.InternalServerError
        response
    }
  }

}

object ForwardedRequest {
  val mandatoryHeaders = Map("X-Forwarded-Proto" -> "https", "Host" -> "api.soundcloud.com")

  def apply(originalRequest: Request) = {
    addMandatoryHeaders(originalRequest)
    originalRequest
  }

  private def addMandatoryHeaders(newRequest: Request) = {
    mandatoryHeaders.foreach {
      case (k, v) => newRequest.headerMap.put(k, v)
    }
  }

}
