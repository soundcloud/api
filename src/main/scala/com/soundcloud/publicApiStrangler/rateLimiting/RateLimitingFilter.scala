package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.finagle.{Request => BffRequest, ResponseBuilder}
import com.soundcloud.bff.web.UserAuthentication
import com.soundcloud.jvmkit.FailsafeUserSession
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.features.{Features, Rollout}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpResponseStatus

class RateLimitingFilter(
  rateLimiterProvider: RateLimiterProvider,
  userAuthentication: UserAuthentication,
  rollout: Rollout
) extends SimpleFilter[Request, Response] {

  val logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  val rateLimiter = rateLimiterProvider.rateLimiters.head // We only have one atm

  def apply(request: Request, next: Service[Request, Response]): Future[Response] = {
    if (!rollout.isActive(Features.ProbeRateLimits)) {
      next(request)
    } else {
      val enforce = rollout.isActive(Features.EnforceRateLimits)
      userAuthentication.withUserSession(new BffRequest(request)) {
        case session: FailsafeUserSession => next(request) // no rate limiting if there is no authenticated client; next filter should take care of authorization
        case session =>
          val apiClient = ApiClient(session.getAgent)
          for {
            status <- rateLimiter.advanceRateLimitStatus(apiClient)
            response <- (enforce, status) match {
              case (true, status @ RateLimitStatus.Reached(_, _)) =>
                Future.value(new ResponseBuilder().typedJson(status).status(HttpResponseStatus.TOO_MANY_REQUESTS.getCode).build)
              case (_, _) =>
                next(request)
            }
          } yield response
      }
    }
  }
}
