package com.soundcloud.publicApiStrangler.support

import com.twitter.finagle.http.{Method, Request}
import com.twitter.io.Reader

import scala.collection.JavaConversions._

object ForwardedRequest {
  val mandatoryHeaders = Map("X-Forwarded-Proto" -> "https", "Host" -> "api.soundcloud.com")

  def apply(originalRequest: Request) = {
    addMandatoryHeaders(originalRequest)
    originalRequest
  }

  private def addMandatoryHeaders(newRequest: Request) = {
    mandatoryHeaders.foreach {
      case (k, v) => newRequest.headerMap.put(k, v)
    }
  }

}
