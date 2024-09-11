package com.soundcloud.apipublic.client.secure

import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.twitter.finagle.Service
import com.twitter.finagle.http.{MediaType, Request, RequestBuilder, Response}
import com.twitter.io.Buf
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when

class SecureClientSpec extends UnitSpecification {

  trait Context extends Scope {
    val service = mock[Service[Request, Response]]
    val captor = capture[Request]
    val subject = new SecureClient(service)

    def body =
      """
        |thingA=1&
        |thingB=2
        |""".stripMargin

    def ogRequest = {
      val r = RequestBuilder()
        .url("https://api-public.soundcloud.com/oauth2/token")
        .setHeader("X-Real-Ip", "1.1.1.1")
        .buildPost(Buf.Utf8(body))

      r.setContentType(MediaType.WwwForm)

      r
    }

    lazy val result = Await.result(subject.forwardToNewTokenEndpoint(ogRequest))
  }

  "it forward the request correctly" in new Context {
    when(service.apply(any())).thenReturn(Future(JsonResponseBuilder.ok()))

    result

    there was one(service).apply(captor)

    captor.value.headerMap.toList must containAllOf(ogRequest.headerMap.toList)
    captor.value.path ==== "/oauth/token"
    captor.value.method ==== ogRequest.method
    captor.value.contentType ==== ogRequest.contentType
    captor.value.headerMap.get("Host") must beSome("api-public.soundcloud.com")
    captor.value.headerMap.get("Referer") must beSome("soundcloud.com")
  }

}
