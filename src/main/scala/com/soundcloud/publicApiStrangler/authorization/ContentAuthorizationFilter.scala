package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.finagle.{Request => BffRequest}
import com.soundcloud.scalakit.UTF8
import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.twitter.finagle.http.Request
import com.twitter.finagle.{Service, SimpleFilter}

import scala.collection.JavaConversions._

class ContentAuthorizationFilter(authorizeContent: AuthorizeHttpResponse) extends SimpleFilter[Request, RouterResponse] {

  override def apply(request: Request, next: Service[Request, RouterResponse]) = {
    val req = new BffRequest(request)
    for {
      response <- next(req)
      modifiedResponse <- authorize(req, response)
    } yield RouterResponse(modifiedResponse, "undefined")
  }

  private def authorize(request: BffRequest, response: RouterResponse) =
    authorizeContent(request, response.statusCode, body(response)).map { render =>
      render.headers(headersMap(response)).build
    }

  private def body(response: RouterResponse) =
    response.getContent().toString(UTF8)

  private def headersMap(response: RouterResponse) =
    response.headers().entries.map(e => e.getKey -> e.getValue).toMap
}
