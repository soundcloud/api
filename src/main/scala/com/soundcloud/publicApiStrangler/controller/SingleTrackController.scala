package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.TrackRepresentationsService
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, Return, Try}

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

    userAuthentication.withUserSession(req) {
      case session =>
        val trackId = req.routeParams("trackId")
        val callback = req.params.get("callback")

        Try(new Urn("soundcloud", "tracks", trackId)) match {

          case Return(urn@Urn(_, _, numericRegexp())) => {
            val secretToken = req.params.get("secret_token")
            tracksService.track(session, urn, secretToken, callback).liftToTry.map {
              case Return(response) => toResponseBuilder(response)
              case _ => new ResponseBuilder().status(Status.InternalServerError.code)
            }
          }
          case _ =>
            generateNotFound(callback)
        }
    }
  }

  private def generateNotFound(callback: Option[String]): Future[ResponseBuilder] = {
    val errorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""
    val contentString = callback.map(cb => s"/**/$cb($errorString);").getOrElse(errorString)
    val contentLength = contentString.getBytes("UTF-8").length
    val res = Response(Status.NotFound)
    res.setContentString(contentString)
    res.contentType = "application/json; charset=utf-8"
    res.contentLength = contentLength
    Future.value(toResponseBuilder(res))
  }

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
