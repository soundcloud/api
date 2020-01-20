package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.client.{Params, StringRequestBuilder}
import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.publicApiStrangler.handler.TokenExchangeHandler.TokenExchangeRequest
import com.soundcloud.publicApiStrangler.handler.TokenExchangeHandler.TokenExchangeRequest.RequestError
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{Method, Request, RequestBuilder, Response, Status}
import com.twitter.util.{Await, Future}

class TokenExchangeHandlerSpec extends UnitSpecification {
  "TokenExchangeRequest deserialization" >> {
    trait WithMockRequestContext extends Scope {
      val request: HandlerRequest = mock[HandlerRequest]
    }

    trait MultipartRequestContext extends Scope {
      def multipartRequest(fields: Seq[(String, String)]): Request =
        RequestBuilder()
          .url("http:///")
          .addFormElement(fields: _*)
          .buildFormPost(multipart = true)

      val parameters: Seq[(String, String)]

      lazy val request: HandlerRequest =
        HandlerRequest(
          multipartRequest(
            parameters
          )
        )
    }

    "of invalid requests" >> {
      "results in unparseable request body for unexpected parse errors" in new WithMockRequestContext {
        request.contentType throws new RuntimeException

        TokenExchangeRequest.parse(request) ==== Left(
          TokenExchangeRequest.UnparseableRequestBody
        )
      }

      "results in invalid request when request body is malformed" in new WithMockRequestContext {
        request.contentString returns
          """{"this-is-not": "form-urlencoded", "it-is": "json"}"""

        TokenExchangeRequest.parse(request) ==== Left(
          TokenExchangeRequest.InvalidRequest("missing_grant_type")
        )
      }

      "results in invalid request when missing grant type" in new WithMockRequestContext {
        request.contentString returns
          "client_id=s6BhdRkqt3&" +
            "client_secret=gX1fBat3bV&code=i1WsRn1uB1&" +
            "redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb"

        TokenExchangeRequest.parse(request) ==== Left(
          TokenExchangeRequest.InvalidRequest("missing_grant_type")
        )
      }

      "results in unsupported grant type when grant type is not supported" in new WithMockRequestContext {
        request.contentString returns "grant_type=unsupported"

        TokenExchangeRequest.parse(request) ==== Left(
          TokenExchangeRequest.UnsupportedGrantType
        )
      }

      "results in invalid request when multipart request body is malformed" in new MultipartRequestContext {
        val parameters = Seq()

        override lazy val request: HandlerRequest =
          HandlerRequest(
            new StringRequestBuilder().build(
              Method.Post,
              Path("/"),
              Params.empty,
              Headers("Content-Type" -> "multipart/fake-data"),
              Some("""{"this-is-not": "multipart/", "it-is": "json"}""")
            )
          )

        TokenExchangeRequest.parse(request) ==== Left(
          TokenExchangeRequest.InvalidRequest("missing_grant_type")
        )
      }
    }

    """emulation of Ruby on Rails' "last write wins" for parameter hashes""" >> {
      "uses the last value if preceding value is blank" in new WithMockRequestContext {
        request.contentString returns
          "grant_type=&grant_type=client_credentials&client_id=s6BhdRkqt3&" +
            "client_secret=47HDu8s"

        TokenExchangeRequest.parse(request) must beLike {
          case Right(tokenExchangeRequest) => {
            tokenExchangeRequest.accessGrant ==== TokenExchangeRequest.ClientCredentialsGrant
          }
        }
      }

      "uses the last value if preceding value is non-blank" in new WithMockRequestContext {
        request.contentString returns
          "grant_type=client_credentials&grant_type=last_write_wins&client_id=s6BhdRkqt3&" +
            "client_secret=47HDu8s"

        TokenExchangeRequest.parse(request) must beLeft[RequestError]
      }
    }

    "of request with access grant of type" >> {
      "client credentials" >> {
        // https://tools.ietf.org/html/draft-ietf-oauth-v2-13#section-4.4
        // Introduced in Mothership with
        // https://github.com/soundcloud/soundcloud/commit/f23da9bd5d3469b62b71f2ac72a5fe1fafce9786
        "succeeds when all required information is present" in new WithMockRequestContext {
          request.contentString returns
            "grant_type=client_credentials&client_id=s6BhdRkqt3&" +
              "client_secret=47HDu8s"

          TokenExchangeRequest.parse(request) must beLike {
            case Right(tokenExchangeRequest) => {
              tokenExchangeRequest.clientCredentials ==== TokenExchangeRequest
                .ClientCredentials("s6BhdRkqt3", "47HDu8s")
              tokenExchangeRequest.accessGrant ==== TokenExchangeRequest.ClientCredentialsGrant
            }
          }
        }

        "fails for missing client id" in new WithMockRequestContext {
          request.contentString returns
            "grant_type=refresh_token&client_id=s6BhdRkqt3&" +
              "refresh_token=n4E9O119d"

          TokenExchangeRequest.parse(request) ==== Left(
            TokenExchangeRequest.InvalidRequest("missing_client_credentials")
          )
        }

        "fails for missing client secret" in new WithMockRequestContext {
          request.contentString returns
            "grant_type=refresh_token&" +
              "client_secret=gX1fBat3bV&refresh_token=n4E9O119d"

          TokenExchangeRequest.parse(request) ==== Left(
            TokenExchangeRequest.InvalidRequest("missing_client_credentials")
          )
        }
      }

      "authorization code" >> {
        // https://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.1
        "succeeds when all required information is present" in new WithMockRequestContext {
          request.contentString returns
            "grant_type=authorization_code&client_id=s6BhdRkqt3&" +
              "client_secret=gX1fBat3bV&" +
              "code=i1WsRn1uB1&" +
              "redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb"

          TokenExchangeRequest.parse(request) must beLike {
            case Right(tokenExchangeRequest) =>
              tokenExchangeRequest.accessGrant ==== TokenExchangeRequest
                .AuthorizationCode(
                  "i1WsRn1uB1",
                  "https://client.example.com/cb"
                )
          }
        }

        "succeeds for valid multipart encoded request" in new MultipartRequestContext {
          val parameters = Seq(
            "grant_type" -> "authorization_code",
            "client_id" -> "s6BhdRkqt3",
            "client_secret" -> "gX1fBat3bV",
            "code" -> "i1WsRn1uB1",
            "redirect_uri" -> "https://client.example.com/cb"
          )

          TokenExchangeRequest.parse(request) must beLike {
            case Right(tokenExchangeRequest) =>
              tokenExchangeRequest.accessGrant ==== TokenExchangeRequest
                .AuthorizationCode(
                  "i1WsRn1uB1",
                  "https://client.example.com/cb"
                )
          }
        }

        "fails for missing authorization code" in new WithMockRequestContext {
          request.contentString returns
            "grant_type=authorization_code&client_id=s6BhdRkqt3&" +
              "client_secret=gX1fBat3bV&" +
              "redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb"

          TokenExchangeRequest.parse(request) ==== Left(
            TokenExchangeRequest.InvalidRequest("incomplete_grant_information")
          )
        }

        "fails for missing redirect URI" in new WithMockRequestContext {
          request.contentString returns
            "grant_type=authorization_code&client_id=s6BhdRkqt3&" +
              "client_secret=gX1fBat3bV&code=i1WsRn1uB1"

          TokenExchangeRequest.parse(request) ==== Left(
            TokenExchangeRequest.InvalidRequest("incomplete_grant_information")
          )
        }
      }

      "resource owner password credentials" >> {
        // https://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.2
        "succeeds when all required information is present" in new WithMockRequestContext {
          request.contentString returns
            "grant_type=password&client_id=s6BhdRkqt3&" +
              "client_secret=47HDu8s&username=johndoe&password=A3ddj3w"

          TokenExchangeRequest.parse(request) must beLike {
            case Right(tokenExchangeRequest) =>
              tokenExchangeRequest.accessGrant ==== TokenExchangeRequest
                .ResourceOwnerPasswordCredentials("johndoe", "A3ddj3w")
          }
        }

        "succeeds for valid multipart encoded request" in new MultipartRequestContext {
          val parameters = Seq(
            "grant_type" -> "password",
            "client_id" -> "s6BhdRkqt3",
            "client_secret" -> "47HDu8s",
            "username" -> "johndoe",
            "password" -> "A3ddj3w"
          )

          TokenExchangeRequest.parse(request) must beLike {
            case Right(tokenExchangeRequest) =>
              tokenExchangeRequest.accessGrant ==== TokenExchangeRequest
                .ResourceOwnerPasswordCredentials("johndoe", "A3ddj3w")
          }
        }

        "fails for missing username" in new WithMockRequestContext {
          request.contentString returns
            "grant_type=password&client_id=s6BhdRkqt3&" +
              "client_secret=47HDu8s&password=A3ddj3w"

          TokenExchangeRequest.parse(request) ==== Left(
            TokenExchangeRequest.InvalidRequest("incomplete_grant_information")
          )
        }

        "fails for missing password" in new WithMockRequestContext {
          request.contentString returns
            "grant_type=password&client_id=s6BhdRkqt3&" +
              "client_secret=47HDu8s&username=johndoe"

          TokenExchangeRequest.parse(request) ==== Left(
            TokenExchangeRequest.InvalidRequest("incomplete_grant_information")
          )
        }
      }

      "refresh token" >> {
        // https://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.4
        "succeeds when all required information is present" in new WithMockRequestContext {
          request.contentString returns
            "grant_type=refresh_token&client_id=s6BhdRkqt3&" +
              "client_secret=8eSEIpnqmM&refresh_token=n4E9O119d"

          TokenExchangeRequest.parse(request) must beLike {
            case Right(tokenExchangeRequest) =>
              tokenExchangeRequest.accessGrant ==== TokenExchangeRequest
                .RefreshToken("n4E9O119d")
          }
        }

        "succeeds for valid multipart encoded request" in new MultipartRequestContext {
          val parameters = Seq(
            "grant_type" -> "refresh_token",
            "client_id" -> "s6BhdRkqt3",
            "client_secret" -> "8eSEIpnqmM",
            "refresh_token" -> "n4E9O119d"
          )

          TokenExchangeRequest.parse(request) must beLike {
            case Right(tokenExchangeRequest) =>
              tokenExchangeRequest.accessGrant ==== TokenExchangeRequest
                .RefreshToken("n4E9O119d")
          }
        }

        "fails for missing refresh token" in new WithMockRequestContext {
          request.contentString returns
            "grant_type=refresh_token&client_id=s6BhdRkqt3&" +
              "client_secret=8eSEIpnqmM"

          TokenExchangeRequest.parse(request) ==== Left(
            TokenExchangeRequest.InvalidRequest("incomplete_grant_information")
          )
        }
      }
    }
  }

