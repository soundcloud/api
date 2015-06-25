package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.features.{Rollout, Features}
import com.soundcloud.ratelimiting.core.ClientApplication

import scala.util.control.NonFatal

sealed trait RolloutStatus

object RolloutStatus {
  case object Probing extends RolloutStatus
  case object Enforcing extends RolloutStatus
  case object Disabled extends RolloutStatus

  private val logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  def forApiClient(clientApplication: ClientApplication, rollout: Rollout): RolloutStatus = try {
    if (!rollout.isActiveForId(Features.ProbeRateLimits, Some(clientApplication.urn)))
      Disabled
    else if (!rollout.isActiveForId(Features.EnforceRateLimits, Some(clientApplication.urn)))
      Probing
    else
      Enforcing
  } catch {
    case NonFatal(ex) =>
      logger.error("Something went wrong while trying to read the feature flags.", ex)
      Disabled
  }
}
