package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.{AnonymousUserSession, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.{ParamMap, Status}

import scala.jdk.CollectionConverters.setAsJavaSetConverter

class ConnectHandlerSpec extends UnitSpecification {
  trait Context extends HandlerSpecificationScope {
    def params = ParamMap("response_type" -> "code", "scope" -> "")
    private val deckApplication = Urn("soundcloud", "applications", "5283")
    val session =
      (new UserSessionBuilder)
        .setAgent(deckApplication)
        .setScopes(Set("a", "b").asJava)
        .build
        .asInstanceOf[AnonymousUserSession]
    val userAuthentication = new FakeUserAuthentication(session)
    val mockMothershipDispatcher = mock[DispatchToMothershipHandler]
    val connectHandler = new ConnectHandler(userAuthentication, mockMothershipDispatcher)
    override def routingDefinitions() = Routing.forConnectHandler(connectHandler)
    val response = get("/connect", params)
  }

  "#connect" >> {
    "valid response_type and scope" >> {
      "returns 302 status code" in new Context {
        response.status ==== Status.Found
      }

      "sets the Location header" in new Context {
        response.headerMap("Location") ==== s"https://secure.soundcloud.com/connect${params.toString()}"
      }

      "returns html body with redirect location" in new Context {
        val url = s"https://secure.soundcloud.com/connect${params.toString()}"
        response.contentString ==== s"""<html><body>You are being <a href="$url">redirected</a>.</body></html>"""
      }
    }
  }
}
