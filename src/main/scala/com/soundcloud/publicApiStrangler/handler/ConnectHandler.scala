package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future

class ConnectHandler(userAuthentication: UserAuthentication, dispatchToMothershipHandler: DispatchToMothershipHandler) {
  private val connectBaseUrl = "https://secure.soundcloud.com/connect"
  private val deckApplication = Urn("soundcloud", "applications", "5283")

  def connect(request: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      session.agent match {
        case Some(urn) if urn == deckApplication => handleConnectInPAS(request)
        case _ => dispatchToMothershipHandler.dispatch(request)
      }
    }
  }

  private def handleConnectInPAS(request: HandlerRequest): Future[Response] = {
    val url = s"$connectBaseUrl${request.params.toString()}"
    val response =
      JsonResponseBuilder(
        status = Status.Found,
        body = body(url),
        headers = Map("Location" -> url)
      ).build
    Future.value(response)
  }

  private def body(url: String) = {
    s"""<html><body>You are being <a href="$url">redirected</a>.</body></html>"""
  }
}
