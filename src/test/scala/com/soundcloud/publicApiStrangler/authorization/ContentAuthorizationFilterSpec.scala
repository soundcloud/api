package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.http.server.{AlwaysMatchesPathMatcher, HandlerRequest, ResponseBuilder}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.{Await, Future}

class ContentAuthorizationFilterSpec extends UnitSpecification with Fixtures {

  trait Context extends Scope {
    val someRequest = HandlerRequest(AlwaysMatchesPathMatcher, Request("/something")).request


    val service = mock[Service[Request, Response]]
    val authorizeContent = mock[AuthorizeHttpResponse]
    val originalResponse = ResponseBuilder().build
    val contentAuthorizationFilter = new ContentAuthorizationFilter(authorizeContent)
  }

  "when the response doesnt contain tracks" >> {

    "returns the response unchanged" in new Context {
      val expectedResponse = ResponseBuilder()
        .body(originalResponse.contentString)
        .status(originalResponse.status)
        .headers(originalResponse.headerMap.toMap)
        .build

      service.apply(any[Request]) returns Future.value(originalResponse)
      authorizeContent.apply(any[HandlerRequest], ===(originalResponse.status), ===(originalResponse.contentString)) returns Future.value(expectedResponse)

      val authorizedResponse = Await.result(contentAuthorizationFilter.apply(someRequest, service))

      authorizedResponse.status mustEqual originalResponse.status
      authorizedResponse.contentString mustEqual originalResponse.contentString
      authorizedResponse.headerMap mustEqual originalResponse.headerMap
    }
  }

  "when the response contains tracks" >> {
    "returns original response with authorization info, if authorized" in new Context {
      val bodyWithAuthorizationInformation = originalResponse.contentString + "some stuff here about policies and stuff"
      val expectedResponse = ResponseBuilder().body(bodyWithAuthorizationInformation).status(originalResponse.status).build

      service.apply(any[Request]) returns Future.value(originalResponse)
      authorizeContent.apply(any[HandlerRequest], ===(originalResponse.status), ===(originalResponse.contentString)) returns Future.value(expectedResponse)

      val authorizedResponse = Await.result(contentAuthorizationFilter.apply(someRequest, service))

      authorizedResponse.status mustEqual expectedResponse.status
      authorizedResponse.contentString mustEqual expectedResponse.contentString
      authorizedResponse.headerMap mustEqual expectedResponse.headerMap
    }
  }
}
