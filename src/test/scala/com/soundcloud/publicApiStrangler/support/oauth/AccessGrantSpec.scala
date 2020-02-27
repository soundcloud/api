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

      AuthorizationCode.unapply(params) ==== Some(
        AuthorizationCode(code = "theCode", redirectUri = "http://example/redirect")
      )
    }

    "is not extracted" in new Context {
      AuthorizationCode.unapply(params) ==== None
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

      RefreshToken.unapply(params) ==== Some(RefreshToken(refreshToken = "token"))
    }

    "is not extracted" in new Context {
      RefreshToken.unapply(params) ==== None
    }
  }

  "ResourceOwnerPasswordCredentials" >> {
    "is extracted when params are complete" in new Context {
      override val params = Map(
        "grant_type" -> "password",
        "username" -> "user",
        "password" -> "p4ss"
      )

      val expected = ResourceOwnerPasswordCredentials(username = "user", password = "p4ss")

      ResourceOwnerPasswordCredentials.unapply(params) ==== Some(expected)
    }

    "is not extracted" in new Context {
      ResourceOwnerPasswordCredentials.unapply(params) ==== None
    }
  }
}
