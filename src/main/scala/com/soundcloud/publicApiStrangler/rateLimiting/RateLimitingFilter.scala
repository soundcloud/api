package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.finagle.{Request => BffRequest, ResponseBuilder}
import com.soundcloud.bff.web.UserAuthentication
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.features.{Features, Rollout}
import com.soundcloud.ratelimiting.core.{ActionableAccessMechanism, ClientApplication}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import play.api.libs.json.{JsObject, Json}

import scala.util.control.NonFatal

class RateLimitingFilter(
  rateLimiterRegistry: RateLimiterRegistry,
  userAuthentication: UserAuthentication,
  rollout: Rollout
) extends SimpleFilter[Request, Response] {

  private val logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  def apply(request: Request, next: Service[Request, Response]): Future[Response] = {
    if (!rollout.isActive(Features.WireRateLimits)) next(request)
    else {
      bypassForInapplicableRoutes(request, next) getOrElse {
        userAuthentication.withUserSession(new BffRequest(request)) { session =>
          ActionableAccessMechanism.fromSession(session) match {
            case None => next(request)
            case Some(accessMechanism) =>
              rateLimiterRegistry.lookup(accessMechanism.clientApplication) flatMap { rateLimiter =>
                Verdict.on(request, rateLimiter, accessMechanism).flatMap {
                  case Pass =>
                    next(request)
                  case Block(status) =>
                    Future.value(errorResponse(status))
                }
              }
          }
        }
      }
    }
  }

  def bypassForInapplicableRoutes(request: Request, next: Service[Request, Response]): Option[Future[Response]] = {
    request match {
      case InternalRoute() => Some(next(request))
      case _               => None
    }
  }

  sealed trait Verdict
  case object Pass extends Verdict
  case class Block(status: CompositeRateLimitStatus) extends Verdict

  object Verdict {
    def on(request: Request, rateLimiter: RateLimiter, accessMechanism: ActionableAccessMechanism): Future[Verdict] = {
      locally {
        RolloutStatus.forApiClient(accessMechanism.clientApplication) match {
          case _ if !rateLimiter.appliesTo(request) => Future(Pass)
          case Disabled                             => Future(Pass)
          case Probing                              =>
            rateLimiter.advanceRateLimitStatus(accessMechanism, request).map(_ => Pass)
          case Enforcing                            =>
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

  sealed trait RolloutStatus
  case object Probing extends RolloutStatus
  case object Enforcing extends RolloutStatus
  case object Disabled extends RolloutStatus

  object RolloutStatus {
    def forApiClient(clientApplication: ClientApplication): RolloutStatus = try {
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

  object InternalRoute {
    def unapply(request: Request): Boolean = {
      request.path.startsWith("/-/")
    }
  }

  private def errorResponse(status: CompositeRateLimitStatus): Response = {
    new ResponseBuilder()
      .typedJson(errorResponseBody(status))
      .status(HttpResponseStatus.TOO_MANY_REQUESTS.getCode)
      .build
  }

  private def errorResponseBody(status: CompositeRateLimitStatus): JsObject = {
    val errors = status.statuses.map(statusToError)
    Json.obj("errors" -> errors)
  }

  private def statusToError(status: RateLimitStatus) = Json.obj("meta" -> Json.toJson(status))
}
