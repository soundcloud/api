package com.soundcloud.publicApiStrangler

import com.twitter.finagle.http.Request
import org.jboss.netty.handler.codec.http.DefaultHttpRequest
import scala.collection.JavaConversions._

object ForwardedRequest {
  val mandatoryHeaders = Map("X-Forwarded-Proto" -> "https", "Host" -> "api.soundcloud.com")

  def apply(originalRequest: Request) = {
    val newRequest = new DefaultHttpRequest(originalRequest.version, originalRequest.method, originalRequest.getUri())
    copyHeaders(originalRequest, newRequest)
    copyContent(originalRequest, newRequest)

    addMandatoryHeaders(newRequest)
    Request(newRequest)
  }

  private def copyContent(originalRequest: Request, newRequest: DefaultHttpRequest) = {
    newRequest.setContent(originalRequest.content)
  }

  private def copyHeaders(originalRequest: Request, newRequest: DefaultHttpRequest) = {
    originalRequest.headers().foreach {
      (mapEntry) =>
        newRequest.headers.set(mapEntry.getKey, mapEntry.getValue)
    }
  }

  private def addMandatoryHeaders(newRequest: DefaultHttpRequest) = {
    mandatoryHeaders.foreach {
      case (k, v) => newRequest.headers.set(k, v)
    }
  }
}
