package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import org.jboss.netty.util.CharsetUtil._

class StaticResponseControllerSpec extends InjectionBasedControllerSpecification {
  "GET /path" >> {

    "it hosts contents at path with default text/plain; charset=UTF-8" in {
      val controller = new StaticResponseController("/path", "contents")
      val response = get(controller, "/path")
      response.code ==== 200
      response.body ==== "contents"
      response.getHeader("Content-Type") ==== "text/plain; charset=UTF-8"
      response.getHeader("Content-Length") ==== "8"
    }

    "it hosts contents at path with provided contentType and charset" in {
      val controller = new StaticResponseController("/path", "contents", "text/html", ISO_8859_1)
      val response = get(controller, "/path")
      response.code ==== 200
      response.body ==== "contents"
      response.getHeader("Content-Type") ==== "text/html; charset=ISO-8859-1"
      response.getHeader("Content-Length") ==== "8"
    }

  }
}

