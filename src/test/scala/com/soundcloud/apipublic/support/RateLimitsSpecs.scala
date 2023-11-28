package com.soundcloud.apipublic.support

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.apipublic.test.UnitSpecification
import com.twitter.finagle.http.RequestBuilder

class RateLimitsSpecs extends UnitSpecification {
  trait Context extends Scope {
    def clientCredentialsExchangeClassifierMatches(request: HandlerRequest): Boolean =
      RateLimits.clientCredentialsExchangeRateLimiter.classifier
        .applyOrElse(request, (_: HandlerRequest) => false)

    def postRequest(uri: String, formParams: Map[String, String] = Map.empty): HandlerRequest =
      HandlerRequest(
        RequestBuilder()
          .url(uri)
          .addFormElement(formParams.toSeq: _*)
          .buildFormPost()
      )
  }

  "client-credential-exchange" >> {
    "does not match requests to create pairing codes" in new Context {
      clientCredentialsExchangeClassifierMatches(postRequest("http://api/pairing/codes")) ==== false
    }

    "does not match requests for exchange without params" in new Context {
      clientCredentialsExchangeClassifierMatches(postRequest("http://api/oauth2/token")) ==== false
    }

    "does not match requests for auth code exchange" in new Context {
      clientCredentialsExchangeClassifierMatches(
        postRequest("http://api/oauth2/token", Map("grant_type" -> "authorization_code"))
      ) ==== false
    }

    "does not match requests for wrong path" in new Context {
      clientCredentialsExchangeClassifierMatches(
        postRequest("http://api/oauth2/token/something", Map("grant_type" -> "authorization_code"))
      ) ==== false
    }

    "matches requests for client credential exchange" in new Context {
      clientCredentialsExchangeClassifierMatches(
        postRequest("http://api/oauth2/token", Map("grant_type" -> "client_credentials"))
      ) ==== true
    }

    "matches requests for client credential exchange with url param" in new Context {
      clientCredentialsExchangeClassifierMatches(
        postRequest("http://api/oauth2/token?param=laksjdfalsk", Map("grant_type" -> "client_credentials"))
      ) ==== true
    }

    "does not match requests for non client credential exchange with url param" in new Context {
      clientCredentialsExchangeClassifierMatches(
        postRequest("http://api/reposts/tracks/1327306987")
      ) ==== false
    }
  }
}
