package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.authorization.ContentAuthorizationFilter
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.soundcloud.scalakit.{Urn, UserSession}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request => FinagleRequest, Response}
import com.twitter.util.{Future, Return, Try}

import scala.collection.JavaConversions._

class SingleTrackController(userAuthentication: UserAuthentication,
                            mothershipDispatcher: DispatchToMothershipHandler,
                            contentAuthorizationFilter: ContentAuthorizationFilter,
                            tracksService: TracksService,
                            responseComparison: ResponseComparison,
                            telemetry: Telemetry,
                            contentAuthEnabledInController: () => Future[Boolean])
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
            val secretToken = req.params.get("secret_token")
            Future.join(legacyResponse(req), migrationResponse(session, urn, secretToken)) map {
              case (legacyResponseResult, migrationResponseResult) =>
                responseComparison.report(req, legacyResponseResult, migrationResponseResult)
                toResponseBuilder(legacyResponseResult)
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

  private def legacyResponse(req: Request): Future[Response] = {
    contentAuthEnabledInController().flatMap {
      case true =>
        val service = new Service[FinagleRequest, RouterResponse] {
          override def apply(request: FinagleRequest): Future[RouterResponse] =
            mothershipDispatcher.dispatchToMothership(req).map(RouterResponse(_, "undefined"))
        }
        contentAuthorizationFilter(req, service).map(_.underlying).map(addSkipContentAuthHeader)
      case false =>
        mothershipDispatcher.dispatchToMothership(req)
    }
  }

  private def migrationResponse(session: UserSession, urn: Urn, secret: Option[String]): Future[Response] = {
    tracksService.track(session, urn, None)
  }

  private def toResponseBuilder(response: Response): ResponseBuilder = {
    val headerMap = response.headerMap.entrySet().map(entry => (entry.getKey, entry.getValue)).toMap
    new ResponseBuilder()
      .status(response.status.code)
      .body(response.getContentString())
      .headers(headerMap)
  }

  /*
  * This magic header is picked up in ContentAuthorizationFilter
  * and its existence causes content authorization to be skipped on the filter for this response.
  * We are doing this because we manually do the content authorization in the controller.
  */
  private def addSkipContentAuthHeader(response: Response): Response = {
    response.headerMap.add(ContentAuthorizationFilter.skipContentAuthHeader, "true")
    response
  }

  /*
  * If-None-Match header causes mothership to return 304
  * We decided not to support this behavior
  */
  private def stripConditionalRequestHeaders(req: Request): Option[String] = {
    req.headerMap.remove("If-None-Match")
  }
}
