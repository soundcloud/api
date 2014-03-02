package com.soudcloud.rateLimiting.web

import com.twitter.finagle.http.{Response, Request}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.{NonFatal, Future}
import com.soudcloud.rateLimiting.{Consumer, RateLimit}
import org.jboss.netty.handler.codec.http.{HttpResponseStatus, HttpVersion}
import org.slf4j.LoggerFactory

class RateLimitingFilter(rateLimit: RateLimit) extends SimpleFilter[Request, Response] {
  val logger = LoggerFactory.getLogger(this.getClass)

  val rateLimitExceededResponse = Response(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(429))

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {

    try {
      checkRateLimit(request, service)
    } catch {
      case NonFatal(e) => logger.error("Error while retrieving rate limiting", e); service(request)
    }
  }

  private def checkRateLimit(request: Request, service: Service[Request, Response]): Future[Response] = {
    val remoteAddress: Consumer = request.remoteAddress

    rateLimit.checkIfAllowed(remoteAddress).flatMap {
      case true => service(request)
      case false => Future.value(rateLimitExceededResponse)
    }.rescue {
      case NonFatal(e) => logger.error("Error while retrieving rate limiting", e); service(request)
    }
  }
}
