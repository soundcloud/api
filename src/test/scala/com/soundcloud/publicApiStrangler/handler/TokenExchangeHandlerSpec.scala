package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest}
import com.soundcloud.jvmkit.module.outcome.{GoodOps, NotAllowed, NotAuthorized, NotValid, OutcomeF}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.service.oauth.{AccessTokenResponse, AuthorizationService, TokenExchangeService}
import com.soundcloud.publicApiStrangler.support.oauth._
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Await, Future}
import org.specs2.matcher.DataTables
import play.api.libs.json.{JsDefined, JsString, Json}

class TokenExchangeHandlerSpec extends UnitSpecification with DataTables {
  trait Context extends Scope {
    val telemetry: Telemetry = Telemetry.createIsolatedInstance

    val dispatchToMothershipHandler: Handler = _ => Future.value(Response(Status.Ok))

    val credential: ClientCredential = ClientCredential(id = "s6BhdRkqt3", secret = "47HDu8s")
    val grant: ClientCredentialsGrant = ClientCredentialsGrant(Set.empty)
    lazy val context: RequestContext = RequestContext("0.1.2.3", "Netscape Navigator 0.86 Beta 3")

    val tokenExchangeRequestParseResult: Either[TokenExchangeRequestError, TokenExchangeRequest] =
      Right(TokenExchangeRequest(credential, grant, context))

    val authorizationService: AuthorizationService = mock[AuthorizationService]
    authorizationService.validateAccessGrant(===(credential), ===(grant), any()).returns(Future.value(true))

    val tokenExchangeService = mock[TokenExchangeService]
    def tokenExchangeResponse: OutcomeF[AccessTokenResponse] = AccessTokenResponse("", None, None, Seq.empty).goodF
    tokenExchangeService.exchange(any()) returns tokenExchangeResponse

    def authenticatorClientIdList: Set[String] = Set.empty

    val handler =
      new TokenExchangeHandler(
        telemetry,
        _ => tokenExchangeRequestParseResult,
        tokenExchangeService
      )

    def getBadRequestCount(errorValue: String): Double = {
      telemetry
        .getSampleValue(
          "oauth_grant_exchange_bad_request_total",
          Array("reason"),
          Array(errorValue)
        )
        .getOrElse(0.0)
        .toInt
    }

    def getGrantExchangeCount(grantType: AccessGrant, status: Status, reason: String = ""): Int = {
      telemetry
        .getSampleValue(
          "oauth_grant_exchange_total",
          Array("grant_type", "status", "reason"),
          Array(grantType.grantType, status.code.toString, reason)
        )
        .getOrElse(0.0)
        .toInt
    }

    val request: HandlerRequest = mock[HandlerRequest]

    lazy val result = Await.result(handler(request))
  }

