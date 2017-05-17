package com.soundcloud.publicApiStrangler.client.sketchy

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future

import scala.util.control.NonFatal

class SketchyClient(client: JsonClient) {

  def ack(session: UserSession, warningId: Long): Future[SketchyResponse] =
    client
      .putWithSession(session, Path() / "spam_warnings" / warningId / "ack", Params.empty, Headers.empty, None)
      .map { response: Response =>
        response.status match {
          case Status.Ok => AckOk
          case Status.NotFound => WarningNotFound
          case _ =>
            logger.warn(s"Unknown response from Sketchy for an ack request: $response")
            UnknownError(response.statusCode, response.contentString)
        }
      }
      .handle {
        case NonFatal(e) =>
          logger.warn(s"Unable to ack warning $warningId", e)
          UnknownError(500, e.getMessage)
      }

  private val logger = SoundCloudLoggerFactory.getLogger(getClass)
}
