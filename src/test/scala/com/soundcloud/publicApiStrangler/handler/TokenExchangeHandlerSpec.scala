package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.support.oauth.{
  ClientCredential,
  ClientCredentialsGrant,
  TokenExchangeRequest,
  TokenExchangeRequestError,
  UnparseableRequest
}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Await, Future}

class TokenExchangeHandlerSpec extends UnitSpecification {
  "#instrumentedMothershipDispatch" >> {
    trait Context extends Scope {
      val telemetry = Telemetry.createIsolatedInstance

      val dispatchToMothershipHandler: Handler =
        _ => Future.value(mothershipResponse)

      val tokenExchangeRequestParseResult: Either[TokenExchangeRequestError, TokenExchangeRequest] =
        Right(
          TokenExchangeRequest(
            accessGrant = ClientCredentialsGrant(),
            clientCredential = ClientCredential(id = "s6BhdRkqt3", secret = "47HDu8s")
          )
        )

      lazy val handler =
        new TokenExchangeHandler(
          dispatchToMothershipHandler,
          telemetry,
          _ => tokenExchangeRequestParseResult
        )

      val mothershipResponseStatus: Status = Status.Ok
      lazy val mothershipResponse: Response = Response(mothershipResponseStatus)

      def getGrantTypeCount(grantTypeValue: String): Double = {
        telemetry
          .getSampleValue(
            "oauth_token_exchange_grant_type",
            Array("grant_type", "response_status"),
            Array(grantTypeValue, mothershipResponseStatus.code.toString)
          )
          .get
      }

      def getRequestErrorCount(errorValue: String, reasonValue: String): Double = {
        telemetry
          .getSampleValue(
            "oauth_token_exchange_error",
            Array("error", "reason", "response_status"),
            Array(errorValue, reasonValue, mothershipResponseStatus.code.toString)
          )
          .get
      }
    }

    trait WithMockRequestContext extends Context {
      val request: HandlerRequest = mock[HandlerRequest]
    }

    "proxies the request to the dispatch handler" in new WithMockRequestContext {
      Await.result(handler.instrumentedMothershipDispatch(request)) ==== mothershipResponse
    }

    "instruments the request body parse result" >> {
      "counts grant type when request is valid" in new WithMockRequestContext {
        Await.result(handler.instrumentedMothershipDispatch(request))

        getGrantTypeCount("client_credentials") ==== 1.0
      }

      "counts error type when parsing fails" in new WithMockRequestContext {
        override val tokenExchangeRequestParseResult =
          Left(UnparseableRequest(None))

        Await.result(handler.instrumentedMothershipDispatch(request))

        getRequestErrorCount("unparseable_request_body", "unknown") ==== 1.0
      }
    }
  }
}
