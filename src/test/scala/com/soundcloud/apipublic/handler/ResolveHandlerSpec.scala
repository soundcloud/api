package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.service.resolve.ResolveService
import com.soundcloud.apipublic.test.UnitSpecification
import com.twitter.util.Future
import com.soundcloud.apipublic.test.HandlerSpecificationScope
import com.soundcloud.apipublic.Routing
import com.soundcloud.jvmkit.module.util.Urn

class ResolveHandlerSpec extends UnitSpecification {
  trait Context extends HandlerSpecificationScope {
    lazy val session: UserSession = anonymousSession
    val userUrn = Urn("soundcloud", "users", "1")
    val permalinkUrlParam = "https://soundcloud.com/user-885473394"
    val userAuthentication = new FakeUserAuthentication(session)
    val mockResolveService = mock[ResolveService]
    val resolveHandler = new ResolveHandler(userAuthentication, mockResolveService)
    override def routingDefinitions = Routing.forResolveHandler(resolveHandler)

    def stubResolveService(maybePermalink: Option[String]) = {
      mockResolveService resolveUrl (session, permalinkUrlParam) returns Future.value(maybePermalink)
    }
  }

  "#resolve" >> {

    "resolve service succeeds" >> {
      trait ServiceSuccessContext extends Context {
        val permalink = "http://api.soundcloud.com/the-user"
        stubResolveService(Some(permalink))
      }

      "returns 302" in new ServiceSuccessContext {
        val response = get(s"/resolve?url=$permalinkUrlParam")
        response.statusCode ==== 302
      }

      "returns expected body with 'location' field" in new ServiceSuccessContext {
        val response = get(s"/resolve?url=$permalinkUrlParam")
        response.contentString ==== s"""{"status":"302 - Found", "location":"$permalink"}"""
      }

      "returns expected Location header" in new ServiceSuccessContext {
        val response = get(s"/resolve?url=$permalinkUrlParam")
        response.headerMap("Location") ==== permalink
      }

      "handles legacy url param key 'permalink_url'" in new ServiceSuccessContext {
        val response = get(s"/resolve?permalink_url=$permalinkUrlParam")
        response.statusCode ==== 302
      }
    }

    "resolve service fails" >> {
      trait ServiceFailureContext extends Context {
        stubResolveService(None)
      }

      "returns 404" in new ServiceFailureContext {
        val response = get(s"/resolve?url=$permalinkUrlParam")
        response.statusCode ==== 404
      }
    }
  }
}
