package com.soundcloud.publicApiStrangler.support

import com.soundcloud.scalakit.finagle.http.{AlwaysMatchesPathMatcher, HandlerRequest}
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.{Await, Future}
import org.jboss.netty.handler.codec.http.{DefaultHttpRequest, HttpMethod, HttpRequest, HttpResponse, HttpVersion}

class DispatchToMothershipHandlerSpec extends UnitSpecification {
  "dispatches requests to the mothership" >> {

    trait Context extends Scope {
      val mothershipClient = mock[Service[HttpRequest, HttpResponse]]
      val handler = new DispatchToMothershipHandler(mothershipClient)
      val response = mock[Response]
      val request = new HandlerRequest(AlwaysMatchesPathMatcher, Request(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.CONNECT, "/")))
    }

    "returns the response verbatim" >> {
      "is the same response" in new Context {
        mothershipClient(any[Request]) returns (Future.value(response))
        Await.result(handler(request)) must be_==(response)
      }
    }

    "returns 500 for failed requests" in new Context {
      mothershipClient(any[Request]) returns (Future.exception(new IllegalStateException))
      Await.result(handler(request)).statusCode mustEqual 500
    }
  }
}
