package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.finagle.{Request => BffRequest, ResponseBuilder}
import com.soundcloud.bff.web.UserAuthentication
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.features.{Features, Rollout}
import com.soundcloud.publicApiStrangler.utilities.FutureExtensions._
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
    val rateLimitedResponse = for {
      _ <- FutureOption.Unit
      if rollout.isActive(Features.WireRateLimits)
      if !isInternalRoute(request)
      wrappedResponse <- FutureOption.sequence {
        userAuthentication.withUserSession(new BffRequest(request)) { session =>
          val response = for {
            accessMechanism <- Future(ActionableAccessMechanism.fromSession(session)).lift
            rateLimiter <- rateLimiterRegistry.lookup(accessMechanism.clientApplication).map(Some(_)).lift
            status <- Verdict.on(request, rateLimiter, accessMechanism).lift
          } yield errorResponse(status)
          Future.value(response)
        }
      }
      response <- wrappedResponse.map(Some(_)).lift
    } yield response

    rateLimitedResponse.run.flatMap(_.getOrElseF(next(request)))
  }

  type Verdict = Option[CompositeRateLimitStatus]
  val Pass = None
  val Block = Some(_: CompositeRateLimitStatus)

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

  def isInternalRoute(request: Request): Boolean = {
    request.path.startsWith("/-/")
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
