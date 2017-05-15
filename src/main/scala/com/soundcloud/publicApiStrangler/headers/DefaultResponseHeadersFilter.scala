package com.soundcloud.publicApiStrangler.headers

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

class DefaultResponseHeadersFilter extends SimpleFilter[Request, Response] {

  override def apply(request: Request, next: Service[Request, Response]): Future[Response] = {
    next(request).map { response =>
      DefaultResponseHeaders.defaultHeaders.collect {
        case (headerName, headerValue) if response.headerMap.get(headerName) == None =>
          response.headerMap.put(headerName, headerValue)
      }
      response
    }
  }

}
