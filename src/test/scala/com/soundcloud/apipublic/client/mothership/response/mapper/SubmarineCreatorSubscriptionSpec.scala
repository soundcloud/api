package com.soundcloud.apipublic.client.mothership.response.mapper

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.client.mothership.response.representation.{CreatorSubscription, Product}
import com.soundcloud.apipublic.subscriptions.{Package, SubmarineCreatorSubscription}
import com.soundcloud.apipublic.test.UnitSpecification

class SubmarineCreatorSubscriptionSpec extends UnitSpecification {
  trait Context extends Scope {

    val userUrn = Urn("soundcloud", "users", "10419549")
    val `package` = mock[Package]
    val submarineSubscription = mock[SubmarineCreatorSubscription]
    submarineSubscription.`package` returns `package`
    val subscriptions = Map(userUrn -> Some(submarineSubscription))
  }

  "maps subscriptions for pro users" in new Context {
    `package`.plan returns "pro"
    val expectedSubscription = CreatorSubscription(Product("creator-pro", "Pro"))
    CreatorSubscription.from(submarineSubscription) ==== expectedSubscription
  }

  "maps subscriptions for pro unlimited users" in new Context {
    `package`.plan returns "pro-unlimited"
    val expectedSubscription = CreatorSubscription(Product("creator-pro-unlimited", "Pro Unlimited"))
    CreatorSubscription.from(submarineSubscription) ==== expectedSubscription
  }

  "maps subscriptions with unhandled (new submarine plans) to use submarine values" in new Context {
    `package`.plan returns "new-and-shiny-plan-name"
    `package`.name returns "New And Shiny Plan Name"
    val expectedSubscription = CreatorSubscription(Product("new-and-shiny-plan-name", "New And Shiny Plan Name"))
    CreatorSubscription.from(submarineSubscription) ==== expectedSubscription
  }

  "maps subscriptions with a recurring field for users that match the session user" in new Context {
    `package`.plan returns "pro-unlimited"
    submarineSubscription.recurring returns true
    val expectedSubscription = CreatorSubscription(Product("creator-pro-unlimited", "Pro Unlimited"), Some(true))
    CreatorSubscription.from(submarineSubscription, true) ==== expectedSubscription
  }
}
