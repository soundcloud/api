package com.soundcloud.publicApiStrangler.support.oauth

import com.soundcloud.publicApiStrangler.test.UnitSpecification

class ClientCredentialSpec extends UnitSpecification {
  trait Context extends Scope {
    val params: AccessGrant.Params = AccessGrant.Params.from(Map.empty)
  }

  "is instantiated when params are complete" in new Context {
    override val params: AccessGrant.Params = AccessGrant.Params.from(Map("client_secret" -> "1", "client_id" -> "2"))

    ClientCredential.from(params) ==== Right(ClientCredential(id = "2", secret = "1"))
  }

  "is not instantiated when params are incomplete" in new Context {
    ClientCredential.from(params) ==== Left(MissingClientCredentials())
  }
}
