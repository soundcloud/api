package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorClient
import com.soundcloud.publicApiStrangler.client.trackcoordinator.datatypes._
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.service.client.OkidokiClient
import com.twitter.util.{Future, NonFatal}
import play.api.libs.json.Json

/**
 * Overrides the public api endpoints for editing tracks
 * Reason for overriding is to re-route updating and deleting tracks through
 * track-coordinator, which implements the correct restrictions.
 */
class TracksController(userAuthentication: UserAuthentication,
                       trackCoordinator: TrackCoordinatorClient,
                       okidokiClient: OkidokiClient,
                       mothershipDispatcher: DispatchToMothershipHandler,
                       trackmetadataClient: TrackmetadataClient)
    extends BffInjectionBasedController {

  get("/tracks/:trackId/comments")(mothershipDispatcher.dispatch)
  get("/tracks/:trackId/comments/")(mothershipDispatcher.dispatch)
  get("/tracks/:trackId/comments.json")(mothershipDispatcher.dispatch)
  get("/tracks/:trackId/comments.json/")(mothershipDispatcher.dispatch)

  get("/tracks/:trackId/download")(mothershipDispatcher.dispatch)
  get("/tracks/:trackId/download/")(mothershipDispatcher.dispatch)
  get("/tracks/:trackId/download.json")(mothershipDispatcher.dispatch)
  get("/tracks/:trackId/download.json/")(mothershipDispatcher.dispatch)

  post("/tracks/:trackId")(mothershipDispatcher.dispatch)
  post("/tracks/:trackId.json")(mothershipDispatcher.dispatch)

  post("/users/:userId/tracks")(mothershipDispatcher.dispatch)

  put("/tracks/:trackId")(handlePut)
  put("/tracks/:trackId.json")(handlePut)

  delete("/tracks/:trackId")(handleDelete)

  private def handleDelete(request: Request): Future[ResponseBuilder] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      trackCoordinator.deleteTrack(session, trackUrn(request)).map {
        case Success(()) => render.ok
        case NotFound => render.notFound
        case _ => render.internalServerError
      }
    }
  }

  private def handlePut(request: Request): Future[ResponseBuilder] =
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      val urn = trackUrn(request)
      trackmetadataClient.track(session, urn).flatMap {
        case Some(track) => {
          track.supply_chain_status match {
            // only allow updating manually uploaded tracks
            case Some("manual_upload") => mothershipDispatcher.dispatch(request)
            case Some(_) => Future.value(render.unauthorized.json(Json.obj("reason" -> "not allowed")))
            case None => mothershipDispatcher.dispatch(request)
          }
        }
        case None => Future.value(render.notFound)
      }.handle {
        case NonFatal(_) => render.internalServerError
      }
    }

  private def trackUrn(request: Request): Urn = {
    val IdParamPattern = "(\\d+)".r
    new Urn(request.routeParams("trackId") match {
      case IdParamPattern(id) => s"soundcloud:tracks:$id"
    })
  }
}
