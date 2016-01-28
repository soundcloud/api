package com.soundcloud.publicApiStrangler.support

import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.finagle.http.Version.Http11
import com.twitter.finagle.http.{Method, Request}

class ForwardedRequestSpec extends UnitSpecification {
  "copies the request" >> {
    trait Context extends Scope {
      def makeRequest(method: Method, uri: String, body: String, headers: Map[String, String]) = {

        val request = Request(Http11, method, uri)
        request.setContentString(body)
        headers.foreach {
          case (k, v) => request.headerMap.put(k, v)
        }
        request
      }
    }

    "sends same methods" in new Context {
      val request = makeRequest(Method.Post, "/", "", Map("" -> "a"))
      ForwardedRequest(request).method must_== (Method.Post)
    }

    "sends same body" in new Context {
      val body = "abcdef"
      val request = makeRequest(Method.Post, "/", body, Map("" -> "a"))
      ForwardedRequest(request).getContentString() must_== (body)
    }

    "sends same headers" in new Context {
      val headers = Map("abc" -> "123")
      val request = makeRequest(Method.Post, "/", "body", headers)

      ForwardedRequest(request).headerMap.get("abc") must_== Some("123")
    }

    "adds mandatory headers" in new Context {
      val request = makeRequest(Method.Post, "/", "body", Map())
      val headers = ForwardedRequest(request).headerMap
      headers.get("X-Forwarded-Proto") must_== (Some("https"))
      headers.get("Host") must_== (Some("api.soundcloud.com"))
    }
  }
}
