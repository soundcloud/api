package com.soundcloud.publicApiStrangler.support

import com.twitter.finagle.http.Request
import com.twitter.io.Reader
import scala.collection.JavaConversions._

object ForwardedRequest {
  val mandatoryHeaders = Map("X-Forwarded-Proto" -> "https", "Host" -> "api.soundcloud.com")

  def apply(originalRequest: Request) = {
    val newRequest = Request(originalRequest.version, originalRequest.method, originalRequest.uri, originalRequest.reader)
    copyHeaders(originalRequest, newRequest)
    addMandatoryHeaders(newRequest)

    newRequest
  }

  private def copyHeaders(originalRequest: Request, newRequest: Request) = {
    originalRequest.headerMap.foreach {
      case (key, value) =>
        newRequest.headerMap.put(key, value)
    }
  }

  private def addMandatoryHeaders(newRequest: Request) = {
    mandatoryHeaders.foreach {
      case (k, v) => newRequest.headerMap.put(k, v)
    }
  }
}
