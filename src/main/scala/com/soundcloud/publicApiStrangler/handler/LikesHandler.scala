package com.soundcloud.publicApiStrangler.handler

import java.net.URL

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionHandler._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.service.LikesService
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackPagination, TracksCollection}
import com.soundcloud.publicApiStrangler.support.{Bad, Good, Result}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.Json

import scala.util.{Success, Try}
import scala.util.control.NonFatal

class LikesHandler(
    userAuthentication: UserAuthentication,
    likesService: LikesService,
    baseUrl: String,
    exceptionCollector: ExceptionCollector
) {
  val logger = SoundCloudLoggerFactory.getLogger(getClass)
  private val numericRegexp = """\d+""".r

  def getUserTracksLikes(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      val userId = req.routeParams("userId")
      performGetTracksLikes(req, session, userId)
    }
  }

  def getMeTracksLikes(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, userUrn) =>
      performGetTracksLikes(req, session, userUrn.identifier)
    }
  }

  private def performGetTracksLikes(request: HandlerRequest, session: UserSession, userId: String): Future[Response] = {
    val pagination = TrackPagination.fromRequest(request.params, new URL(baseUrl + request.uri))

    def fetchTrackRepresentation(urn: Urn): Future[Result[TracksCollection]] = {
      likesService
        .userTracksLikes(session, urn, pagination)
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
