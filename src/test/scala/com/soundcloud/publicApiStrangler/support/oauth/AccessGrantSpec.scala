package com.soundcloud.publicApiStrangler.support.oauth

import com.soundcloud.publicApiStrangler.test.UnitSpecification

class AccessGrantSpec extends UnitSpecification {
  trait Context extends Scope {
    val params: AccessGrant.Params = AccessGrant.Params.from(Map.empty)
  }

  "AuthorizationCode" >> {
    "is instantiated when params are complete" in new Context {
      override val params: AccessGrant.Params = AccessGrant.Params.from(
        Map(
          "grant_type" -> "authorization_code",
          "code" -> "theCode",
          "redirect_uri" -> "http://example/redirect"
        )
      )

      AuthorizationCodeGrant.from(params) ==== Right(
        AuthorizationCodeGrant(code = "theCode", redirectUri = "http://example/redirect")
      )
    }

    "is not instantiated" in new Context {
      AuthorizationCodeGrant.from(params) ==== Left(InvalidGrant(None))
    }
  }

  "ClientCredentialsGrant" >> {
    "is instantiated when params are complete" in new Context {
      override val params: AccessGrant.Params = AccessGrant.Params.from(Map("grant_type" -> "client_credentials"))

      ClientCredentialsGrant.from(params) ==== Right(ClientCredentialsGrant())
    }

    "is not instantiated" in new Context {
      ClientCredentialsGrant.from(params) ==== Left(InvalidGrant(None))
    }
  }

  "RefreshToken" >> {
    "is instantiated when params are complete" in new Context {
      override val params: AccessGrant.Params = AccessGrant.Params.from(
        Map(
          "grant_type" -> "refresh_token",
          "refresh_token" -> "token"
        )
      )

      RefreshTokenGrant.from(params) ==== Right(RefreshTokenGrant(refreshToken = "token"))
    }

    "is not instantiated" in new Context {
      RefreshTokenGrant.from(params) ==== Left(InvalidGrant(None))
    }
  }

  "ResourceOwnerPasswordCredentials" >> {
    "is instantiated when params are complete" in new Context {
      override val params: AccessGrant.Params = AccessGrant.Params.from(
        Map(
          "grant_type" -> "password",
          "username" -> "user",
          "password" -> "p4ss"
        )
      )

      val expected: ResourceOwnerPasswordCredentialsGrant =
        ResourceOwnerPasswordCredentialsGrant(username = "user", password = "p4ss")

      ResourceOwnerPasswordCredentialsGrant.from(params) ==== Right(expected)
    }

    "is not instantiated" in new Context {
      ResourceOwnerPasswordCredentialsGrant.from(params) ==== Left(InvalidGrant(None))
    }
  }
}
