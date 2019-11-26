package com.soundcloud.publicApiStrangler.handler

import java.net.URL

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentationsService,
  TracksRepresentationResult
}
import com.soundcloud.publicApiStrangler.support.{Bad, Good, StringError}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.Json

import scala.util.control.NonFatal
import scala.util.{Success, Try}

class UserTracksHandler(
    userAuthentication: UserAuthentication,
    mothershipDispatcher: TrackMothershipDispatcherWithCounts,
    tracksService: TrackRepresentationsService,
    telemetry: Telemetry,
    shouldUseTrackMetadata: () => Future[Boolean],
    baseUrl: String
) {
  val logger = SoundCloudLoggerFactory.getLogger(getClass)

  private val numericRegexp = """\d+""".r

  def handleRequest(req: HandlerRequest): Future[Response] = {
    shouldUseTrackMetadata().flatMap {
      case true => buildResponse(req)
      case false => mothershipDispatcher.request(req)
    }
  }

  private def buildResponse(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) {
      case session =>
        val userId = req.routeParams("userId")
        val callback = req.params.get("callback")

        val pagination = TrackPagination.fromRequest(req.params, new URL(baseUrl + req.uri))

        def getResult(urn: Urn) = {
          tracksService
            .tracks(session, urn, pagination)
            .handle {
              case NonFatal(e) => {
                logger.error(e.getMessage)
                Bad(HttpError(Status.InternalServerError))
              }
            }
        }

        Try(Urn("soundcloud", "users", userId)) match {
          case Success(urn @ Urn(_, _, numericRegexp())) => {
            getResult(urn).map {
              case Good(tracksRepresentationResult) => {
                generateResponse(Status.Ok, getRepresentation(tracksRepresentationResult, pagination), callback)
              }
              case Bad(error: HttpError) =>
                generateResponse(error.status, generateErrorBody(error.description), callback)
              case Bad(error: StringError) =>
                generateResponse(Status.InternalServerError, generateErrorBody(error.message), callback)
              case Bad(_) =>
                generateResponse(
                  Status.InternalServerError,
                  generateErrorBody("an unexpected error occurred"),
                  callback
                )
            }
          }
          case _ => Future.value(generateNotFound(callback))
        }
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

  private def generateNotFound(callback: Option[String]): Response = {
    val content = jsonpWrapper(callback, notFoundErrorString)
    val contentLength = content.getBytes("UTF-8").length
    val res = Response(Status.NotFound)
    res.setContentString(content)
    res.contentType = "application/json; charset=utf-8"
    res.contentLength = contentLength
    res
  }

  private def generateResponse(status: Status, rawContent: String, callback: Option[String]): Response = {
    val content = jsonpWrapper(callback, rawContent)
    val contentLength = content.getBytes("UTF-8").length
    val res = Response(status)
    res.setContentString(content)
    res.contentType = "application/json; charset=utf-8"
    res.contentLength = contentLength
    res
  }

  private def generateErrorBody(message: String): String =
    Json.stringify(Json.obj("error" -> message))

  /**
    * This JsonpWrapper logic should go to filter,
    * but should be applied only to the migrated endpoitns.
    */
  private def jsonpWrapper(callback: Option[String], contentString: String): String =
    callback.map(cb => s"/**/$cb($contentString);").getOrElse(contentString)

  private val notFoundErrorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""
}
