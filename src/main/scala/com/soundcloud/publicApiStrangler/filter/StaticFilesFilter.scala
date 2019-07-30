package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.jvmkit.module.http.server.ResponseBuilder
import com.twitter.conversions.DurationOps._
import com.twitter.finagle.http.{MediaType, Method, Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import org.jboss.netty.util.CharsetUtil._

class StaticFilesFilter extends SimpleFilter[Request, Response] {

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

  override def apply(request: Request, next: Service[Request, Response]) = {
    (request.method, request.path) match {
      case (Method.Get, "/robots.txt") => renderRobots
      case (Method.Get, "/crossdomain.xml") => renderCrossdomain
      case _ => next(request)
    }
  }

  private def renderRobots: Future[Response] = {
    Future.value(ResponseBuilder().
      mediaType(MediaType.PlainText).
      body(robotsContents).
      headers(Map(
        "Cache-Control" -> s"public, max-age=$oneDayInSeconds",
        "Content-Length" -> contentLength(robotsContents)
      )).
      build
    )
  }

  private def renderCrossdomain: Future[Response] = {
    Future.value(ResponseBuilder().
      mediaType(MediaType.Xml).
      body(crossdomainContents).
      headers(Map(
        "Accept-Ranges" -> "bytes",
        "Cache-Control" -> s"public, max-age=$oneDayInSeconds",
        "Content-Length" -> contentLength(crossdomainContents)
      ))
      .build)
  }
}
