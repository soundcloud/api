package com.soundcloud.publicApiStrangler

import org.jboss.netty.handler.codec.http.{HttpHeaders, HttpVersion, DefaultHttpRequest, HttpMethod}
import com.twitter.finagle.http.Request
import com.soundcloud.scalakit.test.UnitSpecification
import org.jboss.netty.buffer.ChannelBuffers

class ForwardedRequestSpec extends UnitSpecification {
  "copies the request" >> {
    trait Context extends Scope {
      def makeRequest(method: HttpMethod, uri: String, body: String, headers: Map[String, String]) = {
        val request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, method, uri)
        request.setContent(ChannelBuffers.copiedBuffer(body.getBytes))
        headers.foreach {
          case (k, v) => request.headers().add(k, v)
        }
        Request(request)
      }
    }

    "sends same methods" in new Context {
      val request = makeRequest(HttpMethod.POST, "/", "", Map("" -> "a"))
      ForwardedRequest(request).getMethod() must_== (HttpMethod.POST)
    }

    "sends same body" in new Context {
      val body = "abcdef"
      val request = makeRequest(HttpMethod.POST, "/", body, Map("" -> "a"))
      ForwardedRequest(request).getContentString() must_== (body)
    }

    "sends same headers" in new Context {
      val headers = Map("abc" -> "123")
      val request = makeRequest(HttpMethod.POST, "/", "body", headers)

      ForwardedRequest(request).headers.get("abc") must_== ("123")
    }

    "adds mandatory headers" in new Context {
      val request = makeRequest(HttpMethod.POST, "/", "body", Map())
      val headers = ForwardedRequest(request).headers
      headers.get("X-Forwarded-Proto") must_== ("https")
      headers.get("Host") must_== ("api.soundcloud.com")
    }
  }
}
