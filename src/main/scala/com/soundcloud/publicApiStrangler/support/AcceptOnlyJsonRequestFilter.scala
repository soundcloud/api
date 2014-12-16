package com.soundcloud.publicApiStrangler.support

import com.twitter.finagle.Service
import com.twitter.finagle.SimpleFilter
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Response
import com.twitter.finagle.http.Status
import com.twitter.finagle.http.Version
import com.twitter.util.Future

class AcceptOnlyJsonRequestFilter extends SimpleFilter[Request, Response] {

  override def apply(request: Request, next: Service[Request, Response]) =
    if (isJsonRequest(request))
      next(request)
    else
      Future.value(Response(Version.Http11, Status.NotAcceptable))

  private def isJsonRequest(request: Request) =
    (!request.path.contains('.') || request.path.split('.').toList.last == "json") &&
      (request.acceptMediaTypes.isEmpty ||
      	request.acceptMediaTypes.find(_ == "*/*").isDefined ||
        request.acceptMediaTypes.find(_ == "*").isDefined ||
        request.acceptMediaTypes.find(_.matches("(text|application)/(x-)?j(avascript|son)")).isDefined)
}
