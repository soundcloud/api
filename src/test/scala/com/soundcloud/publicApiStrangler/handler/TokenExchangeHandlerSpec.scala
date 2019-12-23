package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.client.{Params, StringRequestBuilder}
import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{Method, Request, RequestBuilder, Response, Status}
import com.twitter.util.{Await, Future}

class TokenExchangeHandlerSpec extends UnitSpecification {
  "#instrumentedMothershipDispatch" >> {
    trait Context extends Scope {
      val metrics: TokenExchangeHandler.Metrics = new TokenExchangeHandler.Metrics(Telemetry.createIsolatedInstance)

      val dispatchToMothershipHandler: Handler = _ => Future.value(mothershipResponse)

      lazy val handler = new TokenExchangeHandler(dispatchToMothershipHandler, metrics)

      val mothershipResponseStatus: Status = Status.Ok
      lazy val mothershipResponse: Response = Response(mothershipResponseStatus)

      def getRequestErrorCount(label: String): Double = {
        metrics.requestErrorCounter.labels(label, mothershipResponseStatus.code.toString).get
      }
    }

    trait WithMockRequestContext extends Context {
      val request: HandlerRequest = mock[HandlerRequest]
    }

    "proxies the request to the dispatch handler" in new WithMockRequestContext {
      Await.result(handler.instrumentedMothershipDispatch(request)) ==== mothershipResponse
    }

    "instruments the request body contents" >> {
      "for invalid requests" >> {
        trait InvalidRequestContext extends WithMockRequestContext {
          override val mothershipResponseStatus: Status = Status.Unauthorized
        }

        "unexpected error" in new InvalidRequestContext {
          request.contentType throws new RuntimeException

          // Ensure that failing instrumentation does not affect the dispatch.
          Await.result(handler.instrumentedMothershipDispatch(request)) ==== mothershipResponse

          getRequestErrorCount("unparseable_request_body") ==== 1.0
        }

        "malformed request body" in new InvalidRequestContext {
          request.contentString returns
            """{"this-is-not": "form-urlencoded", "it-is": "json"}"""

          Await.result(handler.instrumentedMothershipDispatch(request))

          getRequestErrorCount("invalid_request") ==== 1.0
        }

        "missing grant type" in new InvalidRequestContext {
          request.contentType returns Some("application/x-www-form-urlencoded")
          request.contentString returns
            "client_id=s6BhdRkqt3&" +
              "client_secret=gX1fBat3bV&code=i1WsRn1uB1&" +
              "redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb"

          Await.result(handler.instrumentedMothershipDispatch(request))

          getRequestErrorCount("invalid_request") ==== 1.0
        }

        "invalid grant type" in new InvalidRequestContext {
          request.contentType returns Some("application/x-www-form-urlencoded")
          request.contentString returns "grant_type=unsupported"

          Await.result(handler.instrumentedMothershipDispatch(request))

          getRequestErrorCount("unsupported_grant_type") ==== 1.0
        }
      }

      "for valid requests" >> {
        trait ValidRequestContext extends WithMockRequestContext {
          request.contentType returns Some("application/x-www-form-urlencoded")
        }

        "Authorization Code" in new ValidRequestContext {
          // https://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.1
          request.contentString returns
            "grant_type=authorization_code&client_id=s6BhdRkqt3&" +
              "client_secret=gX1fBat3bV&code=i1WsRn1uB1&" +
              "redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb"

          Await.result(handler.instrumentedMothershipDispatch(request))

          metrics.grantTypeCounter.labels("authorization_code", "200").get ==== 1.0
        }

        "Resource Owner Password Credentials" in new ValidRequestContext {
          // https://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.2
          request.contentString returns
            "grant_type=password&client_id=s6BhdRkqt3&" +
              "client_secret=47HDu8s&username=johndoe&password=A3ddj3w"

          Await.result(handler.instrumentedMothershipDispatch(request))

          metrics.grantTypeCounter.labels("password", "200").get ==== 1.0
        }

        "Refresh Token" in new ValidRequestContext {
          // https://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.4
          request.contentString returns
            "grant_type=refresh_token&client_id=s6BhdRkqt3&" +
              "client_secret=8eSEIpnqmM&refresh_token=n4E9O119d"

          Await.result(handler.instrumentedMothershipDispatch(request))

          metrics.grantTypeCounter.labels("refresh_token", "200").get ==== 1.0
        }

        "Client Credentials" in new ValidRequestContext {
          // https://tools.ietf.org/html/draft-ietf-oauth-v2-13#section-4.4
          // Introduced in Mothership with
          // https://github.com/soundcloud/soundcloud/commit/f23da9bd5d3469b62b71f2ac72a5fe1fafce9786
          request.contentString returns
            "grant_type=client_credentials&client_id=s6BhdRkqt3&" +
              "client_secret=47HDu8s"

          Await.result(handler.instrumentedMothershipDispatch(request))

          metrics.grantTypeCounter.labels("client_credentials", "200").get ==== 1.0
        }
      }

      "for non-standard but supported multipart requests" >> {
        trait MultipartRequestContext extends Context {
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

        "malformed request body" in new MultipartRequestContext {
          val parameters = Seq()

          override val mothershipResponseStatus: Status = Status.BadRequest

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

          Await.result(handler.instrumentedMothershipDispatch(request))
          getRequestErrorCount("invalid_request") ==== 1.0
        }

        "for valid requests" >> {
          "Authorization Code" in new MultipartRequestContext {
            // https://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.1
            val parameters = Seq(
              "grant_type" -> "authorization_code",
              "client_id" -> "s6BhdRkqt3",
              "client_secret" -> "gX1fBat3bV",
              "code" -> "i1WsRn1uB1",
              "redirect_uri" -> "https://client.example.com/cb"
            )

            Await.result(handler.instrumentedMothershipDispatch(request))

            metrics.grantTypeCounter.labels("authorization_code", "200").get ==== 1.0
          }

          "Resource Owner Password Credentials" in new MultipartRequestContext {
            // https://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.2
            val parameters = Seq(
              "grant_type" -> "password",
              "client_id" -> "s6BhdRkqt3",
              "client_secret" -> "47HDu8s",
              "username" -> "johndoe",
              "password" -> "A3ddj3w"
            )
            Await.result(handler.instrumentedMothershipDispatch(request))

            metrics.grantTypeCounter.labels("password", "200").get ==== 1.0
          }

          "Refresh Token" in new MultipartRequestContext {
            // https://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.4
            val parameters = Seq(
              "grant_type" -> "refresh_token",
              "client_id" -> "s6BhdRkqt3",
              "client_secret" -> "8eSEIpnqmM",
              "refresh_token" -> "n4E9O119d"
            )

            Await.result(handler.instrumentedMothershipDispatch(request))

            metrics.grantTypeCounter.labels("refresh_token", "200").get ==== 1.0
          }
        }
      }
    }
  }
}
