package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.policies._
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.libs.json.Json

class AuthorizationSpec extends Specification {

  trait Context extends Scope {
    val urn = new Urn("soundcloud:tracks:12412")
    val content = Json.obj("urn" -> urn.toString, "something" -> "else")
    val authorization = new ContentAuthorization(urn, ContentPolicy.MONETIZE, Reason.NOT_SUPPORTED,
      ContentRestriction.ENCRYPTED_STREAM_ONLY, MonetizationModel.AD_SUPPORTED)
  }

  "pattern matching" >> {
    "works for available tracks" in new Context {
      Available(content, authorization) match {
        case Available(json, policy, restrictions) => {
          json ==== content
          policy ==== authorization.getPolicy
          restrictions ==== authorization.getContentRestrictions
        }
        case Unavailable(_, _) => failure("Unavailable")
      }
    }
  }

  "works for unavailable tracks" in new Context {
    Unavailable(content, authorization) match {
      case Available(_, _, _) => failure("Unavailable")
      case Unavailable(contentUrn, reason) => {
        contentUrn ==== urn
        reason ==== authorization.getReason
      }
    }
  }
}
