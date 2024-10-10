package com.soundcloud.apipublic.filter

import com.soundcloud.apipublic.client.secure.SecureClient
import com.soundcloud.apipublic.support.oauth._
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.rollout.Rollout
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import com.soundcloud.jvmkit.module.telemetry.Telemetry

class ForwardToSecureFilterSpec extends UnitSpecification {
  val fallbackBodyMessage = "fallback"
  val forwardedBodyMessage = "forwarded"

  class StubService extends Service[Request, Response] {
    override def apply(request: Request): Future[Response] = {
      Future.value(JsonResponseBuilder.ok(fallbackBodyMessage))
    }
  }

  trait Context extends Scope {
    val parser = mock[GrantExchangeRequestParser]
    val rollout = mock[Rollout]
    val secureClient = mock[SecureClient]
    val telemetry = Telemetry.createIsolatedInstance

    val filter = new ForwardToSecureFilter(parser, rollout, secureClient, telemetry)

    val service = new StubService

    def code: String = "code"

    def request = Request(Method.Post, "/oauth2/token")

    when(rollout.isActive(any())).thenReturn(Future.True)

    lazy val result = Await.result(filter(HandlerRequest(request), service))
  }

  "it defaults to fallback when code is not a jwt" in new Context {
    when(parser.parse(any()))
      .thenReturn(
        Right(
          GrantExchangeRequest(
            ClientCredential("id", "secret"),
            AuthorizationCodeGrant(code, "some-redirect"),
            RequestContext("", "")
          )
        )
      )

    result.contentString ==== fallbackBodyMessage
    verifyNoInteractions(secureClient)
  }

  "it defaults to fallback when FF is off" in new Context {
    when(rollout.isActive(any())).thenReturn(Future.False)

    when(parser.parse(any()))
      .thenReturn(
        Right(
          GrantExchangeRequest(
            ClientCredential("id", "secret"),
            AuthorizationCodeGrant(code, "some-redirect"),
            RequestContext("", "")
          )
        )
      )

    result.contentString ==== fallbackBodyMessage
    verifyNoInteractions(secureClient)
  }

  "it forwards to secure when code is a jwt" in new Context {
    override def code: String =
      "eyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiQTI1NktXIn0.ePCs8GffWZ7vkYgE1XRexDNwCE-1k5y7MslOcZJwMQohuXIZHyQw1w.reDtz2cAnhdTMIP-LVcsdA.78T4MCdF4cuA8vvS8nDK9Ix4ItP43gGElAVebvrn5xa2M4gULKtVEn9N3nEhrYW2HBrsPiu2PJcjo583Q2CH5V336y15BcdMdRAtVffdsGlrLhe9QHiMzkpns3zI5aV4y5JzeVjmMhKoQDWDmGoExYAinfUOw2p7a15DB2XLkdrrE64nKf0Zb3HsYIp6WXtNHpVOxEv8bINF4gijynavXX3mRqE4AAscCAp25yai8WpT7J3gg112CVc7Xc1nZ-gm.73Of_oIm7Hk5Ys6EfP-SzQ"

    when(parser.parse(any()))
      .thenReturn(
        Right(
          GrantExchangeRequest(
            ClientCredential("id", "secret"),
            AuthorizationCodeGrant(code, "some-redirect"),
            RequestContext("", "")
          )
        )
      )

    when(secureClient.forwardToNewTokenEndpoint(any())).thenReturn(Future(JsonResponseBuilder.ok(forwardedBodyMessage)))

    result.contentString ==== forwardedBodyMessage
    verify(secureClient).forwardToNewTokenEndpoint(any())
  }

  "it falls back when for other grant types" in new Context {
    when(parser.parse(any()))
      .thenReturn(
        Right(
          GrantExchangeRequest(
            ClientCredential("id", "secret"),
            ClientCredentialsGrant(Set.empty),
            RequestContext("", "")
          )
        )
      )

    result.contentString ==== fallbackBodyMessage
    verifyNoInteractions(secureClient)
  }

  "it falls back when parsing fails" in new Context {
    when(parser.parse(any()))
      .thenReturn(
        Left(InvalidRequest("bruh.mp3"))
      )

    result.contentString ==== fallbackBodyMessage

    verifyNoInteractions(secureClient)
  }
}
