package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.finagle.{Request => BffRequest, ResponseBuilder}
import com.soundcloud.bff.web.UserAuthentication
import com.soundcloud.jvmkit.FailsafeUserSession
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.features.{Features, Rollout}
import com.soundcloud.ratelimiting.whitelisting.ApplicationLevelWhitelistProxy
import com.soundcloud.scalakit.UserSession
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import play.api.libs.json.{JsObject, Json}

import scala.util.control.NonFatal

class RateLimitingFilter(
  rateLimiterRegistry: RateLimiterRegistry,
  userAuthentication: UserAuthentication,
  rollout: Rollout,
  whitelistProxy: ApplicationLevelWhitelistProxy
) extends SimpleFilter[Request, Response] {

  val logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  def apply(request: Request, next: Service[Request, Response]): Future[Response] = {
    if (!rollout.isActive(Features.WireRateLimits)) next(request)
    else {
      bypassForInapplicableRoutes(request, next) getOrElse {
        userAuthentication.withUserSession(new BffRequest(request)) { session =>
          val apiClient = ApiClient(session.getAgent)
          rateLimiterRegistry.lookup(apiClient) flatMap { rateLimiter =>
            Verdict.on(session, request, apiClient, rateLimiter).flatMap {
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
    def on(session: UserSession, request: Request, apiClient: ApiClient, rateLimiter: RateLimiter): Future[Verdict] = {
      val apiClient = ApiClient(session.getAgent)
      val rolloutStatus = RolloutStatus.forApiClient(apiClient)
      val verdict = if (shouldBailOut(session, request, apiClient, rateLimiter)) {
        Future(Pass)
      } else rolloutStatus match {
        case Disabled =>
          Future(Pass)
        case Probing =>
          rateLimiter.advanceRateLimitStatus(apiClient, request).map(_ => Pass)
        case Enforcing =>
          rateLimiter.advanceRateLimitStatus(apiClient, request).map { status =>
            if (status.hasReachedLimit) Block(status) else Pass
          }
      }
      verdict handle {
        case NonFatal(ex) =>
          logger.error("Something went wrong while trying to rate-limit the request.", ex)
          Pass
      }
    }

    def shouldBailOut(session: UserSession, request: Request, apiClient: ApiClient, rateLimiter: RateLimiter): Boolean = {
      session.isInstanceOf[FailsafeUserSession] ||
        whitelistProxy.hasClientWhitelisted(apiClient.urn) ||
        !rateLimiter.appliesTo(request)
    }
  }

  sealed trait RolloutStatus
  case object Probing extends RolloutStatus
  case object Enforcing extends RolloutStatus
  case object Disabled extends RolloutStatus

  object RolloutStatus {
    def forApiClient(apiClient: ApiClient): RolloutStatus = try {
      if (!rollout.isActiveForId(Features.ProbeRateLimits, Some(apiClient.urn)))
        Disabled
      else if (!rollout.isActiveForId(Features.EnforceRateLimits, Some(apiClient.urn)))
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
