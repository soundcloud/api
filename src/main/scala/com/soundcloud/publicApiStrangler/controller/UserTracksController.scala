package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.jvmkit.module.util.Result.TryToResult
import com.soundcloud.jvmkit.module.util.{Bad, ErrorLike, Good}
import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.jvmkit.Urn
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.TrackRepresentationsService
import com.soundcloud.publicApiStrangler.representation.TrackRepresentationLike
import com.soundcloud.publicApiStrangler.service.TrackPagination
import com.soundcloud.publicApiStrangler.support.migration.TrackCollectionResponseComparison
import com.soundcloud.scalakit.json.Json
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, NonFatal}

import scala.util.{Success, Try}
import scala.collection.JavaConversions._

class UserTracksController(userAuthentication: UserAuthentication,
                           mothershipDispatcher: TrackMothershipDispatcherWithCounts,
                           tracksService: TrackRepresentationsService,
                           telemetry: Telemetry,
                           shouldCompareResponse: () => Future[Boolean])
  extends BffInjectionBasedController {

  private val numericRegexp = """\d+""".r

  private val responseComparison = new TrackCollectionResponseComparison(telemetry)

  get("/users/:userId/tracks")(handleRequest)
  get("/users/:userId/tracks/")(handleRequest)
  get("/users/:userId/tracks.json")(handleRequest)
  get("/users/:userId/tracks.json/")(handleRequest)

  private def handleRequest(req: Request): Future[ResponseBuilder] = {
    shouldCompareResponse().flatMap {
      case true  => compareResponse(req)
      case false => mothershipDispatcher.request(req)
    }
  }

  private def compareResponse(req: Request): Future[ResponseBuilder] = {
    Future.join(mothershipDispatcher.request(req), buildResponse(req)).map {
      case (mothership, migration) =>
        responseComparison.report(req, mothership.build(req), migration)
        mothership
    }
  }

  private def buildResponse(req: Request): Future[Response] = {
    userAuthentication.withUserSession(req) { case session =>
      val userId = req.routeParams("userId")
      val callback = req.params.get("callback")

      def getResult(urn: Urn) = {
        tracksService.tracks(session, urn, TrackPagination.fromRequest(req))
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
            case Good(tracks: List[TrackRepresentationLike]) => generateResponse(Status.Ok, Json.stringify(tracks), callback)
            case Bad(error: HttpError) => generateResponse(error.status, error.message, callback)
            case Bad(error) => generateResponse(Status.InternalServerError, error.toString, callback)
          }
        }
        case _ => Future.value(generateNotFound(callback))
      }
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
