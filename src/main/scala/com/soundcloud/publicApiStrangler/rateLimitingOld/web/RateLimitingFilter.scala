package com.soundcloud.publicApiStrangler.rateLimitingOld.web

import com.soundcloud.publicApiStrangler.rateLimitingOld
import com.soundcloud.publicApiStrangler.rateLimitingOld.{Consumer, Ip, RateLimit}
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.scalakit.finagle.http.HandlerRequest
import com.twitter.finagle.http.Response
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.{Future, NonFatal}
import org.jboss.netty.handler.codec.http.{HttpResponseStatus, HttpVersion}

class RateLimitingFilter(rateLimit: RateLimit) extends SimpleFilter[HandlerRequest, Response] {
  val logger = SoundCloudLoggerFactory.getLogger(this.getClass)
  val realIpHeader = "X-Real-Ip"

  val rateLimitExceededResponse = Response(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(429))

  val badRequestResponse = Response(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(400))

  override def apply(handlerRequest: HandlerRequest, service: Service[HandlerRequest, Response]): Future[Response] = {
    try {
      checkRateLimit(handlerRequest, service)
    } catch {
      case NonFatal(e) => logger.error("Error while retrieving rate limiting", e); service(handlerRequest)
    }
  }

  private def checkRateLimit(handlerRequest: HandlerRequest, service: Service[HandlerRequest, Response]): Future[Response] = {
    Option(handlerRequest.request.headers().get(realIpHeader)) match {
      case Some(ip) => checkRateLimitFor(Ip(ip), handlerRequest, service)
      case None => Future.value(badRequestResponse)
    }
  }

  private def checkRateLimitFor(resourceConsumer: Consumer, handlerRequest: HandlerRequest, service: Service[HandlerRequest, Response]) = {
    rateLimit.checkIfAllowed(resourceConsumer).flatMap {
      case true => service(handlerRequest)
      case false => logDenied(handlerRequest, resourceConsumer); Future.value(rateLimitExceededResponse)
    }.rescue {
      case NonFatal(e) => logger.error("Error while retrieving rate limiting", e); service(handlerRequest)
    }
  }

  private def logDenied(handlerRequest: HandlerRequest, consumer: Consumer) = {
    val requestMethod = handlerRequest.getMethod
    val requestUri = handlerRequest.getUri

    logger.info(s"Rate limit exceeded: [$requestMethod $requestUri] from [${consumer.identifier}]")
  }
}
