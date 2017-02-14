package com.soundcloud.publicApiStrangler.client.sketchy

import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.FinagleLoggerFactory
import com.soundcloud.scalakit.finagle.http.{NotFoundStatus, OkStatus}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.twitter.util.{Future, NonFatal}

class SketchyClient(client: JsonClient) {

  def ack(session: UserSession, warningId: Long): Future[SketchyResponse] =
    client
      .put(session, Path() / "spam_warnings" / warningId / "ack", Params.empty, Params.empty, None)
      .map {
        case JsonResponse(OkStatus, _, _, _) => AckOk

        case JsonResponse(NotFoundStatus, _, _, _) => WarningNotFound

        case response: JsonResponse =>
          logger.warn(s"Unknown response from Sketchy for an ack request: $response")
          UnknownError(response.status.i, response.body.toString)
      }
      .handle {
        case NonFatal(e) =>
          logger.warn(s"Unable to ack warning $warningId", e)
          UnknownError(500, e.getMessage)
      }

  private val logger = FinagleLoggerFactory.getLogger(getClass)
}
