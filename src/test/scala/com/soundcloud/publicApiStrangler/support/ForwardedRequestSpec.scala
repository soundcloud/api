package com.soundcloud.publicApiStrangler.support

import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.finagle.http.Version.Http11
import com.twitter.finagle.http.{Method, Request}
import com.twitter.io.Buf
import java.nio.file.{Files, Paths}

class ForwardedRequestSpec extends UnitSpecification {

  "copies the request" >> {
    trait Context extends Scope {
      def makeRequest(method: Method, uri: String, body: Array[Byte], headers: Map[String, String]) = {

        val request = Request(Http11, method, uri)
        request.content_=(Buf.ByteArray.Owned(body))
        headers.foreach {
          case (k, v) => request.headerMap.put(k, v)
        }
        request
      }

      def stringToBytes(value: String) = value.getBytes("UTF-8")

    }

    "sends same methods" in new Context {
      val request = makeRequest(Method.Post, "/", stringToBytes(""), Map("" -> "a"))
      ForwardedRequest(request).method must_== (Method.Post)
    }

    "sends same body string value" in new Context {
      val body = "abcdef"
      val inputBodyByteArray = stringToBytes(body)
      val request = makeRequest(Method.Post, "/track", inputBodyByteArray, Map("" -> "a"))
      val forwardedRequest = ForwardedRequest(request)
      forwardedRequest.getContentString() must_== (body)

      val forwardedBodyByteArray = new Array[Byte](inputBodyByteArray.length)
      forwardedRequest.content.write(forwardedBodyByteArray, 0)
      forwardedBodyByteArray must beEqualTo(inputBodyByteArray)

    }

    "sends same body binary value" in new Context {
      val bodyInput = Files.readAllBytes(Paths.get("src/test/resources/soundcloud_logo.png"))
      val request = makeRequest(Method.Post, "/", bodyInput, Map("" -> "a"))
      val bodyInputAsBuffer = Buf.ByteArray.Owned(bodyInput)
      val bodyForwardedRequestAsBuf = ForwardedRequest(request).content
      bodyForwardedRequestAsBuf.length mustEqual bodyInputAsBuffer.length
      bodyForwardedRequestAsBuf must beEqualTo(bodyInputAsBuffer)
    }


    "sends same headers" in new Context {
      val headers = Map("abc" -> "123")
      val request = makeRequest(Method.Post, "/", stringToBytes("body"), headers)
      ForwardedRequest(request).headerMap.get("abc") must_== Some("123")
    }

    "adds mandatory headers" in new Context {
      val request = makeRequest(Method.Post, "/", stringToBytes("body"), Map())
      val headers = ForwardedRequest(request).headerMap
      headers.get("X-Forwarded-Proto") must_== (Some("https"))
      headers.get("Host") must_== (Some("api.soundcloud.com"))
    }
  }
}
