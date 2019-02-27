package com.soundcloud.publicApiStrangler.client.mothership.response.representation

import com.soundcloud.jvmkit.module.util.Urn
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.libs.json.Json

class HasUrnSpec extends Specification {

  trait Context extends Scope {
    val expectedUrn = Urn("soundcloud", "tracks", "1")
  }

  "JSON" >> {
    "returns top-level URN attribute" in new Context {
      Json.obj("urn" -> expectedUrn.toString) match {
        case HasUrn(urn) => urn ==== expectedUrn
        case other => failure("Not found")
      }
    }

    "returns 'self' URN attribute" in new Context {
      Json.obj("self" -> Json.obj("urn" -> expectedUrn.toString)) match {
        case HasUrn(urn) => urn ==== expectedUrn
        case other => failure("Not found")
      }
    }

    "returns nothing if neither" in new Context {
      Json.obj("unknownAttr" -> expectedUrn.toString) match {
        case HasUrn(urn) => failure("Found")
        case other => success("Not found")
      }
    }
  }
}
