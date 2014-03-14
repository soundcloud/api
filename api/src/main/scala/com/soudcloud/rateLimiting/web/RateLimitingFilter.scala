package com.soudcloud.rateLimiting.web

import com.twitter.finagle.http.{Response, Request}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.{NonFatal, Future}
import com.soudcloud.rateLimiting.{Ip, Consumer, RateLimit}
import org.jboss.netty.handler.codec.http.{HttpResponseStatus, HttpVersion}
import org.slf4j.LoggerFactory

class RateLimitingFilter(rateLimit: RateLimit) extends SimpleFilter[Request, Response] {
  val realIpHeader = "X-Real-Ip"

  val logger = LoggerFactory.getLogger(this.getClass)

  val rateLimitExceededResponse = Response(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(429))

  val badRequestResponse = Response(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(400))

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    try {
      checkRateLimit(request, service)
    } catch {
      case NonFatal(e) => logger.error("Error while retrieving rate limiting", e); service(request)
    }
  }

  private def checkRateLimit(request: Request, service: Service[Request, Response]): Future[Response] = {
    Option(request.headers().get(realIpHeader)) match {
      case Some(ip) => checkRateLimitFor(Ip(ip), request, service)
      case None => Future.value(badRequestResponse)
    }
  }

  private def checkRateLimitFor(resourceConsumer: Consumer, request: Request, service: Service[Request, Response]) = {
    rateLimit.checkIfAllowed(resourceConsumer).flatMap {
      case true => service(request)
      case false => logDenied(request, resourceConsumer); Future.value(rateLimitExceededResponse)
    }.rescue {
      case NonFatal(e) => logger.error("Error while retrieving rate limiting", e); service(request)
    }
  }

  private def logDenied(request: Request, consumer: Consumer) = {
    val requestMethod = request.getMethod
    val requestUri = request.getUri

    logger.info(s"$requestMethod $requestUri -> ${rateLimitExceededResponse.getStatusCode()} (from [${consumer.identifier}])")
  }
}
