package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

class ContentAuthorizationFilter(authorizeContent: AuthorizeHttpResponse) extends SimpleFilter[Request, Response] {
  override def apply(request: Request, next: Service[Request, Response]) = {
    val req = HandlerRequest(request)
    for {
      response <- next(req)
      modifiedResponse <- authorize(req, response)
    } yield modifiedResponse
  }

  private def authorize(request: HandlerRequest, originalResponse: Response): Future[Response] =
    authorizeContent(request, originalResponse).map { authorizationResponse =>
      ResponseBuilder(
        authorizationResponse.status,
        authorizationResponse.contentString,
        headers = originalResponse.headerMap.toMap
      ).build
    }
}
