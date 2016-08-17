package com.soundcloud.publicApiStrangler.support

import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.twitter.finagle.http._
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

class AcceptOnlyJsonRequestFilter(stripXml: () => Future[Boolean]) extends SimpleFilter[Request, RouterResponse] {

  override def apply(request: Request, next: Service[Request, RouterResponse]) = {
    stripFormatParam(request).flatMap {
      case req =>
        if (isJsonRequest(req)) {
          req.accept = "application/json"
          next(req)
        } else
          Future.value(RouterResponse(Response(Version.Http11, Status.NotAcceptable), "undefined"))
    }
  }

  private def isJsonRequest(request: Request) =
    (!request.path.contains('.') || request.path.split('.').toList.last == "json") &&
      (request.acceptMediaTypes.isEmpty ||
        request.acceptMediaTypes.find(_ == "*/*").isDefined ||
        request.acceptMediaTypes.find(_ == "*").isDefined ||
        request.acceptMediaTypes.find(_.matches("(text|application)/(x-)?j(avascript|son)")).isDefined)

  private def isGet(request: Request) =
    request.method == Method.Get

  private def stripFormatParam(request: Request): Future[Request] = {
    if (isGet(request) == false) {
      Future.value(request)
    } else {
      stripXml().map {
        case true =>
          val paramsWithoutFormat = request.params.toIterable.filterNot { case (k, v) => k == "format" && v == "xml" }
          val req = Request(request.path, paramsWithoutFormat.toSeq: _*)
          req.version_=(request.version)
          req.method_=(request.method)
          request.headerMap.foreach {
            case (k, v) =>
              req.headerMap.add(k, v)
          }
          req
        case false =>
          request
      }
    }
  }
}
