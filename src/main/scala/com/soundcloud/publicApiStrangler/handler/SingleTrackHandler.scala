package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.TrackUrnUtil.trackUrn
import com.soundcloud.publicApiStrangler.client.trackcoordinator.datatypes.{NotFound, Result, Success}
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackRepresentationsService
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionHandler._
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, Return, Try}
import play.api.libs.json._
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackRepresentation
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest

import scala.util.control.NonFatal

class SingleTrackHandler(
    userAuthentication: UserAuthentication,
    tracksService: TrackRepresentationsService,
    telemetry: Telemetry,
    exceptionCollector: ExceptionCollector
) {
  val logger = SoundCloudLoggerFactory.getLogger(getClass)

  def renderTrack(req: HandlerRequest): Future[Response] = {
    stripConditionalRequestHeaders(req)

    userAuthentication.withUserSession(req) { session =>
      Try(trackUrn(req)) match {
        case Return(urn) =>
          val secretToken = req.params.get("secret_token")
          fetchTrackRepresentation(session, urn, secretToken)
            .map {
              case Success(trackRep) => generateResponse(Status.Ok, Json.stringify(Json.toJson(trackRep)))
              case NotFound => generateNotFound
              case _ =>
                logger.error(s"Something went wrong while trying to fetch $urn")
                generateResponse(Status.InternalServerError, "Something went wrong while fetching a track")
            }
            .handleAndReport(exceptionCollector) {
              case NonFatal(e) =>
                logger.error(e.getMessage)
                generateResponse(
                  Status.InternalServerError,
                  "An unexpected error occured while fetching a track"
                )
            }
        case _ => Future.value(generateNotFound)
      }
    }
  }

  private def fetchTrackRepresentation(
      session: UserSession,
      urn: Urn,
      secretToken: Option[String]
  ): Future[Result[TrackRepresentation]] = {
    tracksService
      .track(session, TrackRequest(urn, secretToken))
      .map {
        case Some(track) => Success(track)
        case _ => NotFound
      }
  }

  private def generateNotFound: Response = {
    JsonResponseBuilder.notFound(notFoundErrorString)
  }

  private def generateResponse(status: Status, rawContent: String): Response = {
    JsonResponseBuilder(status = status, body = rawContent).build
  }

  private val notFoundErrorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""

  /*
   * If-None-Match header causes mothership to return 304
   * We decided not to support this behavior
   */
  private def stripConditionalRequestHeaders(req: HandlerRequest): Option[String] = {
    req.headerMap.remove("If-None-Match")
  }

}
