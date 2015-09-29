package com.soundcloud.publicApiStrangler.headers

import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.twitter.finagle.http.Request
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

class DefaultResponseHeadersFilter extends SimpleFilter[Request, RouterResponse] {

  override def apply(request: Request, next: Service[Request, RouterResponse]): Future[RouterResponse] = {
    next(request).map { response =>
      DefaultResponseHeaders.defaultHeaders.collect {
        case (headerName, headerValue) if response.headers().get(headerName) == null =>
          response.headers().add(headerName, headerValue)
      }
      response
    }
  }

}
