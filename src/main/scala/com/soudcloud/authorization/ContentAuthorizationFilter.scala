package com.soudcloud.authorization

import com.soundcloud.scalakit.UTF8
import java.nio.charset.Charset

import scala.collection.JavaConversions.iterableAsScalaIterable

import com.soundcloud.bff.finagle.{Request => BffRequest}
import com.soundcloud.scalakit.finagle.http.HandlerRequest
import com.twitter.finagle.Service
import com.twitter.finagle.SimpleFilter
import com.twitter.finagle.http.{ Response => FinagleResponse }
import com.twitter.util.Future

class ContentAuthorizationFilter(
  authorizeContent: AuthorizeContent)
  extends SimpleFilter[HandlerRequest, FinagleResponse] {

  override def apply(request: HandlerRequest, next: Service[HandlerRequest, FinagleResponse]) =
    next(request).flatMap(authorize(request, _))

  private def authorize(request: HandlerRequest, response: FinagleResponse) =
    authorizeContent(new BffRequest(request.request), response.statusCode, body(response)).map { render =>
      render.headers(headersMap(response)).build
    }

  private def body(response: FinagleResponse) =
    response.getContent.toString(UTF8)

  private def headersMap(response: FinagleResponse) =
    response.headers.map(e => e.getKey -> e.getValue).toMap
}
