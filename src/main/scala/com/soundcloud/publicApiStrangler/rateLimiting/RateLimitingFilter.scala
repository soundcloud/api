package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.finagle.{Request => BffRequest, ResponseBuilder}
import com.soundcloud.bff.web.UserAuthentication
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.features.{Features, Rollout}
import com.soundcloud.publicApiStrangler.utilities.FutureExtensions._
import com.soundcloud.ratelimiting.core.ActionableAccessMechanism
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
    val maybeRateLimitedResponse = try rateLimitedResponse(request).rescue(rescueError) catch rescueError
    maybeRateLimitedResponse.flatMap(_.getOrElseF(next(request)))
  }

  def rateLimitedResponse(request: Request): Future[Option[Response]] = {
    val maybeResponse = for {
      _ <- FutureOption.Unit
      if rollout.isActive(Features.WireRateLimits)
      if !isInternalRoute(request)
      wrappedResponse <- FutureOption.sequence {
        userAuthentication.withUserSession(new BffRequest(request)) { session =>
          val response = for {
            accessMechanism <- Future(ActionableAccessMechanism.fromSession(session, rollout.isActive(Features.PerUserRateLimitBuckets))).lift
            rateLimiter <- rateLimiterRegistry.lookup(accessMechanism.clientApplication).map(Some(_)).lift
            status <- Verdict.on(request, rateLimiter, accessMechanism, rollout).lift
          } yield errorResponse(status)
          Future.value(response)
        }
      }
      response <- wrappedResponse.map(Some(_)).lift
    } yield response
    maybeResponse.run
  }

  private def rescueError: PartialFunction[Throwable, Future[Option[Response]]] = {
    case NonFatal(ex) =>
      logger.error("Something went wrong while trying to rate-limit the request", ex)
      Future.None
  }

  private def isInternalRoute(request: Request): Boolean = {
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
