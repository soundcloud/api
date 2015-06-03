package com.soundcloud.publicApiStrangler.controller

import java.nio.charset.Charset

import com.soundcloud.bff.web.BffInjectionBasedController
import com.twitter.util.Future
import org.jboss.netty.util.CharsetUtil._

class StaticResponseController(path: String, contents: String, contentType: String = "text/plain", charset: Charset = UTF_8) extends BffInjectionBasedController {
  val contentLength = contents.getBytes(charset).length.toString

  get(path) { _ =>
    Future.value(render
      contentType s"$contentType; charset=$charset"
      header("Content-Length", contentLength)
      body contents)
  }
}


