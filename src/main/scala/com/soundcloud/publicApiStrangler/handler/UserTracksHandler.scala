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
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentation,
  TracksCollection
}
import com.soundcloud.publicApiStrangler.support.{Bad, Good, Result}
import com.soundcloud.publicApiStrangler.TrackUrnUtil.trackUrn
import com.soundcloud.publicApiStrangler.handler.representation.tracks.TrackRepresentationResponse.handleResponseFromService

import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, Return, Try}
import play.api.libs.json.Json

import scala.util.control.NonFatal

class UserTracksHandler(
    userAuthentication: UserAuthentication,
    userTracksService: UserTracksService,
    baseUrl: String,
    exceptionCollector: ExceptionCollector
) {
  val logger = SoundCloudLoggerFactory.getLogger(getClass)

  private val numericRegexp = """\d+""".r

  def getTrackByUser(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { (session) =>
      Try(trackUrn(req)) match {
        case Return(urn) =>
          val userId = req.routeParams("userId")
          getTrack(userId, session, urn, req)

        case _ => Future.value(JsonResponseBuilder.notFound(notFoundErrorString))
      }
    }
  }

  def getTrackByMe(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, userUrn) =>
      Try(trackUrn(req)) match {
        case Return(urn) =>
          val userId = userUrn.identifier
          getTrack(userId, session, urn, req)
        case _ =>
          Future.value(JsonResponseBuilder.notFound(notFoundErrorString))
      }
    }
  }

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

  private def getTrack(
      userId: String,
      session: UserSession,
      urn: Urn,
      req: HandlerRequest
  ): Future[Response] = {
    val secretToken = req.params.get("secret_token")

    performGetTrackByUser(userId, session, urn, secretToken)
      .map {
        case Good(trackRep) =>
          generateResponse(Status.Ok, Json.stringify(Json.toJson(trackRep)))
        case Bad(error: HttpError) => JsonResponseBuilder(error.status, generateErrorBody(error.description)).build
        case _ =>
          generateResponse(Status.InternalServerError, "Something went wrong while fetching a track")
      }
      .handleAndReport(exceptionCollector) {
        case NonFatal(e) =>
          generateResponse(
            Status.InternalServerError,
            "An unexpected error occured while fetching a track"
          )
      }
  }

  private def performGetTrackByUser(
      userId: String,
      session: UserSession,
      urn: Urn,
      secretToken: Option[String]
  ): Future[Result[TrackRepresentation]] = {
    userTracksService
      .userTrack(
        urn,
        session,
        userId,
        secretToken
      )
      .map {
        case Some(track) => Good(track)
        case _ => Bad(HttpError(Status.NotFound))
      }
  }

  private def performGetTracks(req: HandlerRequest, session: UserSession, userId: String): Future[Response] = {
    val hasLinkedPartitioning = req.params.get("linked_partitioning").isDefined
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
      case Return(urn @ Urn(_, _, numericRegexp())) =>
        val trackRepresentation = fetchTrackRepresentation(urn)
        handleResponseFromService(trackRepresentation, hasLinkedPartitioning)
      case _ => Future.value(JsonResponseBuilder.notFound(notFoundErrorString))
    }
  }

  private def generateErrorBody(message: String): String =
    Json.stringify(Json.obj("error" -> message))

  private def generateResponse(status: Status, rawContent: String): Response = {
    JsonResponseBuilder(status = status, body = rawContent).build
  }

  private val notFoundErrorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""
}
