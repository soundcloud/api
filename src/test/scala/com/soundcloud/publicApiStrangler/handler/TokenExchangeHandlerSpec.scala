package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.service.oauth.AuthorizationService
import com.soundcloud.publicApiStrangler.support.oauth._
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Await, Future}
import play.api.libs.json.{JsDefined, JsString, Json}

class TokenExchangeHandlerSpec extends UnitSpecification {
  "#instrumentedMothershipDispatch" >> {
    trait Context extends Scope {
      val telemetry: Telemetry = Telemetry.createIsolatedInstance

      val dispatchToMothershipHandler: Handler = _ => Future.value(Response(Status.Ok))

      val credential: ClientCredential = ClientCredential(id = "s6BhdRkqt3", secret = "47HDu8s")
      val grant: ClientCredentialsGrant = ClientCredentialsGrant()

      val tokenExchangeRequestParseResult: Either[TokenExchangeRequestError, TokenExchangeRequest] =
        Right(TokenExchangeRequest(credential, grant))

      val authorizationService: AuthorizationService = mock[AuthorizationService]
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
      "and the grant type is supported" >> {
        "and statically valid" >> {
          "proxies the request to the dispatch handler" in new WithMockRequestContext {
            val result: Response = Await.result(handler.instrumentedMothershipDispatch(request))

            result.status ==== Status.Ok
          }

          "counts grant type and validation result" in new WithMockRequestContext {
            Await.result(handler.instrumentedMothershipDispatch(request))

            getGrantTypeCount("client_credentials", "200", "true") ==== 1.0
          }
        }

        "but statically invalid" >> {
          trait InvalidGrantContext extends WithMockRequestContext {
            override val tokenExchangeRequestParseResult = Left(InvalidGrant("password"))
          }

          "fails the request without proxying to the dispatch handler" in new InvalidGrantContext {
            val result: Response = Await.result(handler.instrumentedMothershipDispatch(request))

            result.status ==== Status.BadRequest
            (Json.parse(result.contentString) \ "error_code") ==== JsDefined(JsString("invalid_grant"))
          }

          "counts error type" in new InvalidGrantContext {
            Await.result(handler.instrumentedMothershipDispatch(request))

            getRequestErrorCount("invalid_grant", "password", "400") ==== 1.0
          }
        }
      }

      "but the grant type is not supported" >> {
        trait UnsupportedGrantTypeContext extends WithMockRequestContext {
          override val tokenExchangeRequestParseResult = Left(UnsupportedGrantType("this_type_is_not_supported"))
        }

        "fails the request without proxying to the dispatch handler" in new UnsupportedGrantTypeContext {
          val result: Response = Await.result(handler.instrumentedMothershipDispatch(request))

          result.status ==== Status.BadRequest
          (Json.parse(result.contentString) \ "error_code") ==== JsDefined(JsString("unsupported_grant_type"))
        }

        "counts error type" in new UnsupportedGrantTypeContext {
          Await.result(handler.instrumentedMothershipDispatch(request))

          getRequestErrorCount("unsupported_grant_type", "this_type_is_not_supported", "400") ==== 1.0
        }
      }
    }

    "when the request cannot be parsed" >> {
      trait UnparseableRequestContext extends WithMockRequestContext {
        override val tokenExchangeRequestParseResult = Left(UnparseableRequest(None))
      }

      "fails the request without proxying to the dispatch handler" in new UnparseableRequestContext {
        val result: Response = Await.result(handler.instrumentedMothershipDispatch(request))

        result.status ==== Status.BadRequest
        (Json.parse(result.contentString) \ "error_code") ==== JsDefined(JsString("invalid_request"))
      }

      "counts error type" in new UnparseableRequestContext {
        Await.result(handler.instrumentedMothershipDispatch(request))

        getRequestErrorCount("unparseable_request_body", "unknown", "400") ==== 1.0
      }
    }
  }
}
