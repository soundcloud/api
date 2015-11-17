package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.publicApiStrangler.mapper.trackcoordinator.TrackCoordinatorMapper
import com.soundcloud.service.response.mapper.TrackMapper
import com.soundcloud.service.request.representation.MissingValue
import com.soundcloud.scalakit.{Urn, UserSession}
import com.soundcloud.trackcoordinator.client.representation.{Errors, NotFound, Success, Track => CoordinatorTrack}
import com.soundcloud.trackcoordinator.client.TrackCoordinatorClient
import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.scalakit.finagle.jsonservice.Params
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.twitter.util.Future
import play.api.libs.json.{JsValue, JsObject, Reads}
import com.soundcloud.scalakit.json.Json
import org.joda.time.DateTime


/**
 * Overrides the public api endpoints for editing tracks
 * Reason for overriding is to re-route updating and deleting tracks through
 * track-coordinator, which implements the correct restrictions.
 */
class TracksController(userAuthentication: UserAuthentication,
                       trackCoordinator: TrackCoordinatorClient,
                       mothershipDispatcher: DispatchToMothershipHandler)
    extends BffInjectionBasedController {

  get("/tracks/:trackId")(request => mothershipDispatcher.dispatch(request))
  post("/tracks/:trackId")(request => mothershipDispatcher.dispatch(request))

  put("/tracks/:trackId") { request =>
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      val trackUpdate = TrackCoordinatorMapper.trackUpdateFromPublicApiTrack(request.getContentString)
      trackUpdate.map { updateCommand =>
        trackCoordinator.updateTrack(session,
                                     trackUrn(request),
                                     updateCommand,
                                     headers(request)).map {
        case Success(track) => render.json(TrackCoordinatorMapper.publicApiTrackFromCoordinatorTrack(track))
        case NotFound => render.notFound
        case Errors(lst) => render.status(422)
        case _ => render.internalServerError
      }
      }.getOrElse(Future.value(render.status(422)))
    }
  }

  delete("/tracks/:trackId") { request =>
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      trackCoordinator.deleteTrack(session, trackUrn(request)).map {
        case Success(()) => render.ok
        case NotFound => render.notFound
        case _ => render.internalServerError
      }
    }
  }

  private def trackUrn(request: Request): Urn = {
    val IdParamPattern = "(\\d+)".r
    Urn(request.routeParams("trackId") match {
          case IdParamPattern(id) => s"soundcloud:tracks:$id"
        })
  }

  private def headers(request: Request): Params = request.headerMap.iterator.toMap
}
