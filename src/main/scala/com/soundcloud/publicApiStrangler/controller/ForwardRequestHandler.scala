package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request => SoundCloudRequest, ResponseBuilder}
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.support.ForwardedRequest
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future

import scala.collection.JavaConversions._

/**
 * Lets public-api handle requests for track streams.
 */
class ForwardRequestHandler(publicApiClient: Service[Request, Response]) {

  val logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  def handle(request: SoundCloudRequest): Future[ResponseBuilder] = {
    val futureResponse = forward(request)
    futureResponse.flatMap(response => responseToResponseBuilder(response))
  }

  private def forward(request: SoundCloudRequest): Future[Response] = {
    request.request.host = "api.soundcloud.com"
    publicApiClient(ForwardedRequest(request.request)).handle {
      case exception: Exception =>
        logger.error("Exception when forwarding request to public-api.", exception)
        new ResponseBuilder().internalServerError.build
    }
  }

  private def responseToResponseBuilder(response: Response): Future[ResponseBuilder] = {
    val headerMap = response.headerMap.entrySet().map(entry => entry.getKey -> entry.getValue).toMap
    Future.value(new ResponseBuilder().status(response.getStatusCode()).body(response.getContentString()).headers(headerMap))
  }
}
