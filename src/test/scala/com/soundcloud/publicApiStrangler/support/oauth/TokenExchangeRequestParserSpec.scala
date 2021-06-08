package com.soundcloud.publicApiStrangler.support.oauth

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{Request, RequestBuilder}
import com.twitter.io.Buf

class TokenExchangeRequestParserSpec extends UnitSpecification {
  "parsing token exchange requests should" >> {
    trait Context extends Scope {
      val paramsParser = new RailsLikeParamsParser()
      val parser = new TokenExchangeRequestParser(paramsParser)

      val request: Request
      lazy val result: Either[TokenExchangeRequestError, TokenExchangeRequest] = parser.parse(HandlerRequest(request))
    }

    "fail for requests that can't be parsed" in new Context {
      val invalidJsonBody: Buf = Buf.Empty

      override val request: Request = RequestBuilder()
        .url(Request.queryString("http://api/test", Map("client_id" -> "s6BhdRkqt3", "client_secret" -> "secret")))
        .setHeader("Content-Type", "application/json")
        .buildPost(invalidJsonBody)

      result ==== Left(UnparseableRequest(Some("application/json")))
    }

    trait RequestWithParamsContext extends Context {
      def params: Map[String, String]

      val remoteIp = "0.1.2.3"
      val userAgent = "Netscape Navigator 0.86 Beta 3"

      override lazy val request: Request = RequestBuilder()
        .url(Request.queryString("http://api/test"))
        .setHeader("X-Real-Ip", remoteIp)
        .setHeader("User-Agent", userAgent)
        .addFormElement(params.toSeq: _*)
        .buildFormPost(multipart = false)
    }

    "fail for requests with blank grant type" in new RequestWithParamsContext {
      override val params = Map(
        "grant_type" -> "",
        "client_id" -> "s6BhdRkqt3",
        "client_secret" -> "gX1fBat3bV"
      )

      result ==== Left(InvalidRequest("missing_grant_type"))
    }

    "fail when client credentials are missing" in new RequestWithParamsContext {
      override val params = Map(
        "grant_type" -> "password",
        "username" -> "johndoe",
        "password" -> "password"
      )

      result ==== Left(MissingClientCredentials())
    }

    "fail for requests with unsupported grant type" in new RequestWithParamsContext {
      override val params = Map(
        "grant_type" -> "invalid",
        "client_id" -> "s6BhdRkqt3",
        "client_secret" -> "gX1fBat3bV"
      )

      result ==== Left(UnsupportedGrantType("invalid"))
    }

    "with supported grant types" >> {
      // https://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.1
      "return an authorization code grant" in new RequestWithParamsContext {
        override val params = Map(
          "grant_type" -> "authorization_code",
          "client_id" -> "s6BhdRkqt3",
          "client_secret" -> "gX1fBat3bV",
          "code" -> "i1WsRn1uB1",
          "redirect_uri" -> "http://redirect/callback"
        )

        result ==== Right(
          TokenExchangeRequest(
            ClientCredential("s6BhdRkqt3", "gX1fBat3bV"),
            AuthorizationCodeGrant("i1WsRn1uB1", "http://redirect/callback"),
            RequestContext(remoteIp, userAgent)
          )
        )
      }

      // https://tools.ietf.org/html/draft-ietf-oauth-v2-13#section-4.4
      // Introduced in Mothership with https://github.com/soundcloud/soundcloud/commit/f23da9bd5d3469b62b71f2ac72a5fe1fafce9786
      "when the request is for a client_credentials grant" >> {
        trait ClientCredentialsParamsContext extends RequestWithParamsContext {
          override def params = Map(
            "grant_type" -> "client_credentials",
            "client_id" -> "s6BhdRkqt3",
            "client_secret" -> "gX1fBat3bV"
          )
        }

        "when no scope parameter is included" >> {
          "it returns a ClientCredentialsGrant with empty scope" in new ClientCredentialsParamsContext {
            result ==== Right(
              TokenExchangeRequest(
                ClientCredential("s6BhdRkqt3", "gX1fBat3bV"),
                ClientCredentialsGrant(Set.empty),
                RequestContext(remoteIp, userAgent)
              )
            )
          }
        }

        "when the scope is included in the request" >> {
          "it returns a ClientCredentialsGrant with a set of access ranges" in new ClientCredentialsParamsContext {
            override def params: Map[String, String] = super.params ++ Map("scope" -> "some_range another_range")

            result ==== Right(
              TokenExchangeRequest(
                ClientCredential("s6BhdRkqt3", "gX1fBat3bV"),
                ClientCredentialsGrant(Set("some_range", "another_range")),
                RequestContext(remoteIp, userAgent)
              )
            )
          }

          "when the scope includes multiple spaces" >> {
            "it only passes non-empty access ranges" in new ClientCredentialsParamsContext {
              override def params: Map[String, String] = super.params ++ Map("scope" -> " ab c d   f")

              result should beLike {
                case Right(TokenExchangeRequest(_, ClientCredentialsGrant(scope), _)) =>
                  scope ==== Set("ab", "c", "d", "f")
              }
            }
          }
        }
      }

      // https://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.2
      "when the request is for a password grant" >> {
        trait PasswordGrantParamsContext extends RequestWithParamsContext {
          override def params = Map(
            "grant_type" -> "password",
            "client_id" -> "s6BhdRkqt3",
            "client_secret" -> "gX1fBat3bV",
            "username" -> "johndoe",
            "password" -> "A3ddj3w"
          )
        }

        "when no scope parameter is included" >> {
          "it returns a PasswordGrant with empty scopes" in new PasswordGrantParamsContext {
            result ==== Right(
              TokenExchangeRequest(
                ClientCredential("s6BhdRkqt3", "gX1fBat3bV"),
                PasswordGrant("johndoe", "A3ddj3w", Set.empty),
                RequestContext(remoteIp, userAgent)
              )
            )
          }
        }

        "when a scope parameter is included" >> {
          "it returns a PasswordGrant with a set of access ranges" in new PasswordGrantParamsContext {
            override def params = super.params ++ Map("scope" -> "range_one range_two")

            result ==== Right(
              TokenExchangeRequest(
                ClientCredential("s6BhdRkqt3", "gX1fBat3bV"),
                PasswordGrant("johndoe", "A3ddj3w", Set("range_one", "range_two")),
                RequestContext(remoteIp, userAgent)
              )
            )
          }
        }
      }

      // https://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.4
      "return a refresh token grant" in new RequestWithParamsContext {
        override val params = Map(
          "grant_type" -> "refresh_token",
          "client_id" -> "s6BhdRkqt3",
          "client_secret" -> "gX1fBat3bVt",
          "refresh_token" -> "n4E9O119d"
        )

        result ==== Right(
          TokenExchangeRequest(
            ClientCredential("s6BhdRkqt3", "gX1fBat3bVt"),
            RefreshTokenGrant("n4E9O119d"),
            RequestContext(remoteIp, userAgent)
          )
        )
      }

      "return an invalid refresh token grant" in new RequestWithParamsContext {
        override val params = Map(
          "grant_type" -> "refresh_token",
          "client_id" -> "s6BhdRkqt3",
          "client_secret" -> "gX1fBat3bVt"
        )

        result ==== Left(InvalidGrant("refresh_token"))
      }
    }
  }
}
