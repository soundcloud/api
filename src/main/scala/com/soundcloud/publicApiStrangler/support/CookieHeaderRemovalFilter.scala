package com.soundcloud.publicApiStrangler.support

import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.twitter.finagle.http.Request
import com.twitter.finagle.{Service, SimpleFilter}

case class CookieHeaderRemovalFilter extends SimpleFilter[Request, RouterResponse] {
  override def apply(request: Request, next: Service[Request, RouterResponse]) = {
    for (key <- request.headerMap.keySet) {
      if (key.equalsIgnoreCase("Cookie")) request.headerMap.remove(key)
    }
    next(request)
  }
}