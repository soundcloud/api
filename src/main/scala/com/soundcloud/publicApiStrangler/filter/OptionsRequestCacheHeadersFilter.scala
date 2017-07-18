package com.soundcloud.publicApiStrangler.filter

import com.twitter.finagle.http.{Request, Response, Method}
import com.twitter.finagle.{Service, SimpleFilter}

class OptionsRequestCacheHeadersFilter extends SimpleFilter[Request, Response] {
  override def apply(request: Request, next: Service[Request, Response]) = {
    next(request).map { response => 
      if (request.method == Method.Options) {
        response.headerMap.set("Cache-Control", "public, max-age=3600")
      }
      response
    }
  }
}