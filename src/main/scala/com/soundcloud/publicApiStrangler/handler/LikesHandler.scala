package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionHandler._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.service.LikesService
import com.soundcloud.publicApiStrangler.service.pagination.Pagination
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TracksCollection
import com.soundcloud.publicApiStrangler.support.{Bad, Good, Result}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.Json

import scala.util.control.NonFatal
import scala.util.{Success, Try}

class LikesHandler(
    userAuthentication: UserAuthentication,
    likesService: LikesService,
    baseUrl: String,
    exceptionCollector: ExceptionCollector
) {
  val logger = SoundCloudLoggerFactory.getLogger(getClass)
  private val numericRegexp = """\d+""".r

  def getUserLikedTrackId(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      val userId = req.routeParams("userId")
      performGetUserLikedTrackId(req, session, userId)
    }
  }

  def getMeLikedTrackId(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, userUrn) =>
      performGetUserLikedTrackId(req, session, userUrn.identifier)
    }
  }

  private def performGetUserLikedTrackId(
      request: HandlerRequest,
      session: UserSession,
      userId: String
  ): Future[Response] = {
    val trackId = request.routeParams("trackId")
    Try(Urn("soundcloud", "users", userId)) match {
      case Success(userUrn @ Urn(_, _, numericRegexp())) =>
        Try(Urn("soundcloud", "tracks", trackId)) match {
          case Success(trackUrn @ Urn(_, _, numericRegexp())) =>
            likesService
              .userTrackLikeForUrn(session, userUrn, trackUrn)
              .map {
                case None => JsonResponseBuilder.notFound(notFoundErrorString)
                case Some(track) => JsonResponseBuilder.ok(Json.stringify(Json.toJson(track)))
              }
              .handleAndReport(exceptionCollector) {
                case NonFatal(e) =>
                  logger.error(e.getMessage)
                  JsonResponseBuilder.internalServerError(generateErrorBody("an unexpected error occurred"))
              }
          case _ => Future.value(JsonResponseBuilder.notFound(notFoundErrorString))
        }
      case _ => Future.value(JsonResponseBuilder.notFound(notFoundErrorString))
    }
  }

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
    val hasLinkedPartitioning = request.params.get("linked_partitioning").isDefined
    val pagination = Pagination.buildCursorBasedPagination(request)

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
                TracksCollection.getRepresentation(tracksRepresentationResult, hasLinkedPartitioning)
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
