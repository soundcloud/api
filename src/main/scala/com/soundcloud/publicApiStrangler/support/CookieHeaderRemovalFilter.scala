package com.soundcloud.publicApiStrangler.support

import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finagle.http.Request
import com.twitter.util.Future

case class CookieHeaderRemovalFilter(enabled: () => Future[Boolean]) extends SimpleFilter[Request, RouterResponse] {
  override def apply(request: Request, next: Service[Request, RouterResponse]) = {
    enabled().flatMap {
      case true =>
        for (key <- request.headerMap.keySet) {
          if (key.equalsIgnoreCase("Cookie")) request.headerMap.remove(key)
        }
        next(request)
      case _ => next(request)
    }
  }
}