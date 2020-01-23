package com.soundcloud.publicApiStrangler.filter

import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finagle.http.{Method, Request, Response}

// This filter processes any incoming HEAD request to convert it into a GET response
// and removes the body message in the response
class HeadRequestFilter extends SimpleFilter[Request, Response] {
  override def apply(request: Request, service: Service[Request, Response]) = {
    request.method match {
      case Method.Head =>
        request.method = Method.Get
        service(request).map { response =>
          {
            response.setContentString("")
            response
          }
        }
      case _ => service(request)
    }
  }
}
