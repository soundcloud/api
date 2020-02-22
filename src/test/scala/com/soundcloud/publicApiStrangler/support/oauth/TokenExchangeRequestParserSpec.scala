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
      lazy val result = parser.parse(HandlerRequest(request))
    }

    "fail for requests that can't be parsed" in new Context {
      val invalidJsonBody = Buf.Empty

      override val request = RequestBuilder()
        .url(Request.queryString("http://api/test", Map("client_id" -> "s6BhdRkqt3", "client_secret" -> "secret")))
        .setHeader("Content-Type", "application/json")
        .buildPost(invalidJsonBody)

      result ==== Left(UnparseableRequest(Some("application/json")))
    }

    trait RequestWithParamsContext extends Context {
      val params: Map[String, String]

      override lazy val request = RequestBuilder()
        .url(Request.queryString("http://api/test"))
        .addFormElement(params.toSeq: _*)
        .buildFormPost(false)
    }

    "fail for requests with unsupported grant type" in new RequestWithParamsContext {
      override val params = Map(
        "grant_type" -> "invalid",
        "client_id" -> "s6BhdRkqt3",
        "client_secret" -> "gX1fBat3bV"
      )

      result ==== Left(UnsupportedGrantType(Some("invalid")))
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
            accessGrant = AuthorizationCode(code = "i1WsRn1uB1", redirectUri = "http://redirect/callback"),
            clientCredential = ClientCredential(id = "s6BhdRkqt3", secret = "gX1fBat3bV")
          )
        )
      }

      // https://tools.ietf.org/html/draft-ietf-oauth-v2-13#section-4.4
      // Introduced in Mothership with https://github.com/soundcloud/soundcloud/commit/f23da9bd5d3469b62b71f2ac72a5fe1fafce9786
      "return a client credentials grant" in new RequestWithParamsContext {
        override val params = Map(
          "grant_type" -> "client_credentials",
          "client_id" -> "s6BhdRkqt3",
          "client_secret" -> "gX1fBat3bV"
        )

        result ==== Right(
          TokenExchangeRequest(
            accessGrant = ClientCredentialsGrant(),
            clientCredential = ClientCredential(id = "s6BhdRkqt3", secret = "gX1fBat3bV")
          )
        )
      }

      // https://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.2
      "return a resource owner password credentials grant" in new RequestWithParamsContext {
        override val params = Map(
          "grant_type" -> "password",
          "client_id" -> "s6BhdRkqt3",
          "client_secret" -> "gX1fBat3bV",
          "username" -> "johndoe",
          "password" -> "A3ddj3w"
        )

        result ==== Right(
          TokenExchangeRequest(
            accessGrant = ResourceOwnerPasswordCredentials(username = "johndoe", password = "A3ddj3w"),
            clientCredential = ClientCredential(id = "s6BhdRkqt3", secret = "gX1fBat3bV")
          )
        )
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
            accessGrant = RefreshToken(refreshToken = "n4E9O119d"),
            clientCredential = ClientCredential(id = "s6BhdRkqt3", secret = "gX1fBat3bVt")
          )
        )
      }
    }
  }
}
