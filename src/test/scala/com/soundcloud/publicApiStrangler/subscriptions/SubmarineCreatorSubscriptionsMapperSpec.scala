package com.soundcloud.publicApiStrangler.subscriptions

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import org.specs2.mutable.Specification
import play.api.libs.json.{JsNull, JsResultException}

class SubmarineCreatorSubscriptionsMapperSpec extends Specification {
  "#apply" >> {
    "when body is correct returns the subscriptions" >> {
      val body = Fixtures.submarineCreatorSubscription

      val creatorSub = SubmarineCreatorSubscription(
        false,
        Package(
          "Yearly Pro plan",
          "pro"
        )
      )

      val mapper = new SubmarineCreatorSubscriptionsMapper
      mapper(body) ==== Map(
        Urn("soundcloud", "users", "123") -> Some(creatorSub),
        Urn("soundcloud", "users", "2") -> None
      )
    }

    "when body is malformed throws exception" >> {
      val mapper = new SubmarineCreatorSubscriptionsMapper
      mapper(JsNull) must throwAn[JsResultException]
    }
  }
}
