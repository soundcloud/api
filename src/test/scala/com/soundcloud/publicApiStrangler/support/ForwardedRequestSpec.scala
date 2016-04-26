package com.soundcloud.publicApiStrangler.support

import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.finagle.http.Version.Http11
import com.twitter.finagle.http.{Request, Method}
import com.twitter.io.Buf
import com.twitter.io.Reader
import java.nio.file.{Files, Paths}
import java.io.ByteArrayInputStream

import com.twitter.io.Buf.Utf8
import com.twitter.util.Await

class ForwardedRequestSpec extends UnitSpecification {
  def makeRequest(method: Method, uri: String, body: Array[Byte], headers: Map[String, String]) = {
    val reader = Reader.fromStream(new ByteArrayInputStream(body))
    val request = Request(Http11, method, uri, reader)
    headers.foreach {
      case (k, v) => request.headerMap.put(k, v)
    }
    request
  }

  "sends same methods" >> {
    val request = makeRequest(Method.Post, "/", "".getBytes("UTF-8"), Map("" -> "a"))
    ForwardedRequest(request).method ==== Method.Post
  }

  "sends same body string value" >> {
    val body = "abcdef"
    val inputBodyByteArray = body.getBytes("UTF-8")
    val request = makeRequest(Method.Post, "/track", inputBodyByteArray, Map("" -> "a"))
    val forwardedRequest = ForwardedRequest(request)
    val bodyForwardedRequestAsBuf = Await.result(Reader.readAll(forwardedRequest.reader))
    val bodyForwardedRequestAsString = Utf8.unapply(bodyForwardedRequestAsBuf)
    bodyForwardedRequestAsString ==== Some(body)

    val forwardedBodyByteArray = new Array[Byte](inputBodyByteArray.length)
    bodyForwardedRequestAsBuf.write(forwardedBodyByteArray, 0)
    forwardedBodyByteArray must beEqualTo(inputBodyByteArray)
  }

  "sends same body binary value" >> {
    val bodyInput = Files.readAllBytes(Paths.get("src/test/resources/soundcloud_logo.png"))
    val request = makeRequest(Method.Post, "/", bodyInput, Map("" -> "a"))
    val bodyInputAsBuffer = Buf.ByteArray.Owned(bodyInput)
    val forwardedRequest = ForwardedRequest(request)
    val bodyForwardedRequestAsBuf = Await.result(Reader.readAll(forwardedRequest.reader))
    bodyForwardedRequestAsBuf.length ==== bodyInputAsBuffer.length
    bodyForwardedRequestAsBuf must beEqualTo(bodyInputAsBuffer)
  }

  "sends same headers" >> {
    val headers = Map("abc" -> "123")
    val request = makeRequest(Method.Post, "/", "body".getBytes("UTF-8"), headers)
    ForwardedRequest(request).headerMap.get("abc") must_== Some("123")
  }

  "adds mandatory headers" >> {
    val request = makeRequest(Method.Post, "/", "body".getBytes("UTF-8"), Map())
    val headers = ForwardedRequest(request).headerMap
    headers.get("X-Forwarded-Proto") ==== Some("https")
    headers.get("Host") ==== Some("api.soundcloud.com")
  }
}
