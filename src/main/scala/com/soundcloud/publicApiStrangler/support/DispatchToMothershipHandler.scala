package com.soundcloud.publicApiStrangler.support

import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest => ModulesHandlerRequest}
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future

class DispatchToMothershipHandler(mothershipClient: Service[Request, Response]) extends Handler {

  val logger = SoundCloudLoggerFactory.getLogger(getClass)

  override def apply(request: ModulesHandlerRequest): Future[Response] = dispatchToMothership(request)

  def dispatchToMothership(handlerRequest: ModulesHandlerRequest): Future[Response] = {
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
