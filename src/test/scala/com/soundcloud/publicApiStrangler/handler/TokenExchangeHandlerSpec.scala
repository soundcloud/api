package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.service.oauth.AuthorizationService
import com.soundcloud.publicApiStrangler.support.oauth._
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Await, Future}

class TokenExchangeHandlerSpec extends UnitSpecification {
  "#instrumentedMothershipDispatch" >> {
    trait Context extends Scope {
      val telemetry = Telemetry.createIsolatedInstance

      val dispatchToMothershipHandler: Handler = _ => Future.value(Response(Status.Ok))

      val credential = ClientCredential(id = "s6BhdRkqt3", secret = "47HDu8s")
      val grant = ClientCredentialsGrant()

      val tokenExchangeRequestParseResult: Either[TokenExchangeRequestError, TokenExchangeRequest] =
        Right(TokenExchangeRequest(credential, grant))

      val authorizationService = mock[AuthorizationService]
      authorizationService.validateAccessGrant(credential, grant).returns(Future.value(true))

      val handler =
        new TokenExchangeHandler(
          dispatchToMothershipHandler,
          telemetry,
          _ => tokenExchangeRequestParseResult,
          authorizationService
        )

      def getGrantTypeCount(grantTypeValue: String, responseStatusValue: String, isValidValue: String): Double = {
        telemetry
          .getSampleValue(
            "oauth_token_exchange_grant_type",
            Array("grant_type", "response_status", "is_valid"),
            Array(grantTypeValue, responseStatusValue, isValidValue)
          )
          .get
      }

      def getRequestErrorCount(errorValue: String, reasonValue: String, responseStatusValue: String): Double = {
        telemetry
          .getSampleValue(
            "oauth_token_exchange_error",
            Array("error", "reason", "response_status"),
            Array(errorValue, reasonValue, responseStatusValue)
          )
          .get
      }
    }

    trait WithMockRequestContext extends Context {
      val request: HandlerRequest = mock[HandlerRequest]
    }

    "when the request can be parsed" >> {
      "proxies the request to the dispatch handler" in new WithMockRequestContext {
        val result = Await.result(handler.instrumentedMothershipDispatch(request))

        result.status ==== Status.Ok
      }

      "counts grant type and validation result" in new WithMockRequestContext {
        Await.result(handler.instrumentedMothershipDispatch(request))

        getGrantTypeCount("client_credentials", "200", "true") ==== 1.0
      }
    }

    "when the request cannot be parsed" >> {
      trait UnparseableRequestContext extends WithMockRequestContext {
        override val tokenExchangeRequestParseResult = Left(UnparseableRequest(None))
      }

      "fails the request without proxying to the dispatch handler" in new UnparseableRequestContext {
        val result = Await.result(handler.instrumentedMothershipDispatch(request))

        result.status ==== Status.BadRequest
      }

      "counts error type" in new UnparseableRequestContext {
        Await.result(handler.instrumentedMothershipDispatch(request))

        getRequestErrorCount("unparseable_request_body", "unknown", "400") ==== 1.0
      }
    }
  }
}
