package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.support.ForwardedRequest
import com.twitter.finagle.Service
import com.twitter.finagle.http.Response
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

import scala.collection.JavaConversions._

/**
 * Lets public-api handle requests for track streams.
 */
class ForwardRequestHandler(publicApiClient: Service[HttpRequest, HttpResponse]) {

  val logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  def handle(request: Request): Future[ResponseBuilder] = {
    val futureResponse = forward(request)
    futureResponse.flatMap(response => responseToResponseBuilder(response))
  }

  private def forward(request: Request): Future[Response] = {
    request.request.host = "api.soundcloud.com"
    publicApiClient(ForwardedRequest(request.request)).map(Response.apply).handle {
      case exception: Exception =>
        logger.error("Exception when forwarding request to public-api.", exception)
        new ResponseBuilder().internalServerError.build
    }
  }

  private def responseToResponseBuilder(response: Response): Future[ResponseBuilder] = {
    val headerMap = response.headers().entries().iterator().map(entry => (entry.getKey, entry.getValue)).toMap
    Future.value(new ResponseBuilder().status(response.getStatusCode()).body(response.getContentString()).headers(headerMap))

  }
}
