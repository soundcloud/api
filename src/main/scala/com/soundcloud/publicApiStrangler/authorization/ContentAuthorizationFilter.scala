package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.scalakit.UTF8
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.soundcloud.bff.finagle.{Request => BffRequest}
import scala.collection.JavaConversions._

class ContentAuthorizationFilter(authorizeContent: AuthorizeHttpResponse) extends SimpleFilter[Request, Response] {

  override def apply(request: Request, next: Service[Request, Response]) = {
    val req = new BffRequest(request)
    for {
      response <- next(req)
      modifiedResponse <- authorize(req, response)
    } yield modifiedResponse
  }

  private def authorize(request: BffRequest, response: Response) =
    authorizeContent(request, response.statusCode, body(response)).map { render =>
      render.headers(headersMap(response)).build
    }

  private def body(response: Response) =
    response.getContent.toString(UTF8)

  private def headersMap(response: Response) =
    response.headers.entries.map(e => e.getKey -> e.getValue).toMap
}
