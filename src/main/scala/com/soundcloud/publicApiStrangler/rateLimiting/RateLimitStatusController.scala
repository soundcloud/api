package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.features.{Features, Rollout}
import com.soundcloud.ratelimiting.core.{CompositeRateLimitStatus, ActionableAccessMechanism, RateLimiterRegistry}
import com.twitter.util.Future
import play.api.libs.json.{Json, Writes}

class RateLimitStatusController(
 rateLimiterRegistry: RateLimiterRegistry,
 userAuthentication: UserAuthentication,
 rollout: Rollout) extends BffInjectionBasedController {

  get("/rate_limit_status") { request =>
    if (!rollout.isActive(Features.WireRateLimits)) emptyResponse
    else {
      userAuthentication.withUserSession(request) { session =>
        ActionableAccessMechanism.fromSession(session, rollout.isActive(Features.PerUserRateLimitBuckets)) match {
          case Some(accessMechanism) =>
            if (!rollout.isActiveForId(Features.EnforceRateLimits, Some(session.getAgent))) {
              emptyResponse
            } else {
              for {
                rateLimiter <- rateLimiterRegistry.lookup(accessMechanism.clientApplication)
                status <- rateLimiter.currentStatus(accessMechanism)
              } yield render.typedJson(status)
            }
          case _ =>
            Future.value(render.internalServerError)
        }
      }
    }
  }

  private def emptyResponse = Future.value(render.typedJson(CompositeRateLimitStatus(Set.empty)))


  implicit val statusWrites: Writes[CompositeRateLimitStatus] = Writes { status =>
    Json.obj("statuses" -> status.statuses)
  }
}
