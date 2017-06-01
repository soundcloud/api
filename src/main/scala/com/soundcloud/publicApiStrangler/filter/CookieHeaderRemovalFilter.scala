package com.soundcloud.publicApiStrangler.filter

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}

class CookieHeaderRemovalFilter extends SimpleFilter[Request, Response] {
  override def apply(request: Request, next: Service[Request, Response]) = {
    for (key <- request.headerMap.keySet) {
      if (key.equalsIgnoreCase("Cookie")) request.headerMap.remove(key)
    }
    next(request)
  }
}