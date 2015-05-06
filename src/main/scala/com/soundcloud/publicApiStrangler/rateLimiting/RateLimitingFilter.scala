package com.soundcloud.publicApiStrangler.rateLimiting

import java.util.concurrent.atomic.AtomicBoolean

import com.soundcloud.bff.finagle.{Request => BffRequest, ResponseBuilder}
import com.soundcloud.bff.web.UserAuthentication
import com.soundcloud.jvmkit.FailsafeUserSession
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.features.{Features, Rollout}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import com.soundcloud.scalakit.UserSession

class RateLimitingFilter(
  rateLimiter: RateLimiter,
  userAuthentication: UserAuthentication,
  rollout: Rollout,
  whitelistingService: WhitelistingService
) extends SimpleFilter[Request, Response] {

  val logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  def apply(request: Request, next: Service[Request, Response]): Future[Response] =
    userAuthentication.withUserSession(new BffRequest(request)) { session =>
      limitReached(session).flatMap {
        case Some(status) =>
          Future.value(new ResponseBuilder().typedJson(status).status(HttpResponseStatus.TOO_MANY_REQUESTS.getCode).build)
        case None =>
          next(request)
      }
    }

  private def limitReached(session: UserSession): Future[Option[RateLimitStatus.Reached]] =
    session match {
      case session: FailsafeUserSession =>
        Future.None
      case session =>
        Future.Unit.flatMap { _ =>
          limitReached(ApiClient(session.getAgent))
        }.handle {
          case ex: Exception =>
            logger.error("Something went wrong while trying to rate-limit the request.", ex)
            None
        }
    }

  private def limitReached(apiClient: ApiClient) =
    if (!rollout.isActiveForId(Features.ProbeRateLimits, Option(apiClient.urn)) ||
      whitelistingService.hasClientWhitelisted(apiClient.urn))
      Future.None
    else
      applyLimit(apiClient)

  private def applyLimit(apiClient: ApiClient) =
    rateLimiter.advanceRateLimitStatus(apiClient).map {
      case status: RateLimitStatus.Reached if (rollout.isActiveForId(Features.EnforceRateLimits, Option(apiClient.urn))) =>
        Option(status)
      case _ =>
        None
    }
}
