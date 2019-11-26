package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.Service
import com.twitter.finagle.http._
import com.twitter.util.{Await, Future}

class StaticFilesFilterSpec extends UnitSpecification {
  trait Context extends Scope {
    val next = mock[Service[Request, Response]]
    val filter = new StaticFilesFilter
  }

  List(
    Request(Method.Delete, "/dummy"),
    Request(Method.Connect, "/not-found"),
    Request(Method.Post, "/post"),
    Request(Method.Put, "/update"),
    Request(Method.Options, "/"),
    Request(Method.Head, "/head"),
    Request(Method.Trace, "/deal"),
    Request(Method.Get, "/non-static"),
    Request(Method.Get, "/tracks/213"),
    Request(Method.Get, "/nope.txt")
  ) foreach { request =>
    s"Passes through all requests that are not static files, testing ${request.method} request to ${request.path} " in new Context {
      val expected = Response(Version.Http11, Status.Ok)
      expected.setContentString("dealwithit")

      next.apply(request) returns (Future(expected))

      val resp = Await.result(filter(request, next))
      resp.statusCode ==== Status.Ok.code
      resp.contentString ==== "dealwithit"
    }
  }

  "serves robots.txt" in new Context {
    val request = Request(Method.Get, "/robots.txt")
    val resp = Await.result(filter(request, next))

    resp.statusCode ==== Status.Ok.code
    resp.contentString ==== "User-agent: *\nDisallow: \n"
    resp.contentLength ==== Some(25L)
    resp.contentType ==== Some("text/plain;charset=utf-8")

    resp.headerMap.get("Cache-Control") ==== Some("public, max-age=86400")
  }

  "serves crossdomain.xml" in new Context {
    val request = Request(Method.Get, "/crossdomain.xml")
    val resp = Await.result(filter(request, next))

    resp.statusCode ==== Status.Ok.code
    resp.contentString ==== filter.crossdomainContents
    resp.contentLength ==== Some(666)
    resp.contentType ==== Some("application/xml;charset=utf-8")

    resp.headerMap.get("Cache-Control") ==== Some("public, max-age=86400")

    resp.headerMap.get("Accept-Ranges") ==== Some("bytes")
  }
}
