package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.finagle.{Request => BffRequest}
import com.soundcloud.publicApiStrangler.authorization.ContentAuthorizationFilter.SkipContentAuthHeader
import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.twitter.finagle.http.Request
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

import scala.collection.JavaConversions._

class ContentAuthorizationFilter(authorizeContent: AuthorizeHttpResponse)
  extends SimpleFilter[Request, RouterResponse] {

  override def apply(request: Request, next: Service[Request, RouterResponse]) = {
    val req = new BffRequest(request)

    next(req).flatMap {
      case response =>
        if (response.headerMap.contains(SkipContentAuthHeader)) {
          response.headerMap.remove(SkipContentAuthHeader)
          Future.value(RouterResponse(response, "undefined"))
        } else {
          authorize(req, response).map(RouterResponse(_, "undefined"))
        }
    }
  }

  private def authorize(request: BffRequest, response: RouterResponse) =
    authorizeContent(request, response.statusCode, body(response)).map { render =>
      render.headers(headersMap(response)).build
    }

  private def body(response: RouterResponse) =
    response.contentString

  private def headersMap(response: RouterResponse) =
    response.headerMap.entrySet.map(e => e.getKey -> e.getValue).toMap
}

object ContentAuthorizationFilter {
  val SkipContentAuthHeader = "Skip-Content-Auth"
}
