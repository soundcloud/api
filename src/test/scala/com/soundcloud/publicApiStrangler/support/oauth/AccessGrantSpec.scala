package com.soundcloud.publicApiStrangler.support.oauth

import com.soundcloud.publicApiStrangler.test.UnitSpecification

class AccessGrantSpec extends UnitSpecification {

  trait Context extends Scope {
    val params: Map[String, String] = Map.empty
  }

  "AuthorizationCode" >> {
    "is extracted when params are complete" in new Context {
      override val params = Map(
        "grant_type" -> "authorization_code",
        "code" -> "theCode",
        "redirect_uri" -> "http://example/redirect"
      )

      AuthorizationCodeGrant.unapply(params) ==== Some(
        AuthorizationCodeGrant(code = "theCode", redirectUri = "http://example/redirect")
      )
    }

    "is not extracted" in new Context {
      AuthorizationCodeGrant.unapply(params) ==== None
    }
  }

  "ClientCredentialsGrant" >> {
    "is extracted when params are complete" in new Context {
      override val params = Map("grant_type" -> "client_credentials")

      ClientCredentialsGrant.unapply(params) ==== Some(ClientCredentialsGrant())
    }

    "is not extracted" in new Context {
      ClientCredentialsGrant.unapply(params) ==== None
    }
  }

  "RefreshToken" >> {
    "is extracted when params are complete" in new Context {
      override val params = Map(
        "grant_type" -> "refresh_token",
        "refresh_token" -> "token"
      )

      RefreshTokenGrant.unapply(params) ==== Some(RefreshTokenGrant(refreshToken = "token"))
    }

    "is not extracted" in new Context {
      RefreshTokenGrant.unapply(params) ==== None
    }
  }

  "ResourceOwnerPasswordCredentials" >> {
    "is extracted when params are complete" in new Context {
      override val params = Map(
        "grant_type" -> "password",
        "username" -> "user",
        "password" -> "p4ss"
      )

      val expected = ResourceOwnerPasswordCredentialsGrant(username = "user", password = "p4ss")

      ResourceOwnerPasswordCredentialsGrant.unapply(params) ==== Some(expected)
    }

    "is not extracted" in new Context {
      ResourceOwnerPasswordCredentialsGrant.unapply(params) ==== None
    }
  }
}
