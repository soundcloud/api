package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.finagle.{Request => BffRequest, ResponseBuilder}
import com.soundcloud.bff.web.UserAuthentication
import com.soundcloud.jvmkit.FailsafeUserSession
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.features.{Features, Rollout}
import com.soundcloud.ratelimiting.core.ClientApplication
import com.soundcloud.scalakit.{Urn, UserSession}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsObject, Json}

import scala.util.control.NonFatal

class RateLimitingFilter(
  rateLimiterRegistry: RateLimiterRegistry,
  userAuthentication: UserAuthentication,
  rollout: Rollout
) extends SimpleFilter[Request, Response] {

  private val soundLogger = SoundCloudLoggerFactory.getLogger(this.getClass)
  private val logger = LoggerFactory.getLogger(this.getClass)

  def apply(request: Request, next: Service[Request, Response]): Future[Response] = {
    if (!rollout.isActive(Features.WireRateLimits)) next(request)
    else {
      logger.info(s"NORMAL_LOGGER Rate limiting a request to ${request.path}")
      soundLogger.info(s"SOUND_LOGGER Rate limiting a request to ${request.path}")
      bypassForInapplicableRoutes(request, next) getOrElse {
        userAuthentication.withUserSession(new BffRequest(request)) { session =>
          val clientApplication = ClientApplication(session.getAgent)
          rateLimiterRegistry.lookup(clientApplication) flatMap { rateLimiter =>
            Verdict.on(session, request, clientApplication, rateLimiter).flatMap {
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
    def on(session: UserSession, request: Request, clientApplication: ClientApplication, rateLimiter: RateLimiter): Future[Verdict] = {
      val clientApplication = ClientApplication(session.getAgent)
      val rolloutStatus = RolloutStatus.forApiClient(clientApplication)
      val verdict = if (shouldBailOut(session, request, clientApplication, rateLimiter)) {
        Future(Pass)
      } else rolloutStatus match {
        case Disabled =>
          Future(Pass)
        case Probing =>
          rateLimiter.advanceRateLimitStatus(clientApplication, request).map(_ => Pass)
        case Enforcing =>
          rateLimiter.advanceRateLimitStatus(clientApplication, request).map { status =>
            status.keepingReachedEnforcedRateLimitStatuses.map(Block).getOrElse(Pass)
          }
      }
      verdict handle {
        case NonFatal(ex) =>
          logger.error("Something went wrong while trying to rate-limit the request.", ex)
          Pass
      }
    }

    def shouldBailOut(session: UserSession, request: Request, clientApplication: ClientApplication, rateLimiter: RateLimiter): Boolean = {
      val rateLimiterApplies = rateLimiter.appliesTo(request)
      session.isInstanceOf[FailsafeUserSession] || !rateLimiterApplies
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
