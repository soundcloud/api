package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.module.util.session.{UserSession, UserSessionHeadersConverter}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import scala.collection.JavaConverters._

import scala.util.control.NonFatal

class DispatchToMothershipHandler(userAuthentication: UserAuthentication, mothershipClient: Service[Request, Response]) {
  val logger = SoundCloudLoggerFactory.getLogger(getClass)

  def dispatch(request: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      mothershipClient(ForwardedRequest(request.request, session)).handle {
        case NonFatal(exception: Exception) =>
          logger.debug("Bad response from mothership", exception)
          Response(Status.InternalServerError)
      }
    }
  }
}

object ForwardedRequest {
  val mandatoryHeaders = Map("X-Forwarded-Proto" -> "https", "Host" -> "api.soundcloud.com")

  def apply(originalRequest: Request, session: UserSession) = {
    originalRequest.host = "api.soundcloud.com"
    addMandatoryHeaders(originalRequest)
    addUserSessionHeaders(originalRequest, session)
    originalRequest
  }

  private def addMandatoryHeaders(newRequest: Request) = {
    mandatoryHeaders.foreach {
      case (k, v) => newRequest.headerMap.put(k, v)
    }
  }

  private def addUserSessionHeaders(request: Request, session: UserSession): Unit = {
    UserSessionHeadersConverter.fromSession(session).entrySet().asScala.foreach { header =>
      request.headerMap.set(header.getKey, header.getValue.asScala.last)
    }
  }
}
