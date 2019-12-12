package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Await, Future}

class TokenExchangeHandlerSpec extends UnitSpecification {
  "#instrumentedMothershipDispatch" >> {
    trait Context extends Scope {
      val metrics: TokenExchangeHandler.Metrics = new TokenExchangeHandler.Metrics(Telemetry.createIsolatedInstance)

      lazy val handler = new TokenExchangeHandler(dispatchToMothershipHandler, metrics)

      val request: HandlerRequest = mock[HandlerRequest]
      val mothershipResponse = Response(Status.Ok)

      val dispatchToMothershipHandler: Handler = {
        case r if r == request => {
          Future.value(mothershipResponse)
        }
      }
    }

    "proxies the request to the dispatch handler" in new Context {
      Await.result(handler.instrumentedMothershipDispatch(request)) ==== mothershipResponse
    }

    "instruments the request body contents" >> {
      "for invalid requests" >> {
        trait InvalidRequestContext extends Context {
          override val mothershipResponse = Response(Status.Unauthorized)
        }

        "unexpected error" in new InvalidRequestContext {
          request.contentType throws new RuntimeException

          // Ensure that failing instrumentation does not affect the dispatch.
          Await.result(handler.instrumentedMothershipDispatch(request)) ==== mothershipResponse

          metrics.requestErrorCounter.labels("unexpected_error", "401").get ==== 1.0
        }

        "missing content type" in new InvalidRequestContext {
          request.contentType returns None
          Await.result(handler.instrumentedMothershipDispatch(request))

          metrics.requestErrorCounter.labels("missing_content_type", "401").get ==== 1.0
        }

        "unsupported content type" in new InvalidRequestContext {
          request.contentType returns Some("application/json")
          Await.result(handler.instrumentedMothershipDispatch(request))

          metrics.requestErrorCounter.labels("unsupported_content_type", "401").get ==== 1.0
        }

        "non-standard multipart content type" in new InvalidRequestContext {
          request.contentType returns Some(
            "multipart/what-ever+but-most-probably-form-data; boundary=------------nx-skip-this;and-this-too\""
          )
          Await.result(handler.instrumentedMothershipDispatch(request))

          metrics.requestErrorCounter
            .labels("non_standard_multipart_content_type: multipart/what-ever+but-most-probably-form-data", "401")
            .get ==== 1.0
        }

        "incorrect content format" in new InvalidRequestContext {
          request.contentType returns Some("application/x-www-form-urlencoded")
          request.contentString returns
            """{"this-is-not": "form-urlencoded", "it-is": "json"}"""

          Await.result(handler.instrumentedMothershipDispatch(request))

          metrics.requestErrorCounter.labels("invalid_request", "401").get ==== 1.0
        }

        "missing grant type" in new InvalidRequestContext {
          request.contentType returns Some("application/x-www-form-urlencoded")
          request.contentString returns
            "client_id=s6BhdRkqt3&" +
              "client_secret=gX1fBat3bV&code=i1WsRn1uB1&" +
              "redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb"

          Await.result(handler.instrumentedMothershipDispatch(request))

          metrics.requestErrorCounter.labels("invalid_request", "401").get ==== 1.0
        }

        "invalid grant type" in new InvalidRequestContext {
          request.contentType returns Some("application/x-www-form-urlencoded")
          request.contentString returns "grant_type=unsupported"

          Await.result(handler.instrumentedMothershipDispatch(request))

          metrics.requestErrorCounter.labels("unsupported_grant_type", "401").get ==== 1.0
        }
      }

      "for valid requests" >> {
        trait ValidRequestContext extends Context {
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
              "client_secret=47HDu8s&username=johndoe&password=A3ddj3w"

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
    }
  }
}
