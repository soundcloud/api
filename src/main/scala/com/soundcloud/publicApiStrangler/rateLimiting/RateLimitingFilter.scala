package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.finagle.{Request => BffRequest, ResponseBuilder}
import com.soundcloud.bff.web.UserAuthentication
import com.soundcloud.jvmkit.FailsafeUserSession
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.features.{Features, Rollout}
import com.soundcloud.scalakit.UserSession
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.{Future}
import org.jboss.netty.handler.codec.http.HttpResponseStatus

import scala.util.control.NonFatal

class RateLimitingFilter(
  rateLimiter: RateLimiter,
  userAuthentication: UserAuthentication,
  rollout: Rollout,
  whitelistingService: WhitelistingService
) extends SimpleFilter[Request, Response] {

  val logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  def apply(request: Request, next: Service[Request, Response]): Future[Response] = {
    if (!isWired)
      next(request)
    else {
      userAuthentication.withUserSession(new BffRequest(request)) { session =>
        Verdict.on(session).flatMap {
          case Pass =>
            next(request)
          case Block(status) =>
            Future.value(new ResponseBuilder().typedJson(status).status(HttpResponseStatus.TOO_MANY_REQUESTS.getCode).build)
        }
      }
    }
  }

  private def isWired: Boolean = {
    rollout.isActive(Features.WireRateLimits)
  }

  sealed trait Verdict
  case object Pass extends Verdict
  case class Block(status: RateLimitStatus.Reached) extends Verdict

  object Verdict {
    def on(session: UserSession): Future[Verdict] = {
      val apiClient = ApiClient(session.getAgent)
      val verdict = (session, apiClient, RolloutStatus.forApiClient(apiClient)) match {
        case (_: FailsafeUserSession, _, _) | (_, Whitelisted(), _) | (_, _, Disabled) =>
          Future(Pass)
        case (_, _, Probing) =>
          rateLimiter.advanceRateLimitStatus(apiClient).map(_ => Pass)
        case (_, _, Enforcing) =>
          rateLimiter.advanceRateLimitStatus(apiClient).map {
            case s @ RateLimitStatus.Reached(_, _) => Block(s)
            case _                                 => Pass
          }
      }
      verdict handle {
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

  object Whitelisted {
    def unapply(apiClient: ApiClient): Boolean = {
      whitelistingService.hasClientWhitelisted(apiClient.urn)
    }
  }
}
