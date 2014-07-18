package com.soudcloud.publicApiStrangler

import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.finagle.http.{Request, Response}
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}
import com.twitter.finagle.Service
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpVersion, DefaultHttpRequest}
import com.twitter.util.{Await, Future}
import com.soundcloud.scalakit.finagle.http.{HandlerRequest, AlwaysMatchesPathMatcher}

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
  }
}
