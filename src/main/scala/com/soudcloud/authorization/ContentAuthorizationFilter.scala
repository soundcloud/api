package com.soudcloud.authorization

import com.soundcloud.bff.finagle.{Request => BffRequest}
import com.soundcloud.scalakit.UTF8
import com.soundcloud.scalakit.finagle.http.HandlerRequest
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finagle.http.{Response => FinagleResponse}

import scala.collection.JavaConversions.iterableAsScalaIterable

class ContentAuthorizationFilter(authorizeContent: AuthorizeHttpResponse) extends SimpleFilter[HandlerRequest, FinagleResponse] {

  override def apply(request: HandlerRequest, next: Service[HandlerRequest, FinagleResponse]) =
    for {
      response <- next(request)
      modifiedResponse <- authorize(request, response)
    } yield modifiedResponse

  private def authorize(request: HandlerRequest, response: FinagleResponse) =
    authorizeContent(new BffRequest(request.request), response.statusCode, body(response)).map { render =>
      render.headers(headersMap(response)).build
    }

  private def body(response: FinagleResponse) =
    response.getContent.toString(UTF8)

  private def headersMap(response: FinagleResponse) =
    response.headers.map(e => e.getKey -> e.getValue).toMap
}
