package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.finagle.{ResponseBuilder, Request => BffRequest}
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.finagle.http.{AlwaysMatchesPathMatcher, HandlerRequest, RouterResponse}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Method, Request => FinagleRequest, Response => FinagleResponse}
import com.twitter.util.{Await, Future}
import org.mockito.ArgumentMatcher
import org.mockito.Matchers.{eq => eqTo, argThat => argT}

class ContentAuthorizationFilterSpec extends UnitSpecification with Fixtures {

  trait Context extends Scope {
    val someRequest = new HandlerRequest(AlwaysMatchesPathMatcher, FinagleRequest("/something")).request

    // We need to use a custom matcher because equals doesn't work anymore since Finagle 6.35 :(
    val requestMatcher = new ArgumentMatcher[BffRequest] {
      override def matches(argument: scala.Any): Boolean = {
        val request = argument.asInstanceOf[BffRequest]
        request.path.equals("/something") &&  request.method.equals(Method.Get)
      }
    }

    val service = mock[Service[FinagleRequest, RouterResponse]]
    val authorizeContent = mock[AuthorizeHttpResponse]
    val originalResponse = RouterResponse(FinagleResponse(), "undefined")
    val contentAuthorizationFilter = new ContentAuthorizationFilter(authorizeContent)
  }

  "when the response doesnt contain tracks" >> {

    "returns the response unchanged" in new Context {
      val expectedResponseBuilder = new ResponseBuilder().body(originalResponse.contentString).status(originalResponse.statusCode)

      service.apply(any[BffRequest]) returns Future.value(originalResponse)
      authorizeContent.apply(any[BffRequest], ===(originalResponse.statusCode), ===(originalResponse.contentString)) returns Future.value(expectedResponseBuilder)

      val authorizedResponse = Await.result(contentAuthorizationFilter.apply(someRequest, service))

      authorizedResponse.status mustEqual originalResponse.status
      authorizedResponse.contentString mustEqual originalResponse.contentString
      authorizedResponse.headerMap mustEqual originalResponse.headerMap + ("Content-Length" -> originalResponse.contentString.length.toString)
    }
  }

  "when the response contains tracks" >> {
    "returns original response with authorization info, if authorized" in new Context {
      val bodyWithAuthorizationInformation = originalResponse.contentString + "some stuff here about policies and stuff"
      val expectedResponseBuilder = new ResponseBuilder().body(bodyWithAuthorizationInformation).status(originalResponse.statusCode)
      val expectedResponse = expectedResponseBuilder.build

      service.apply(any[BffRequest]) returns Future.value(originalResponse)
      authorizeContent.apply(any[BffRequest], ===(originalResponse.statusCode), ===(originalResponse.contentString)) returns Future.value(expectedResponseBuilder)

      val authorizedResponse = Await.result(contentAuthorizationFilter.apply(someRequest, service))

      authorizedResponse.status mustEqual expectedResponse.status
      authorizedResponse.contentString mustEqual expectedResponse.contentString
      authorizedResponse.headerMap mustEqual expectedResponse.headerMap
    }
  }
}
