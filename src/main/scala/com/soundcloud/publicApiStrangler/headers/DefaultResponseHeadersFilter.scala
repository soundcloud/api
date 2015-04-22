package com.soundcloud.publicApiStrangler.headers

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.Future

class DefaultResponseHeadersFilter extends Filter[Request, Response, Request, Response] {

  override def apply(request: Request, next: Service[Request, Response]): Future[Response] = {
    next(request).map { response =>
      DefaultResponseHeaders.defaultHeaders.foreach {
        case (headerName, headerValue) => response.headers().add(headerName, headerValue)
      }
      response
    }
  }

}
