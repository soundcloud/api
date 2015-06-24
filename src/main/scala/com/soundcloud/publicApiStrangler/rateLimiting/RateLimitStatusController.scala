package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.jvmkit.FailsafeUserSession
import com.soundcloud.publicApiStrangler.features.{Features, Rollout}
import com.soundcloud.ratelimiting.core.{ActionableAccessMechanism, ClientApplication}
import com.twitter.util.Future
import play.api.libs.json.{Json, Writes}

class RateLimitStatusController(
 rateLimiterRegistry: RateLimiterRegistry,
 userAuthentication: UserAuthentication,
 rollout: Rollout) extends BffInjectionBasedController {

  get("/rate_limit_status") { request =>
    if (!rollout.isActive(Features.WireRateLimits)) emptyResponse
    else {
      userAuthentication.withUserSession(request) {
        case _: FailsafeUserSession =>
          Future.value(render.internalServerError)
        case session =>
          ActionableAccessMechanism.fromSession(session) match {
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
              emptyResponse // TBD
          }
      }
    }
  }

  private def emptyResponse = Future.value(render.typedJson(CompositeRateLimitStatus(Set.empty)))


  implicit val statusWrites: Writes[CompositeRateLimitStatus] = Writes { status =>
    Json.obj(
      "statuses" -> status.statuses
    )
  }

}
