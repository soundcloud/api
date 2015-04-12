package com.soundcloud.publicApiStrangler.support

import java.nio.charset.Charset

import com.soundcloud.scalakit.finagle.http.{AlwaysMatchesPathMatcher, HandlerRequest}
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status, Version}
import com.twitter.util.{Await, Future}
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.handler.codec.http._

class DispatchToMothershipHandlerSpec extends UnitSpecification {
  "dispatches requests to the mothership" >> {

    trait Context extends Scope {
      val mothershipClient = mock[Service[HttpRequest, HttpResponse]]
      val handler = new DispatchToMothershipHandler(mothershipClient)

      val httpResponse = new DefaultHttpResponse(Version.Http11, Status.Ok)
      httpResponse.headers().add("header1", "valueHeader1").add("header2", "valueHeader2")
      httpResponse.setContent(ChannelBuffers.copiedBuffer("body content", Charset.forName("UTF-8")))
      val response = Response (httpResponse)

      val request = Request(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.CONNECT, "/"))
      val handlerRequest = new HandlerRequest(AlwaysMatchesPathMatcher, request)
    }

    "returns the response verbatim" >> {
      "for defaultHandling" in new Context {
        mothershipClient(any[Request]) returns (Future.value(response))
        Await.result(handler(handlerRequest)) must be_==(response)
      }

      "for dispatch method" in new Context {
        mothershipClient(any[Request]) returns (Future.value(response))
        val responseFromBuilder = Await.result(handler.dispatch(request)).build
        responseFromBuilder.getStatusCode() ==== response.getStatusCode()
        responseFromBuilder.headers().get("header1") ==== "valueHeader1"
        responseFromBuilder.headers().get("header2") ==== "valueHeader2"
        responseFromBuilder.getContentString() ==== "body content"
      }

    }

    "returns 500 for failed requests" >> {
      "for defaultHandling" in new Context {
        mothershipClient(any[Request]) returns (Future.exception(new IllegalStateException))
        Await.result(handler(handlerRequest)).statusCode mustEqual 500
      }

      "for dispatch method" in new Context {
        mothershipClient(any[Request]) returns (Future.exception(new IllegalStateException))
        val responseFromBuilder = Await.result(handler.dispatch(request)).build
        responseFromBuilder.getStatusCode() ==== 500
      }
    }
  }
}
