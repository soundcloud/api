package com.soundcloud.publicApiStrangler.filter

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

/**
  * Default headers returned by public api.
  */
object DefaultResponseHeaders {

  val defaultHeaders = Map(
    "Access-Control-Allow-Headers" -> "Accept, Authorization, Content-Type, Origin",
    "Access-Control-Allow-Methods" -> "GET, PUT, POST, DELETE",
    "Access-Control-Allow-Origin" -> "*",
    "Access-Control-Expose-Headers" -> "Date",
    "Cache-Control" -> "private, max-age=0, must-revalidate")

}
