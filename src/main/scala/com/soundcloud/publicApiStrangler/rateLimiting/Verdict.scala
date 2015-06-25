package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.features.Rollout
import com.soundcloud.ratelimiting.core.ActionableAccessMechanism
import com.twitter.finagle.http.Request
import com.twitter.util.Future

import scala.util.control.NonFatal

object Verdict {
  type Type = Option[CompositeRateLimitStatus]
  val Pass = None
  val Block = Some(_: CompositeRateLimitStatus)

  private val logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  def on(request: Request, rateLimiter: RateLimiter, accessMechanism: ActionableAccessMechanism, rollout: Rollout): Future[Verdict.Type] = {
    locally {
      RolloutStatus.forApiClient(accessMechanism.clientApplication, rollout) match {
        case _ if !rateLimiter.appliesTo(request) => Future(Pass)
        case RolloutStatus.Disabled               => Future(Pass)
        case RolloutStatus.Probing                =>
          rateLimiter.advanceRateLimitStatus(accessMechanism, request).map(_ => Pass)
        case RolloutStatus.Enforcing              =>
          rateLimiter.advanceRateLimitStatus(accessMechanism, request).map { status =>
            status.keepingReachedEnforcedRateLimitStatuses.map(Block).getOrElse(Pass)
          }
      }
    } handle {
      case NonFatal(ex) =>
        logger.error("Something went wrong while trying to rate-limit the request.", ex)
        Pass
    }
  }
}

