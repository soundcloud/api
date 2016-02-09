package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.twitter.finagle.http.{Method, Request}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import com.twitter.util.TimeConversions._
import org.jboss.netty.util.CharsetUtil._

class StaticFilesFilter extends SimpleFilter[Request, RouterResponse] {

  val crossdomainContents =
    """<?xml version="1.0"?>
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
      | """.stripMargin

  val robotsContents = "User-agent: *\nDisallow: \n"

  private val oneDayInSeconds = 1.day.inSeconds

  private def contentLength(content: String) = {
    content.getBytes(UTF_8).length.toString
  }

  override def apply(request: Request, next: Service[Request, RouterResponse]) = {
    (request.method, request.path) match {
      case (Method.Get, "/robots.txt") => renderRobots
      case (Method.Get, "/crossdomain.xml") => renderCrossdomain
      case _ => next(request)
    }
  }

  private def renderRobots: Future[RouterResponse] = {
    val responseBuilder = (new ResponseBuilder)
      .contentType(s"text/plain; charset=UTF-8")
      .body(robotsContents)

    Map(
      "Access-Control-Allow-Headers" -> "Accept, Authorization, Content-Type, Origin",
      "Access-Control-Allow-Methods" -> "GET, PUT, POST, DELETE",
      "Access-Control-Allow-Origin" -> "*",
      "Access-Control-Expose-Headers" -> "Date",
      "Cache-Control" -> s"public, max-age=$oneDayInSeconds",
      "Content-Length" -> contentLength(robotsContents)).foreach {
      case (headerName, headerValue) =>
        responseBuilder.header(headerName, headerValue)
    }

    Future.value(
      RouterResponse(responseBuilder.build, "/robots.txt")
    )
  }

  private def renderCrossdomain: Future[RouterResponse] = {
    val responseBuilder = (new ResponseBuilder)
      .contentType(s"text/xml")
      .body(crossdomainContents)

    Map(
      "Access-Control-Allow-Headers" -> "Accept, Authorization, Content-Type, Origin",
      "Access-Control-Allow-Methods" -> "GET, PUT, POST, DELETE",
      "Access-Control-Allow-Origin" -> "*",
      "Access-Control-Expose-Headers" -> "Date",
      "Accept-Ranges" -> "bytes",
      "Cache-Control" -> s"public, max-age=$oneDayInSeconds",
      "Content-Length" -> contentLength(crossdomainContents)).foreach {
      case (headerName, headerValue) =>
        responseBuilder.header(headerName, headerValue)
    }

    Future.value(
      RouterResponse(responseBuilder.build, "/crossdomain.xml")
    )
  }
}
