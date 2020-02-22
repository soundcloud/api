package com.soundcloud.publicApiStrangler.support.oauth

import com.soundcloud.publicApiStrangler.test.UnitSpecification

class ClientCredentialSpec extends UnitSpecification {

  trait Context extends Scope {
    val params: Map[String, String] = Map.empty
  }

  "is extracted when params are complete" in new Context {
    override val params = Map("client_secret" -> "1", "client_id" -> "2")

    ClientCredential.unapply(params) ==== Some(ClientCredential(id = "2", secret = "1"))
  }

  "is not extracted when params are incomplete" in new Context {
    ClientCredential.unapply(params) ==== None
  }
}
