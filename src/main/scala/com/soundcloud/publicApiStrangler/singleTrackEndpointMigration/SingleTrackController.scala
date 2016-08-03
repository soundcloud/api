package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.json.Json
import com.twitter.finagle.http.Response
import com.twitter.util.{Future, Return, Throw, Try}
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.{Json => PlayJson}

import scala.collection.JavaConversions._


class SingleTrackController(userAuthentication: UserAuthentication,
                            mothershipDispatcher: DispatchToMothershipHandler,
                            tracksService: TracksService,
                            responseComparison: ResponseComparison,
                            telemetry: Telemetry)
  extends BffInjectionBasedController {

  private val numericRegexp = """\d+""".r

  private val nonNumericTrackIdCounter = telemetry.counter(
    "non_numeric_track_id",
    "counter for non numeric track ids",
    "type"
  )

  get("/tracks/:trackId")(renderTrack)
  get("/tracks/:trackId/")(renderTrack)

  private def renderTrack(req: Request): Future[ResponseBuilder] = {

    Try(Urn("soundcloud", "tracks", req.routeParams("trackId"))) match {
      case Return(urn@Urn(_, _, numericRegexp())) => {
        Future.join(legacyResponse(req), migrationResponse(urn)) map {
          case (legacyResponseResult, migrationResponseResult) =>
            responseComparison.report(legacyResponseResult.contentString, migrationResponseResult.contentString)
            toResponseBuilder(legacyResponseResult)
        }
      }

      case Return(urn) =>
        nonNumericTrackIdCounter.labels("valid").inc() // valid non numeric identifier
        legacyResponse(req).map(toResponseBuilder)

      case Throw(_) =>
        nonNumericTrackIdCounter.labels("invalid").inc() // invalid identifier
        Future.value(invalidTrackIdRespone)
    }
  }

  private def invalidTrackIdRespone = {
    val status = HttpResponseStatus.NOT_FOUND
    val errorMessage = s"${status.getCode} - ${status.getReasonPhrase} - Track id is not valid"

    render.notFound.
      header("Status", status.getCode + " " + status.getReasonPhrase).
      header("Date", DateTime.now.toString(DateTimeFormat.forPattern("E, d MMM yyyy HH:mm:ss z"))).
      header("Content-Type", "application/json; charset=utf-8").
      body(s"""{"errors":[{"error_message":"$errorMessage"}]}""")
  }

  private def legacyResponse(req: Request): Future[Response] =
    mothershipDispatcher.dispatchToMothership(req)

  private def migrationResponse(urn: Urn): Future[Response] = {
    tracksService.track(urn).map {
      track =>
        val res = Response()
        implicit val format = PlayJson.format[SingleTrackPublicApiRepresentation] // Sam really enjoys this
        res.setContentString(Json.stringify(track))
        res
    }
  }

  private def toResponseBuilder(response: Response): ResponseBuilder = {
    val headerMap = response.headerMap.entrySet().map(entry => (entry.getKey, entry.getValue)).toMap
    new ResponseBuilder()
      .status(response.status.code)
      .body(response.getContentString())
      .headers(headerMap)
  }
}
