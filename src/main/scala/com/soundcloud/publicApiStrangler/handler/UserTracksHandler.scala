package com.soundcloud.publicApiStrangler.handler

import java.net.URL

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  LegacyTrackRepresentationsService,
  TrackPagination,
  TrackRepresentationsService,
  TracksRepresentationResult
}
import com.soundcloud.publicApiStrangler.support.{Bad, Good, Result, StringError}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.Json

import scala.util.control.NonFatal
import scala.util.{Success, Try}

class UserTracksHandler(
    userAuthentication: UserAuthentication,
    trackService: TrackRepresentationsService,
    legacyTracksService: LegacyTrackRepresentationsService,
    telemetry: Telemetry,
    baseUrl: String
) {
  val logger = SoundCloudLoggerFactory.getLogger(getClass)

  private val numericRegexp = """\d+""".r
  private val inconsistentUserTracksFetchResponsesCounter =
    telemetry.counter(
      "inconsistent_user_tracks_fetch_response_total",
      "Count of inconsistent (not matching) responses from legacy and new user tracks fetch"
    )

  def handleRequest(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      val userId = req.routeParams("userId")

      val pagination = TrackPagination.fromRequest(req.params, new URL(baseUrl + req.uri))

      def getResult(urn: Urn): Future[Result[TracksRepresentationResult]] = {
        Future
          .join(
            trackService
              .userTracks(session, urn, pagination)
              .map(Good(_))
              .handle {
                case NonFatal(e) =>
                  logger.error(e.getMessage)
                  Bad(HttpError(Status.InternalServerError))
              },
            legacyTracksService
              .userTracks(session, urn, pagination)
              .handle {
                case NonFatal(e) =>
                  logger.error(e.getMessage)
                  Bad(HttpError(Status.InternalServerError))
              }
          )
          .map {
            case (tracks, legacyTracks) =>
              compareAndReportTracks(tracks, legacyTracks)
              legacyTracks
          }
      }

      Try(Urn("soundcloud", "users", userId)) match {
        case Success(urn @ Urn(_, _, numericRegexp())) =>
          getResult(urn).map {
            case Good(tracksRepresentationResult) =>
              generateResponse(Status.Ok, getRepresentation(tracksRepresentationResult, pagination))
            case Bad(error: HttpError) =>
              generateResponse(error.status, generateErrorBody(error.description))
            case Bad(error: StringError) =>
              generateResponse(Status.InternalServerError, generateErrorBody(error.message))
            case Bad(_) =>
              generateResponse(
                Status.InternalServerError,
                generateErrorBody("an unexpected error occurred")
              )
          }
        case _ => Future.value(generateNotFound)
      }
    }
  }

  private def compareAndReportTracks(
      tracks1: Result[TracksRepresentationResult],
      tracks2: Result[TracksRepresentationResult]
  ): Unit = {
    (tracks1, tracks2) match {
      case (Good(t1), Good(t2)) =>
        val t1Json = Json.toJson(t1.tracks)
        val t2Json = Json.toJson(t2.tracks)

        if (t1Json != t2Json) {
          inconsistentUserTracksFetchResponsesCounter.inc()
          SoundCloudLoggerFactory
            .getLogger(getClass)
            .warn(s"Track inconsistency: ${Json.stringify(t1Json)} != ${Json.stringify(t2Json)}")
        }
      case _ =>
    }
  }

  private def getRepresentation(result: TracksRepresentationResult, pagination: TrackPagination) = {
    if (pagination.linkedPartitioning) {
      val tracksJson = Json.obj("collection" -> Json.toJson(result.tracks))
      val json = result.nextHref
        .map(nextHref => {
          tracksJson ++ Json.obj("next_href" -> nextHref)
        })
        .getOrElse(tracksJson)

      Json.stringify(json)
    } else {
      Json.stringify(Json.toJson(result.tracks))
    }
  }

  private def generateNotFound: Response = {
    val content = notFoundErrorString
    val contentLength = content.getBytes("UTF-8").length
    val res = Response(Status.NotFound)
    res.setContentString(content)
    res.contentType = "application/json; charset=utf-8"
    res.contentLength = contentLength
    res
  }

  private def generateResponse(status: Status, rawContent: String): Response = {
    val content = rawContent
    val contentLength = content.getBytes("UTF-8").length
    val res = Response(status)
    res.setContentString(content)
    res.contentType = "application/json; charset=utf-8"
    res.contentLength = contentLength
    res
  }

  private def generateErrorBody(message: String): String =
    Json.stringify(Json.obj("error" -> message))

  private val notFoundErrorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""
}
