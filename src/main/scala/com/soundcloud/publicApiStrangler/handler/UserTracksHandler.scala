package com.soundcloud.publicApiStrangler.handler

import java.net.URL

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionHandler._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.service.UserTracksService
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackPagination, TracksCollection}
import com.soundcloud.publicApiStrangler.support.{Bad, Good, Result, StringError}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.Json

import scala.util.control.NonFatal
import scala.util.{Success, Try}

class UserTracksHandler(
    userAuthentication: UserAuthentication,
    userTracksService: UserTracksService,
    baseUrl: String,
    exceptionCollector: ExceptionCollector
) {
  val logger = SoundCloudLoggerFactory.getLogger(getClass)

  private val numericRegexp = """\d+""".r

  def getUserTracks(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      val userId = req.routeParams("userId")
      performGetTracks(req, session, userId)
    }
  }

  def getMeTracks(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, userUrn) =>
      performGetTracks(req, session, userUrn.identifier)
    }
  }

  private def performGetTracks(req: HandlerRequest, session: UserSession, userId: String): Future[Response] = {
    val pagination = TrackPagination.fromRequest(req.params, new URL(baseUrl + req.uri))

    def fetchTrackRepresentation(urn: Urn): Future[Result[TracksCollection]] = {
      userTracksService
        .userTracks(session, urn, pagination)
        .map(Good(_))
        .handle {
          case NonFatal(e) =>
            logger.error(e.getMessage)
            Bad(HttpError(Status.InternalServerError))

        }
    }

    Try(Urn("soundcloud", "users", userId)) match {
      case Success(urn @ Urn(_, _, numericRegexp())) =>
        fetchTrackRepresentation(urn)
          .map {
            case Good(tracksRepresentationResult) =>
              JsonResponseBuilder.ok(
                TracksCollection.getRepresentation(tracksRepresentationResult, pagination)
              )
            case Bad(error: HttpError) =>
              JsonResponseBuilder(error.status, generateErrorBody(error.description)).build
            case Bad(error: StringError) =>
              JsonResponseBuilder.internalServerError(generateErrorBody(error.message))
            case Bad(_) =>
              JsonResponseBuilder.internalServerError(generateErrorBody("an unexpected error occurred"))
          }
          .handleAndReport(exceptionCollector) {
            case NonFatal(e) =>
              logger.error(e.getMessage)
              JsonResponseBuilder.internalServerError(generateErrorBody("an unexpected error occurred"))
          }
      case _ => Future.value(JsonResponseBuilder.notFound(notFoundErrorString))
    }
  }

  private def generateErrorBody(message: String): String =
    Json.stringify(Json.obj("error" -> message))

  private val notFoundErrorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""
}
