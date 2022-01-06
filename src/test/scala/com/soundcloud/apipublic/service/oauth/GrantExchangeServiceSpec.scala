package com.soundcloud.apipublic.service.oauth

import com.soundcloud.jvmkit.module.outcome.{GoodOps, NotAuthorized, NotValid, Outcome}
import com.soundcloud.apipublic.support.oauth._
import com.soundcloud.apipublic.test.UnitSpecification
import com.twitter.util.{Await, Future}
import proto.soundcloud.authenticator.{access_grant_exchange => proto}

class GrantExchangeServiceSpec extends UnitSpecification {
  trait Context extends Scope {
    val accessGrantExchangeService = mock[proto.AccessGrantExchangeService]

    val subject = new GrantExchangeService(accessGrantExchangeService)

    def accessGrant: AccessGrant

    val clientCredential = ClientCredential("some-client-id", "some-client-secret")

    def grantExchangeRequest: GrantExchangeRequest = GrantExchangeRequest(
      clientCredential,
      accessGrant,
      RequestContext("remote-ip", "user-agent")
    )

    def response: proto.AccessGrantExchangeResponse =
      proto
        .AccessGrantExchangeResponse()
        .withAccessToken(proto.AccessGrantExchangeResponse.AccessToken("", None, None, Seq.empty))

    val protoClientCredential = proto.ClientCredential(clientCredential.id, clientCredential.secret)

    def result: Outcome[AccessTokenResponse] = Await.result(subject.exchange(grantExchangeRequest))
  }

  "when received request contains an auth code grant" >> {
    trait AuthCodeContext extends Context {
      override def accessGrant: AuthorizationCodeGrant = AuthorizationCodeGrant("auth-code", "redirect-uri")

      accessGrantExchangeService
        .authorizationCodeExchange(any())
        .returns(Future.value(response))
    }

    "it calls authorizationCodeExchange" in new AuthCodeContext {
      result

      there was one(accessGrantExchangeService).authorizationCodeExchange(
        proto.AuthorizationCodeGrant(Some(protoClientCredential), accessGrant.code, accessGrant.redirectUri)
      )
    }

    verifyResponseHandlingBehaviour(resp =>
      new AuthCodeContext {
        override def response = resp
      }
    )
  }

  "when received request contains a client credential grant" >> {
    trait ClientCredentialContext extends Context {
      override def accessGrant: ClientCredentialsGrant = ClientCredentialsGrant(Set("scoooope"))

      accessGrantExchangeService
        .clientCredentialExchange(any())
        .returns(Future.value(response))
    }

    "it calls clientCredentialExchange" in new ClientCredentialContext {
      result

      there was one(accessGrantExchangeService).clientCredentialExchange(
        proto.ClientCredentialGrant(Some(protoClientCredential), Seq("scoooope"))
      )
    }

    verifyResponseHandlingBehaviour(resp =>
      new ClientCredentialContext {
        override def response = resp
      }
    )
  }

  "when received request contains a refresh token grant" >> {
    trait RefreshTokenContext extends Context {
      override def accessGrant: RefreshTokenGrant = RefreshTokenGrant("refresh")

      accessGrantExchangeService
        .refreshTokenExchange(any())
        .returns(Future.value(response))
    }

    "it calls refreshTokenExchange" in new RefreshTokenContext {
      result

      there was one(accessGrantExchangeService).refreshTokenExchange(
        proto.RefreshTokenGrant(Some(protoClientCredential), "refresh")
      )
    }

    verifyResponseHandlingBehaviour(resp =>
      new RefreshTokenContext {
        override def response = resp
      }
    )
  }

  /**
    * This function emulates rspec shared_examples style behaviour.
    *
    * I.e. it produces a specs2 Fragment which can be added to any
    * org.specs2.mutable.Specification subclass in order to run the
    * included tests.
    *
    * We use it here because this service may dispatch to any one of
    * a number of different rpc calls based on its argument, but each
    * call returns the same type of response. By calling this function
    * in the above context we can ensure that each rpc call handles its
    * response in the same way.
    *
    */
  private def verifyResponseHandlingBehaviour[C <: Context](ctxFactory: proto.AccessGrantExchangeResponse => C) = {
    "when the response is an access token" >> {
      "it returns an AccessTokenResponse" in {
        val response = proto
          .AccessGrantExchangeResponse()
          .withAccessToken(
            proto.AccessGrantExchangeResponse.AccessToken("token", Some(200), Some("refresh"), Seq("scope"))
          )
        val ctx = ctxFactory(response)

        ctx.result ==== AccessTokenResponse("token", Some(200), Some("refresh"), Seq("scope"), "bearer").good
      }
    }

    "when the response is empty" >> {
      "it throws an IllegalArgumentException" in {
        val response = proto.AccessGrantExchangeResponse()
        val ctx = ctxFactory(response)

        ctx.result must throwAn[IllegalArgumentException]
      }
    }

    "when the response is INVALID_CLIENT" >> {
      "it returns a NotAuthorized error" in {
        val response = proto
          .AccessGrantExchangeResponse()
          .withError(
            proto.AccessGrantExchangeResponse.Error.INVALID_CLIENT
          )
        val ctx = ctxFactory(response)

        ctx.result ==== NotAuthorized("invalid_client").bad
      }
    }

    "when the response is INVALID_GRANT" >> {
      "it returns a NotValid error" in {
        val response = proto
          .AccessGrantExchangeResponse()
          .withError(
            proto.AccessGrantExchangeResponse.Error.INVALID_GRANT
          )
        val ctx = ctxFactory(response)

        ctx.result ==== NotValid(List("invalid_grant")).bad
      }
    }

    "when the response is INVALID_SCOPE" >> {
      "it returns a NotAuthorized error" in {
        val response = proto
          .AccessGrantExchangeResponse()
          .withError(
            proto.AccessGrantExchangeResponse.Error.INVALID_SCOPE
          )
        val ctx = ctxFactory(response)

        ctx.result ==== NotValid("invalid_scope").bad
      }
    }

    "when the response is an unknown error" >> {
      "it throws an IllegalArgumentException" in {
        val response = proto
          .AccessGrantExchangeResponse()
          .withError(
            proto.AccessGrantExchangeResponse.Error.fromValue(100)
          )
        val ctx = ctxFactory(response)

        ctx.result must throwAn[IllegalArgumentException]
      }
    }
  }
}
