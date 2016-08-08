package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.TrackmetadataClient
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.json.Json
import com.soundcloud.scalakit.{Urn, UserSession}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, Return, Try}
import play.api.libs.json.{Json => PlayJson}

import scala.collection.JavaConversions._

class SingleTrackController(userAuthentication: UserAuthentication,
                            mothershipDispatcher: DispatchToMothershipHandler,
                            trackmetadataClient: TrackmetadataClient,
                            responseComparison: ResponseComparison,
                            telemetry: Telemetry,
                            singleTrackEndpointRollout: Urn => Future[Boolean])
  extends BffInjectionBasedController {

  private val numericRegexp = """\d+""".r

  private val nonNumericTrackIdCounter = telemetry.counter(
    "non_numeric_track_id",
    "counter for non numeric track ids",
    "statusCode"
  )

  get("/tracks/:trackId")(renderTrack)
  get("/tracks/:trackId/")(renderTrack)

  private def renderTrack(req: Request): Future[ResponseBuilder] = {

    stripConditionalRequestHeaders(req)

    userAuthentication.withUserSession(req) {
      case session =>
        val trackId = req.routeParams("trackId")

        Try(Urn("soundcloud", "tracks", trackId)) match {

          case Return(urn@Urn(_, _, numericRegexp())) => {
            singleTrackEndpointRollout(urn).flatMap {
              case true =>
                Future.join(legacyResponse(req), migrationResponse(session, urn)) map {
                  case (legacyResponseResult, migrationResponseResult) =>
                    responseComparison.report(legacyResponseResult, migrationResponseResult)
                    toResponseBuilder(legacyResponseResult)
                }
              case false =>
                legacyResponse(req).map(toResponseBuilder)
            }
          }

          case _ =>
            legacyResponse(req).map {
              case res =>
                nonNumericTrackIdCounter.labels(res.statusCode.toString).inc()
                toResponseBuilder(res)
            }
        }
    }
  }

  private def legacyResponse(req: Request): Future[Response] =
    mothershipDispatcher.dispatchToMothership(req)

  private def migrationResponse(session: UserSession, urn: Urn): Future[Response] = {
    trackmetadataClient.track(session, urn, None).map {
      case Some(track) =>

        if (track.public || track.user_urn == session.getUser) {
          val res = Response()
          val singleTrackPublicApiRepresentation = new SingleTrackPublicApiRepresentation(
            "track",
            track.urn.getIdentifier.toLong,
            track.user_urn.getIdentifier.toLong)
          res.setContentString(Json.stringify(singleTrackPublicApiRepresentation))
          res
        } else {
          Response(Status.Unauthorized)
        }
      case None =>
        Response(Status.NotFound)
    }
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
