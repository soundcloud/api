package com.soundcloud.publicApiStrangler.filter

import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finagle.http.{Method, Request, Response}

// This filter processes any incoming HEAD request to remove the body message in the response
class HeadRequestFilter extends SimpleFilter[Request, Response] {
  override def apply(request: Request, service: Service[Request, Response]) = {
    request.method match {
      case Method.Head =>
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
