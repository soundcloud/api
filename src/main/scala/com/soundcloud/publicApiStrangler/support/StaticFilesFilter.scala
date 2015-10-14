package com.soundcloud.publicApiStrangler.support

import java.nio.charset.Charset

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.twitter.finagle.http.Request
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import com.twitter.util.TimeConversions._
import org.jboss.netty.handler.codec.http.HttpMethod
import org.jboss.netty.util.CharsetUtil._

class StaticFilesFilter extends SimpleFilter[Request, RouterResponse] {

  val crossdomainContents = """<?xml version="1.0"?>
                              |<!DOCTYPE cross-domain-policy SYSTEM "http://www.macromedia.com/xml/dtds/cross-domain-policy.dtd">
                              |<cross-domain-policy>
                              |  <!--
                              |    Lock down access to Flash applications served by us.
                              |    This policy should be served for all domains that Flash apps require
                              |    access to and that are not serving api, images or stream (those need
                              |    to be open to third party apps as well).
                              |  -->
                              |  <allow-access-from domain="soundcloud.com" />
                              |  <allow-access-from domain="*.soundcloud.com" secure="false" />
                              |  <allow-access-from domain="*.sndcdn.com" secure="false" />
                              |  <site-control permitted-cross-domain-policies="master-only"/>
                              |</cross-domain-policy>
                              |""".stripMargin

  override def apply(request: Request, next: Service[Request, RouterResponse]) = {
    (request.method, request.path) match {
      case (HttpMethod.GET, "/robots.txt") => renderRobots
      case (HttpMethod.GET, "/crossdomain.xml") => renderCrossdomain
      case _ => next(request)
    }
  }

  private def renderRobots: Future[RouterResponse] = {
    val charset: Charset = UTF_8
    val contents = "User-agent: *\nDisallow: \n"
    val contentLength = contents.getBytes(charset).length.toString
    val finagleResponse = (new ResponseBuilder)
      .contentType(s"text/plain; charset=$charset")
      .header("Content-Length", contentLength)
      .body(contents).build

    val oneDayInSeconds = 1.day.inSeconds

    Map(
      "Access-Control-Allow-Headers" -> "Accept, Authorization, Content-Type, Origin",
      "Access-Control-Allow-Methods" -> "GET, PUT, POST, DELETE",
      "Access-Control-Allow-Origin" -> "*",
      "Access-Control-Expose-Headers" -> "Date",
      "Cache-Control" -> s"public, max-age=$oneDayInSeconds").foreach {
      case (headerName, headerValue) =>
        finagleResponse.headers().add(headerName, headerValue)
    }

    Future.value(
      RouterResponse(finagleResponse, "/robots.txt")
    )
  }

  private def renderCrossdomain: Future[RouterResponse] = {
    val contentLength = crossdomainContents.getBytes(UTF_8).length.toString
    val finagleResponse = (new ResponseBuilder)
      .contentType(s"text/xml")
      .header("Content-Length", contentLength)
      .body(crossdomainContents).build

    val oneDayInSeconds = 1.day.inSeconds

    Map(
      "Access-Control-Allow-Headers" -> "Accept, Authorization, Content-Type, Origin",
      "Access-Control-Allow-Methods" -> "GET, PUT, POST, DELETE",
      "Access-Control-Allow-Origin" -> "*",
      "Access-Control-Expose-Headers" -> "Date",
      "Cache-Control" -> s"public, max-age=$oneDayInSeconds",
      "Accept-Ranges" -> "bytes").foreach {
      case (headerName, headerValue) =>
        finagleResponse.headers().add(headerName, headerValue)
    }

    Future.value(
      RouterResponse(finagleResponse, "/crossdomain.xml")
    )
  }
}
