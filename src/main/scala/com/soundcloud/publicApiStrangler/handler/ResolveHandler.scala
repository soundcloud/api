package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.publicApiStrangler.service.resolve.ResolveService
import com.soundcloud.publicApiStrangler.support.ErrorResponse
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future

class ResolveHandler(
    userAuthentication: UserAuthentication,
    resolveService: ResolveService
) {
  def resolve(request: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      val maybeUrl = Option(request.getParam("url"))
        .orElse(Option(request.getParam("permalink_url"))) // backwards-compatibility
      maybeUrl match {
        case Some(url) =>
          resolveService.resolveUrl(session, url).map {
            case Some(url) =>
              JsonResponseBuilder(
                status = Status.Found,
                body = responseBody(url),
                headers = Map("Location" -> url)
              ).build
            case None => ErrorResponse.notFound("404 - Not Found")
          }
        case None => Future.value(JsonResponseBuilder.badRequest())
      }
    }
  }

  private def responseBody(url: String) = s"""{"status":"302 - Found", "location":"$url"}"""
}
