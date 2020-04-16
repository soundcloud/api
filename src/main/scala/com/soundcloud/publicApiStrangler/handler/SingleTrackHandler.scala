package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.TrackUrnUtil.trackUrn
import com.soundcloud.publicApiStrangler.client.trackcoordinator.datatypes.{NotFound, Result, Success}
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  LegacyTrackRepresentationsService,
  TrackRepresentationsService
}
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionHandler._
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, Return, Try}
import play.api.libs.json._
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackRepresentationLike
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest

import scala.util.control.NonFatal

class SingleTrackHandler(
    userAuthentication: UserAuthentication,
    tracksService: TrackRepresentationsService,
    legacyTracksService: LegacyTrackRepresentationsService,
    telemetry: Telemetry,
    exceptionCollector: ExceptionCollector
) {
  val logger = SoundCloudLoggerFactory.getLogger(getClass)

  private val inconsistentSingleTrackFetchResponsesCounter =
    telemetry.counter(
      "inconsistent_single_track_fetch_response_total",
      "Count of inconsistent (not matching) responses from legacy and new single track fetch"
    )

  def renderTrack(req: HandlerRequest): Future[Response] = {
    stripConditionalRequestHeaders(req)

    userAuthentication.withUserSession(req) { session =>
      Try(trackUrn(req)) match {
        case Return(urn) =>
          val secretToken = req.params.get("secret_token")
          fetchAndOrchestrateTrackRepresentation(session, urn, secretToken)
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

  private def fetchAndOrchestrateTrackRepresentation(
      session: UserSession,
      urn: Urn,
      secretToken: Option[String]
  ): Future[Result[TrackRepresentationLike]] = {
    Future
      .join(
        tracksService
          .track(session, TrackRequest(urn, secretToken))
          .map {
            case Some(track) => Success(track)
            case _ => NotFound
          }
          .rescue { case NonFatal(_) => Future.value(NotFound) },
        legacyTracksService.track(session, urn, secretToken)
      )
      .map {
        case (track, legacyTrack) =>
          compareAndReportTracks(track, legacyTrack)
          legacyTrack
      }
  }

  private def compareAndReportTracks(
      track1: Result[TrackRepresentationLike],
      track2: Result[TrackRepresentationLike]
  ): Unit = {
    (track1, track2) match {
      case (Success(t1), Success(t2)) =>
        val t1Json = Json.toJson(t1)
        val t2Json = Json.toJson(t2)

        if (t1Json != t2Json) {
          inconsistentSingleTrackFetchResponsesCounter.inc()
          SoundCloudLoggerFactory
            .getLogger(getClass)
            .warn(s"Track inconsistency: ${Json.stringify(t1Json)} != ${Json.stringify(t2Json)}")
        }
      case _ =>
    }
  }
}
