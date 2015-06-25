package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.finagle.{Request => BffRequest, ResponseBuilder}
import com.soundcloud.bff.web.UserAuthentication
import com.soundcloud.publicApiStrangler.features.{Features, Rollout}
import com.soundcloud.publicApiStrangler.utilities.FutureExtensions._
import com.soundcloud.ratelimiting.core.ActionableAccessMechanism
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import play.api.libs.json.{JsObject, Json}

class RateLimitingFilter(
  rateLimiterRegistry: RateLimiterRegistry,
  userAuthentication: UserAuthentication,
  rollout: Rollout
) extends SimpleFilter[Request, Response] {

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
            status <- Verdict.on(request, rateLimiter, accessMechanism, rollout).lift
          } yield errorResponse(status)
          Future.value(response)
        }
      }
      response <- wrappedResponse.map(Some(_)).lift
    } yield response

    rateLimitedResponse.run.flatMap(_.getOrElseF(next(request)))
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
