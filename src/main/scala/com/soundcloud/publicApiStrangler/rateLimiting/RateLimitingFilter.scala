package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.finagle.{Request => BffRequest, ResponseBuilder}
import com.soundcloud.bff.web.UserAuthentication
import com.soundcloud.jvmkit.FailsafeUserSession
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import com.soundcloud.scalakit.utilities.OptionExtensions._
import org.jboss.netty.handler.codec.http.HttpResponseStatus

class RateLimitingFilter(rateLimiterProvider: RateLimiterProvider, userAuthentication: UserAuthentication) extends SimpleFilter[Request, Response] {
  val logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  private def render = new ResponseBuilder

  def apply(request: Request, next: Service[Request, Response]): Future[Response] = {
    val rateLimiter = rateLimiterProvider.rateLimiters.head // We only have one atm
    userAuthentication.withUserSession(new BffRequest(request)) {
      case session: FailsafeUserSession =>
        next(request) // no rate limiting if there is no authenticated client; next filter should take care of authorization
      case session =>
        val apiClient = ApiClient(session.getAgent)
        for {
          status <- rateLimiter.advanceRateLimitStatus(apiClient)
          response <- status match {
            case status @ RateLimitStatus.Reached(_, _) =>
              Future.value(render.typedJson(status).status(HttpResponseStatus.TOO_MANY_REQUESTS.getCode).build)
            case RateLimitStatus.Advancing(_, _) =>
              next(request)
          }
        } yield response
    }
  }
}
