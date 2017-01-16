package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.jvmkit.Urn
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.TrackRepresentationsService
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.publicApiStrangler.support.migration.TrackCollectionResponseComparison
import com.soundcloud.scalakit.json.Json
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, NonFatal, Return, Try}

import scala.collection.JavaConversions._

class UserTracksController(userAuthentication: UserAuthentication,
                           mothershipDispatcher: DispatchToMothershipHandler,
                           tracksService: TrackRepresentationsService,
                           telemetry: Telemetry)
  extends BffInjectionBasedController {

  private val numericRegexp = """\d+""".r

  private val shouldCompareResponse = () => Future.False
  private val responseComparison = new TrackCollectionResponseComparison(telemetry)

//  get("/users/:id/tracks")(userRelatedMothershipDispatcher.dispatchToMothership _)
//  get("/users/:id/tracks/")(userRelatedMothershipDispatcher.dispatchToMothership _)
//  get("/users/:id/tracks.json")(userRelatedMothershipDispatcher.dispatchToMothership _)
//  get("/users/:id/tracks.json/")(userRelatedMothershipDispatcher.dispatchToMothership _)

  get("/users/:userId/tracks")(renderTracks)
  get("/users/:userId/tracks/")(renderTracks)
  get("/users/:userId/tracks.json")(renderTracks)
  get("/users/:userId/tracks.json/")(renderTracks)

  private def handleRequest(req: Request): Future[ResponseBuilder] = {
    shouldCompareResponse().flatMap {
      case true  => compareResponse(req)
      case false => mothershipDispatcher.dispatchToMothership(req)
    }.map(toResponseBuilder)
  }

  private def compareResponse(req: Request): Future[Response] = {
    Future.join(mothershipDispatcher.dispatchToMothership(req), buildResponse(req)).map {
      case (mothership, migration) =>
        responseComparison.report(req, mothership, migration)
        mothership
    }
  }

  private def renderTracks(req: Request): Future[ResponseBuilder] = {
    buildResponse(req).map(toResponseBuilder)
  }

  private def buildResponse(req: Request): Future[Response] = {
    stripConditionalRequestHeaders(req)

    userAuthentication.withUserSession(req) { case session =>
      val userId = req.routeParams("userId")
      val callback = req.params.get("callback")

      Try(new Urn("soundcloud", "users", userId)) match {
        case Return(urn@Urn(_, _, numericRegexp())) => {
          // TODO add limit and offset
          tracksService.tracks(session, urn, None, None).map { tracks =>
            generateResponse(Status.Ok, Json.stringify(tracks), callback)
          } handle {
            case NonFatal(e) => {
              logger.error(e.getMessage)
              generateResponse(Status.InternalServerError, "An unexpected error occured while fetching a track", callback)
            }
          }
        }
        case _ => Future.value(generateNotFound(callback))
      }
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

  /**
    * This JsonpWrapper logic should go to filter,
    * but should be applied only to the migrated endpoitns.
    */
  private def jsonpWrapper(callback: Option[String], contentString: String): String =
    callback.map(cb => s"/**/$cb($contentString);").getOrElse(contentString)

  private val notFoundErrorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""

  private def toResponseBuilder(response: Response): ResponseBuilder = {
    val headerMap = response.headerMap.entrySet().map(entry => (entry.getKey, entry.getValue)).toMap
    new ResponseBuilder()
      .status(response.status.code)
      .body(response.getContentString())
      .headers(headerMap)
  }

  /*
  * If-None-Match header causes mothership to return 304
  * We decided not to support this behavior
  */
  private def stripConditionalRequestHeaders(req: Request): Option[String] = {
    req.headerMap.remove("If-None-Match")
  }
}
