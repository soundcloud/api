package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.jvmkit.Urn
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.client.trackcoordinator.datatypes.{NotFound, ServerError, Success}
import com.soundcloud.publicApiStrangler.TrackRepresentationsService
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.json.Json
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, NonFatal, Return, Try}

import scala.collection.JavaConversions._

class SingleTrackController(userAuthentication: UserAuthentication,
                            mothershipDispatcher: DispatchToMothershipHandler,
                            tracksService: TrackRepresentationsService,
                            telemetry: Telemetry)
  extends BffInjectionBasedController {

  private val numericRegexp = """\d+""".r

  get("/tracks/:trackId")(renderTrack)
  get("/tracks/:trackId/")(renderTrack)
  get("/tracks/:trackId.json")(renderTrack)
  get("/tracks/:trackId.json/")(renderTrack)

  private def renderTrack(req: Request): Future[ResponseBuilder] = {
    stripConditionalRequestHeaders(req)

    userAuthentication.withUserSession(req) { case session =>
      val trackId = req.routeParams("trackId")
      val callback = req.params.get("callback")

      Try(new Urn("soundcloud", "tracks", trackId)) match {
        case Return(urn@Urn(_, _, numericRegexp())) => {
          val secretToken = req.params.get("secret_token")
          tracksService.track(session, urn, secretToken).map {
            case Success(trackRep) => toResponseBuilder(generateResponse(Status.Ok, Json.stringify(trackRep), callback))
            case NotFound => generateNotFound(callback)
            case _ => {
              logger.error(s"Something went wrong while trying to fetch $urn")
              toResponseBuilder(generateResponse(Status.InternalServerError, "Something went wrong while fetching a track", callback))
            }
          } handle {
            case NonFatal(e) => {
              logger.error(e.getMessage)
              toResponseBuilder(generateResponse(Status.InternalServerError, "An unexpected error occured while fetching a track", callback))
            }
          }
        }
        case _ => Future.value(generateNotFound(callback))
      }
    }
  }

  private def generateNotFound(callback: Option[String]): ResponseBuilder= {
    val content = jsonpWrapper(callback, notFoundErrorString)
    val contentLength = content.getBytes("UTF-8").length
    val res = Response(Status.NotFound)
    res.setContentString(content)
    res.contentType = "application/json; charset=utf-8"
    res.contentLength = contentLength
    toResponseBuilder(res)
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
