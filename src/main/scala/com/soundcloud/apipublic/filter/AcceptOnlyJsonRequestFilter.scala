package com.soundcloud.apipublic.filter

import com.twitter.finagle.http._
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

class AcceptOnlyJsonRequestFilter extends SimpleFilter[Request, Response] {
  override def apply(request: Request, next: Service[Request, Response]) = {
    stripFormatParam(request).flatMap { req =>
      if (isJsonRequest(req)) {
        req.accept = "application/json"
        next(req)
      } else
        Future.value(Response(Version.Http11, Status.NotAcceptable))
    }
  }

  private def isJsonRequest(request: Request) =
    (!request.path.contains('.') || request.path.split('.').toList.last == "json") &&
      (request.acceptMediaTypes.isEmpty ||
        request.acceptMediaTypes.contains("*/*") ||
        request.acceptMediaTypes.contains("*") ||
        request.acceptMediaTypes.exists(_.matches("(text|application)/(x-)?j(avascript|son)")))

  private def isGet(request: Request) =
    request.method == Method.Get

  private def stripFormatParam(request: Request): Future[Request] = {
    if (!isGet(request)) {
      Future.value(request)
    } else {
      val paramsWithoutFormat = request.params.filterNot { case (k, v) => k == "format" && v == "xml" }
      val req = Request(request.path, paramsWithoutFormat.toSeq: _*)
      req.version_=(request.version)
      req.method_=(request.method)
      request.headerMap.foreach {
        case (k, v) =>
          req.headerMap.set(k, v)
      }
      Future.value(req)
    }
  }
}
