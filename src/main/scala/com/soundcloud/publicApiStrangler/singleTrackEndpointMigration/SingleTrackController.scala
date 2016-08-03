package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.json.Json
import com.twitter.finagle.http.Response
import com.twitter.util.{Future, Return, Throw, Try}
import play.api.libs.json.{Json => PlayJson}

import scala.collection.JavaConversions._


class SingleTrackController(userAuthentication: UserAuthentication,
                            mothershipDispatcher: DispatchToMothershipHandler,
                            tracksService: TracksService,
                            responseComparison: ResponseComparison)
  extends BffInjectionBasedController {

  get("/tracks/:trackId")(renderTrack)
  get("/tracks/:trackId/")(renderTrack)

  private def renderTrack(req: Request): Future[ResponseBuilder] = {
    Try(Urn("soundcloud", "tracks", req.routeParams("trackId"))) match {
      case Throw(_) => Future.value(render.notFound.body("Track id is not valid."))

      case Return(urn) => {
        Future.join(legacyResponse(req), migrationResponse(urn)) map {
          case (legacyResponseResult, migrationResponseResult) =>
            responseComparison.report(legacyResponseResult.contentString, migrationResponseResult.contentString)
            toResponseBuilder(legacyResponseResult)
        }
      }
    }
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