  "when the request can be parsed" >> {
    "and the grant type is supported" >> {

      "it sends the request to the TokenExchangeService" in new Context {
        result
        there was one(tokenExchangeService).exchange(TokenExchangeRequest(credential, grant, context))
      }

      "it counts the grant type and validation result" in new Context {
        result
        getGrantExchangeCount(grant, Status.Ok) ==== 1
      }

      "when the exchange was successful" >> {
        "it returns an ok response the given response content" >> {
          def expectedJson(
              accessToken: String,
              expiresIn: Option[Int] = None,
              refreshToken: Option[String] = None,
              scope: String = ""
          ) = {
            val base = Json.obj("access_token" -> accessToken, "scope" -> scope)
            val expiresInJson = expiresIn.map(value => Json.obj("expires_in" -> value)).getOrElse(Json.obj())
            val refreshTokenJson =
              refreshToken.map(value => Json.obj("refresh_token" -> value)).getOrElse(Json.obj())
            base ++ expiresInJson ++ refreshTokenJson
          }

          // @formatter:off
              "serviceResult"                                                 | "expectedJson"                                                 |>
              AccessTokenResponse("aaa", None, None, Seq.empty)               ! expectedJson("aaa")                                 |
              AccessTokenResponse("aaa", Some(123), None, Seq.empty)          ! expectedJson("aaa", Some(123))                      |
              AccessTokenResponse("aaa", None, Some("refresh"), Seq.empty)    ! expectedJson("aaa", refreshToken = Some("refresh")) |
              AccessTokenResponse("aaa", None, None, Seq("scope1", "scope2")) ! expectedJson("aaa", scope = "scope1 scope2")        |>
              // @formatter:on
          { (serviceResult, expectedJson) =>
            new Context {
              override def tokenExchangeResponse = serviceResult.goodF

              result.status ==== Status.Ok
              Json.parse(result.contentString) ==== expectedJson
            }
          }
        }

        "it counts the grant type and status" in new Context {
          override def tokenExchangeResponse: OutcomeF[AccessTokenResponse] =
            AccessTokenResponse("aaa", None, None, Seq.empty).goodF
          result
          getGrantExchangeCount(grant, Status.Ok) ==== 1
          getGrantExchangeCount(grant, Status.BadRequest) ==== 0
        }
      }

      "when the exchange fails" >> {
        "it returns an error object with the appropriate message and error code" >> {
          // @formatter:off
              "serviceResult"           | "expectedMessage" | "expectedStatus"    |>
              NotValid("invalid_grant") ! "invalid_grant"   ! Status.Unauthorized |
              NotAuthorized("gah!")     ! "gah!"            ! Status.Unauthorized |
              NotAllowed("noooo")       ! ""                ! Status.BadRequest   |>
              // @formatter:on
          { (serviceResult, expectedMessage, expectedStatus) =>
            new Context {
              override def tokenExchangeResponse = serviceResult.badF

              result.status ==== expectedStatus
              (Json.parse(result.contentString) \ "message").get ==== JsString(expectedMessage)
              (Json.parse(result.contentString) \ "error_code").get ==== JsString(expectedMessage)
            }
          }
        }

        "it counts the grant type, status and failure reason" in new Context {
          override def tokenExchangeResponse: OutcomeF[AccessTokenResponse] = NotValid("invalid_grant").badF
          result
          getGrantExchangeCount(grant, Status.Unauthorized, "invalid_grant") ==== 1
          getGrantExchangeCount(grant, Status.BadRequest, "other_failure") ==== 0
        }
      }
    }
  }

  "but statically invalid" >> {
    trait InvalidGrantContext extends Context {
      override val tokenExchangeRequestParseResult = Left(InvalidGrant("password"))
    }

    "fails the request without proxying to the dispatch handler" in new InvalidGrantContext {
      result.status ==== Status.BadRequest
      (Json.parse(result.contentString) \ "error_code") ==== JsDefined(JsString("invalid_grant"))
    }

    "counts error type" in new InvalidGrantContext {
      result
      getBadRequestCount("invalid_grant") ==== 1
    }
  }

  "but the grant type is not supported" >> {
    trait UnsupportedGrantTypeContext extends Context {
      override val tokenExchangeRequestParseResult = Left(UnsupportedGrantType("this_type_is_not_supported"))
    }

    "fails the request without proxying to the dispatch handler" in new UnsupportedGrantTypeContext {
      result.status ==== Status.BadRequest
      (Json.parse(result.contentString) \ "error_code") ==== JsDefined(JsString("unsupported_grant_type"))
    }

    "counts error type" in new UnsupportedGrantTypeContext {
      result
      getBadRequestCount("unsupported_grant_type") ==== 1
    }
  }

  "when the request cannot be parsed" >> {
    trait UnparseableRequestContext extends Context {
      override val tokenExchangeRequestParseResult = Left(UnparseableRequest(None))
    }

    "fails the request without proxying to the dispatch handler" in new UnparseableRequestContext {
      result.status ==== Status.BadRequest
      (Json.parse(result.contentString) \ "error_code") ==== JsDefined(JsString("invalid_request"))
    }

    "counts error type" in new UnparseableRequestContext {
      result
      getBadRequestCount("unparseable_request_body") ==== 1
    }
  }
}
