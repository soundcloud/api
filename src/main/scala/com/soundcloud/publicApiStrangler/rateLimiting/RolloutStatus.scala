package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.publicApiStrangler.features.{Features, Rollout}
import com.soundcloud.ratelimiting.core.ClientApplication

sealed trait RolloutStatus

object RolloutStatus {
  case object Probing extends RolloutStatus
  case object Enforcing extends RolloutStatus
  case object Disabled extends RolloutStatus

  def forApiClient(clientApplication: ClientApplication, rollout: Rollout): RolloutStatus = {
    if (!rollout.isActiveForId(Features.ProbeRateLimits, Some(clientApplication.urn)))
      Disabled
    else if (!rollout.isActiveForId(Features.EnforceRateLimits, Some(clientApplication.urn)))
      Probing
    else
      Enforcing
  }
}
