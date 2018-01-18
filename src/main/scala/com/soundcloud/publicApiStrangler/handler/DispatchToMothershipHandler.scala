package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest}
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.module.util.session.{UserSession, UserSessionHeadersConverter}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future

class DispatchToMothershipHandler(userAuthentication: UserAuthentication,
                                  mothershipClient: Service[Request, Response],
                                  newMothershipClient: Service[Request, Response],
                                  checkRollout: () => Future[Boolean]) extends Handler {

  val logger = SoundCloudLoggerFactory.getLogger(getClass)

  override def apply(request: HandlerRequest): Future[Response] = dispatchToMothership(request)

  def dispatch(request: Request): Future[Response] = {
    dispatchToMothership(HandlerRequest(request))
  }

  def dispatchToMothership(request: HandlerRequest): Future[Response] = {
    request.host = "api.soundcloud.com"

    checkRollout().flatMap { useNewClient =>
      val client = if (useNewClient) {
        newMothershipClient
      } else {
        mothershipClient
      }

      userAuthentication.withUserSession(HandlerRequest(request)) { (userSession) =>
        client(ForwardedRequest(request.request, userSession)).handle {
          case exception: Exception =>
            logger.debug("Bad response from mothership", exception)
            val response = Response()
            response.status = Status.InternalServerError
            response
        }
      }
    }
  }
}

object ForwardedRequest {
  import collection.JavaConverters._

  protected val mandatoryHeaders = Map("X-Forwarded-Proto" -> "https", "Host" -> "api.soundcloud.com")

  def apply(originalRequest: Request, session: UserSession): Request = {
    val sessionHeaders = UserSessionHeadersConverter.fromSession(session).headers.asScala

    mandatoryHeaders.foreach { case (k, v) => originalRequest.headerMap.put(k, v) }

    sessionHeaders.foreach { case (key, values) =>
      values.asScala.foreach(value => originalRequest.headerMap.add(key, value))
    }

    originalRequest
  }
}