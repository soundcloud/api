package com.soundcloud.publicApiStrangler.support

import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.soundcloud.scalakit.test.{UnitSpecification, VerifiedMocks}
import com.twitter.finagle.Service
import com.twitter.finagle.http._
import com.twitter.util.{Await, Future}
import org.jboss.netty.handler.codec.http.HttpMethod

class StaticFilesFilterSpec extends UnitSpecification {

  trait Context extends VerifiedMocks {
    val next = mock[Service[Request, RouterResponse]]
    val filter = new StaticFilesFilter
  }

  List(
    Request(HttpMethod.DELETE, "/dummy"),
    Request(HttpMethod.CONNECT, "/not-found"),
    Request(HttpMethod.POST, "/post"),
    Request(HttpMethod.PUT, "/update"),
    Request(HttpMethod.OPTIONS, "/"),
    Request(HttpMethod.HEAD, "/head"),
    Request(HttpMethod.TRACE, "/deal")
  ) foreach {
    request =>
      s"Passes through all ${request.method} requests" in new Context {
        val underlyingResp = Response(Version.Http11, Status.Ok)
        underlyingResp.setContentString("dealwithit")
        val expected = RouterResponse(underlyingResp, "/")

        next.apply(request) returns (Future(expected))

        val resp = Await.result(filter(request, next))
        resp.statusCode ==== Status.Ok.getCode
        resp.contentString ==== "dealwithit"
      }
  }

  "serves robots.txt" in new Context {
    val request = Request(HttpMethod.GET, "/robots.txt")
    val resp = Await.result(filter(request, next))

    resp.statusCode ==== Status.Ok.getCode
    resp.contentString ==== "User-agent: *\nDisallow: \n"
    resp.contentLength ==== Some(25L)
    resp.contentType ==== Some("text/plain; charset=UTF-8")

    resp.headerMap.get("Access-Control-Allow-Headers") ==== Some("Accept, Authorization, Content-Type, Origin")
    resp.headerMap.get("Access-Control-Allow-Methods") ==== Some("GET, PUT, POST, DELETE")
    resp.headerMap.get("Access-Control-Allow-Origin") ==== Some("*")
    resp.headerMap.get("Access-Control-Expose-Headers") ==== Some("Date")
    resp.headerMap.get("Cache-Control") ==== Some("public, max-age=86400")
  }

  "serves crossdomain.xml" in new Context {
    val request = Request(HttpMethod.GET, "/crossdomain.xml")
    val resp = Await.result(filter(request, next))

    resp.statusCode ==== Status.Ok.getCode
    resp.contentString ==== filter.crossdomainContents
    resp.contentLength ==== Some(665)
    resp.contentType ==== Some("text/xml")

    resp.headerMap.get("Access-Control-Allow-Headers") ==== Some("Accept, Authorization, Content-Type, Origin")
    resp.headerMap.get("Access-Control-Allow-Methods") ==== Some("GET, PUT, POST, DELETE")
    resp.headerMap.get("Access-Control-Allow-Origin") ==== Some("*")
    resp.headerMap.get("Access-Control-Expose-Headers") ==== Some("Date")
    resp.headerMap.get("Cache-Control") ==== Some("public, max-age=86400")

    resp.headerMap.get("Accept-Ranges") ==== Some("bytes")
  }
}


