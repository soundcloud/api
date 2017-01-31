package com.soundcloud.publicApiStrangler.controller

import java.net.URL

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.jvmkit.Urn
import com.soundcloud.jvmkit.module.util.{Bad, ErrorLike, Good}
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.service.TrackPagination
import com.soundcloud.publicApiStrangler.{TrackRepresentationsService, TracksRepresentationResult}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, NonFatal}
import play.api.libs.json.Json

import scala.util.{Success, Try}

class UserTracksController(userAuthentication: UserAuthentication,
                           mothershipDispatcher: TrackMothershipDispatcherWithCounts,
                           tracksService: TrackRepresentationsService,
                           telemetry: Telemetry,
                           shouldUseTrackMetadata: () => Future[Boolean],
                           baseUrl: String)
  extends BffInjectionBasedController {

  private val numericRegexp = """\d+""".r

  get("/users/:userId/tracks")(handleRequest)
  get("/users/:userId/tracks/")(handleRequest)
  get("/users/:userId/tracks.json")(handleRequest)
  get("/users/:userId/tracks.json/")(handleRequest)

  private def handleRequest(req: Request): Future[ResponseBuilder] = {
    shouldUseTrackMetadata().flatMap {
      case true  => buildResponse(req).map(toResponseBuilder)
      case false => mothershipDispatcher.request(req)
    }
  }

  private def buildResponse(req: Request): Future[Response] = {
    userAuthentication.withUserSession(req) { case session =>
      val userId = req.routeParams("userId")
      val callback = req.params.get("callback")

      val pagination = TrackPagination.fromRequest(req.params, new URL(baseUrl + req.uri))

      def getResult(urn: Urn) = {
        tracksService.tracks(session, urn, pagination)
          .handle {
            case NonFatal(e) => {
              logger.error(e.getMessage)
              Bad(HttpError(Status.InternalServerError, "An unexpected error occurred while fetching the tracks"))
            }
          }
      }

      Try(Urn(s"soundcloud:users:$userId")) match {
        case Success(urn@Urn(_, _, numericRegexp())) => {
          getResult(urn).map {
            case Good(tracksRepresentationResult) => {
              generateResponse(Status.Ok, getRepresentation(tracksRepresentationResult, pagination), callback)
            }
            case Bad(error: HttpError) => generateResponse(error.status, Json.stringify(Json.obj("error" -> error.message)), callback)
            case Bad(error) => generateResponse(Status.InternalServerError, Json.stringify(Json.obj("error" -> error.toString)), callback)
          }
        }
        case _ => Future.value(generateNotFound(callback))
      }
    }
  }

  private def toResponseBuilder(response: Response): ResponseBuilder = {
    val headerMap = response.headerMap.iterator.map { case (key, value) => (key, value) }.toMap
    new ResponseBuilder()
      .status(response.statusCode)
      .body(response.getContentString())
      .headers(headerMap)
  }

  private def getRepresentation(result: TracksRepresentationResult, pagination: TrackPagination) = {
    if (pagination.linkedPartitioning) {
      val tracksJson = Json.obj("collection" -> Json.toJson(result.tracks))
      val json = result.nextHref.map(nextHref => {
        tracksJson ++ Json.obj("next_href" -> nextHref)
      }).getOrElse(tracksJson)

      Json.stringify(json)
    } else {
      Json.stringify(Json.toJson(result.tracks))
    }
  }

  case class HttpError(status: Status, message: String) extends ErrorLike

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

  /**
    * This JsonpWrapper logic should go to filter,
    * but should be applied only to the migrated endpoitns.
    */
  private def jsonpWrapper(callback: Option[String], contentString: String): String =
    callback.map(cb => s"/**/$cb($contentString);").getOrElse(contentString)

  private val notFoundErrorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""
}