  "#instrumentedMothershipDispatch" >> {
    trait Context extends Scope {
      val metrics: TokenExchangeHandler.Metrics =
        new TokenExchangeHandler.Metrics(Telemetry.createIsolatedInstance)

      val dispatchToMothershipHandler: Handler =
        _ => Future.value(mothershipResponse)

      val tokenExchangeRequestParseResult: TokenExchangeRequest.ParseResult =
        Right(
          TokenExchangeRequest(
            TokenExchangeRequest
              .ClientCredentials("s6BhdRkqt3", "47HDu8s"),
            TokenExchangeRequest.ClientCredentialsGrant
          )
        )

      lazy val handler =
        new TokenExchangeHandler(
          dispatchToMothershipHandler,
          metrics,
          _ => tokenExchangeRequestParseResult
        )

      val mothershipResponseStatus: Status = Status.Ok
      lazy val mothershipResponse: Response = Response(mothershipResponseStatus)

      def getGrantTypeCount(labelValue: String): Double = {
        metrics.grantTypeCounter
          .labels(labelValue, mothershipResponseStatus.code.toString)
          .get
      }

      def getRequestErrorCount(labelValue: String): Double = {
        metrics.requestErrorCounter
          .labels(labelValue, mothershipResponseStatus.code.toString)
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

      "counts error type when request is invalid" in new WithMockRequestContext {
        override val tokenExchangeRequestParseResult =
          Left(TokenExchangeRequest.InvalidRequest("reason"))

        Await.result(handler.instrumentedMothershipDispatch(request))

        getRequestErrorCount("invalid_request:reason") ==== 1.0
      }

      "counts error type when parsing fails" in new WithMockRequestContext {
        override val tokenExchangeRequestParseResult =
          Left(TokenExchangeRequest.UnparseableRequestBody)

        Await.result(handler.instrumentedMothershipDispatch(request))

        getRequestErrorCount("unparseable_request_body") ==== 1.0
      }
    }
  }
}
