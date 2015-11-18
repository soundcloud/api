package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.publicApiStrangler.mapper.trackcoordinator.TrackCoordinatorMapper
import com.soundcloud.service.client.OkidokiClient
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
                       okidokiClient: OkidokiClient,
                       mothershipDispatcher: DispatchToMothershipHandler)
    extends BffInjectionBasedController {

  get("/tracks/:trackId")(request => mothershipDispatcher.dispatch(request))
  post("/tracks/:trackId")(request => mothershipDispatcher.dispatch(request))

  put("/tracks/:trackId") { request =>
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      val trackUpdate = TrackCoordinatorMapper.trackUpdateFromPublicApiTrack(request.getContentString) _
      val urn = trackUrn(request)
      val result = for {
        track <- trackCoordinator.fetchTrack(session, urn, Params.empty)
        update <- trackUpdate(track).map { updateCommand =>
          trackCoordinator.updateTrack(session,
                                       urn,
                                       updateCommand,
                                       headers(request))
          }.getOrElse(Future.value(Errors(List.empty)))
        user <- okidokiClient.fetch(session, track.asOption.map(_.user_urn).toSet).map(_.headOption)
      } yield (user, update)
      result.map {
        case (Some(user), Success(track)) =>
          render.json(TrackCoordinatorMapper
                        .publicApiTrackFromCoordinatorTrack(track, user))
        case (_, NotFound) => render.notFound
        case (_, Errors(lst)) => render.status(422)
        case _ => render.internalServerError
      }
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
