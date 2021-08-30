package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future

class ConnectHandler() {
  private val connectBaseUrl = "https://secure.soundcloud.com/connect"

  def connect(request: HandlerRequest): Future[Response] = {
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
