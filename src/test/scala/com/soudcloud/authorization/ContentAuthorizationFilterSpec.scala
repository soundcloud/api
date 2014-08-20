package com.soudcloud.authorization

import com.twitter.finagle.http.{ Response => FinagleResponse }
import org.mockito.Matchers

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.scalakit.UserSession
import com.soundcloud.scalakit.finagle.http.HandlerRequest
import com.soundcloud.scalakit.test.VerifiedMocks
import com.twitter.finagle.Service
import com.twitter.finagle.http.Request
import com.twitter.util.Await
import com.twitter.util.Future

class ContentAuthorizationFilterSpec extends UnitSpecification with Fixtures {

  trait Context extends VerifiedMocks {
    val session = mock[UserSession]
    val authorizeContent = mock[AuthorizeContent]
    val request = mock[HandlerRequest]

    val status = 200
    val content = playlist.toString

    val response = {
      val builder = new ResponseBuilder
      builder.header("additional", "header")
      builder.body(content)
      builder.build
    }
    val feature = "PUBLIC_API_STRANGLER_CONTENT_AUTHORIZATION"
    val service = new Service[HandlerRequest, FinagleResponse] {
      override def apply(request: HandlerRequest) = {
        request mustEqual Context.this.request
        Future(response)
      }
    }

    val filter = new ContentAuthorizationFilter(authorizeContent)

    lazy val authorizedResponse = Await.result(filter.apply(request, service))

    def builder = {
      val builder = new ResponseBuilder
      builder.body(content)
    }

    override def before = {
      when(request.request).thenReturn(Request())
      when(request.userSession).thenReturn(session)
      when(authorizeContent.apply(any, Matchers.eq(status), Matchers.eq(content)))
        .thenReturn(Future(builder))
    }
  }

  "authorizes the content" in new Context {
    authorizedResponse.statusCode mustEqual status
    authorizedResponse.getContentString mustEqual content
  }

  "copies the original response response to the authorized response" in new Context {
    authorizedResponse.headers.get("additional") mustEqual "header"
  }
}
