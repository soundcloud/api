package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future


class ContentAuthorizationFilter(authorizeContent: AuthorizeHttpResponse)
  extends SimpleFilter[Request, Response] {

  override def apply(request: Request, next: Service[Request, Response]) = {
    val req = HandlerRequest(request)
    for {
      response <- next(req)
      modifiedResponse <- authorize(req, response)
    } yield modifiedResponse
  }

  private def authorize(request: HandlerRequest, response: Response): Future[Response] =
    authorizeContent(request, response.status, response.contentString).map { authorizationResponse =>
      response.headerMap.foreach { case (key, value) => authorizationResponse.headerMap.set(key, value) }
      authorizationResponse
    }
}
