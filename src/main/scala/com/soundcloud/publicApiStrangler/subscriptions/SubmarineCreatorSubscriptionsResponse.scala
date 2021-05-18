package com.soundcloud.publicApiStrangler.subscriptions

import com.soundcloud.jvmkit.module.util.Urn

sealed trait SubmarineCreatorSubscriptionsResponse {
  def subscriptions: Map[Urn, Option[SubmarineCreatorSubscription]]
}
case class ActiveSubmarineCreatorSubscriptionsResponse(subscriptions: Map[Urn, Option[SubmarineCreatorSubscription]])
    extends SubmarineCreatorSubscriptionsResponse
case object NoSubmarineCreatorSubscriptionsResponse extends SubmarineCreatorSubscriptionsResponse {
  override def subscriptions: Map[Urn, Nothing] = Map.empty
}
