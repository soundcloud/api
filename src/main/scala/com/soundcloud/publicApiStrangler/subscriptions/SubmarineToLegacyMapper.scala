package com.soundcloud.publicApiStrangler.subscriptions

class CreatorSubscriptionInfo(val id: String, val name: String, val planName: String)
case object Pro extends CreatorSubscriptionInfo("creator-pro", "Pro", "Pro")
case object ProUnlimited extends CreatorSubscriptionInfo("creator-pro-unlimited", "Pro Unlimited", "Pro Plus")

object SubmarineToLegacyMapper {
  private def values = List(Pro, ProUnlimited)
  def from(subscription: SubmarineCreatorSubscription): CreatorSubscriptionInfo = {
    val creatorPlanId = s"creator-${subscription.`package`.plan}"
    val fallbackPlan = subscription.`package`.plan
    val fallbackId = fallbackPlan
    val fallbackName = subscription.`package`.name
    values.find(_.id == creatorPlanId).getOrElse(new CreatorSubscriptionInfo(fallbackId, fallbackName, fallbackPlan))
  }
}
