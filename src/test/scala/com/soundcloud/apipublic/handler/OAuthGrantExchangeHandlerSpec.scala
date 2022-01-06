package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.outcome.{GoodOps, NotAllowed, NotAuthorized, NotFound, NotValid, Outcome}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.apipublic.service.oauth.{AccessTokenResponse, GrantExchangeService}
import com.soundcloud.apipublic.support.oauth._
import com.soundcloud.apipublic.test.UnitSpecification
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}
import org.specs2.matcher.DataTables
import play.api.libs.json.{JsDefined, JsString, Json}

class OAuthGrantExchangeHandlerSpec extends UnitSpecification with DataTables {
  trait Context extends Scope {
    val telemetry: Telemetry = Telemetry.createIsolatedInstance

    lazy val credential: ClientCredential = ClientCredential(id = "s6BhdRkqt3", secret = "47HDu8s")
    val grant: ClientCredentialsGrant = ClientCredentialsGrant(Set.empty)
    lazy val context: RequestContext = RequestContext("0.1.2.3", "Netscape Navigator 0.86 Beta 3")

    val grantExchangeRequestParseResult: Either[GrantExchangeRequestError, GrantExchangeRequest] =
      Right(GrantExchangeRequest(credential, grant, context))

    val grantExchangeService = mock[GrantExchangeService]
    def grantExchangeResponse: Outcome[AccessTokenResponse] = AccessTokenResponse("", None, None, Seq.empty).good
    grantExchangeService.exchange(any()) returns Future.value(grantExchangeResponse)

    val handler =
      new OauthGrantExchangeHandler(
        telemetry,
        _ => grantExchangeRequestParseResult,
        grantExchangeService
      )

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

    def getBadRequestCount(reason: String = ""): Int = {
      telemetry
        .getSampleValue(
          "oauth_grant_exchange_total",
          Array("grant_type", "status", "reason"),
          Array("unknown", "400", reason)
        )
        .getOrElse(0.0)
        .toInt
    }

    val request: HandlerRequest = mock[HandlerRequest]

    lazy val result = Await.result(handler(request))
  }

  "when the request can be parsed" >> {
    "and the grant type is supported" >> {
      "and statically valid" >> {
        "it sends the request to the TokenExchangeService" in new Context {
          result
          there was one(grantExchangeService).exchange(GrantExchangeRequest(credential, grant, context))
        }

        "when the exchange was successful" >> {
          "it returns an ok response the given response content" >> {
            def expectedJson(
                accessToken: String,
                expiresIn: Option[Int] = None,
                refreshToken: Option[String] = None,
                scope: String = ""
            ) = {
              val base = Json.obj("access_token" -> accessToken, "scope" -> scope, "token_type" -> "bearer")
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
                override def grantExchangeResponse = serviceResult.good

                result.status ==== Status.Ok
                Json.parse(result.contentString) ==== expectedJson
              }
            }
          }

          "it counts the grant type and status" in new Context {
            override def grantExchangeResponse: Outcome[AccessTokenResponse] =
              AccessTokenResponse("aaa", None, None, Seq.empty).good
            result
            getGrantExchangeCount(grant, Status.Ok) ==== 1
            getGrantExchangeCount(grant, Status.BadRequest) ==== 0
          }

          "it returns a response with content type json" in new Context {
            result
            result.contentType ==== Some("application/json; charset=utf-8")
          }
        }

        "when the exchange fails" >> {
          "it returns an error object with the appropriate message and error code" >> {
            // @formatter:off
              "serviceResult"           | "expectedMessage" | "expectedStatus"       |>
              NotValid("invalid_grant") ! "invalid_grant"   ! Status.Unauthorized    |
              NotAuthorized("gah!")     ! "gah!"            ! Status.Unauthorized    |
              NotAllowed("irks")        ! "irks"            ! Status.TooManyRequests |
              NotFound("noooo")         ! ""                ! Status.BadRequest      |>
              // @formatter:on
            { (serviceResult, expectedMessage, expectedStatus) =>
              new Context {
                override def grantExchangeResponse = serviceResult.bad

                result.status ==== expectedStatus
                (Json.parse(result.contentString) \ "message").get ==== JsString(expectedMessage)
                (Json.parse(result.contentString) \ "error_code").get ==== JsString(expectedMessage)
              }
            }
          }

          "it counts the grant type, status and failure reason" in new Context {
            override def grantExchangeResponse: Outcome[AccessTokenResponse] = NotValid("invalid_grant").bad
            result
            getGrantExchangeCount(grant, Status.Unauthorized, "invalid_grant") ==== 1
            getGrantExchangeCount(grant, Status.Unauthorized, "other_failure") ==== 0
          }
        }
      }

      "but statically invalid" >> {
        trait InvalidGrantContext extends Context {
          override val grantExchangeRequestParseResult = Left(InvalidGrant("password"))
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
    }

    "but the grant type is not supported" >> {
      trait UnsupportedGrantTypeContext extends Context {
        override val grantExchangeRequestParseResult = Left(UnsupportedGrantType("this_type_is_not_supported"))
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
  }

  "when the request cannot be parsed" >> {
    trait UnparseableRequestContext extends Context {
      override val grantExchangeRequestParseResult = Left(UnparseableRequest(None))
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
