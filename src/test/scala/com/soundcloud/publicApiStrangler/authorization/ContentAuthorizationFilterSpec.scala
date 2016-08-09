package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.finagle.{Request => BffRequest, ResponseBuilder}
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.finagle.http.{AlwaysMatchesPathMatcher, HandlerRequest, RouterResponse}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request => FinagleRequest, Response => FinagleResponse}
import com.twitter.util.{Await, Future}
import org.mockito.Matchers.{argThat => argT, eq => eqTo}

class ContentAuthorizationFilterSpec extends UnitSpecification with Fixtures {

  trait Context extends Scope {
    val someRequest = new HandlerRequest(AlwaysMatchesPathMatcher, FinagleRequest("/something")).request


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
