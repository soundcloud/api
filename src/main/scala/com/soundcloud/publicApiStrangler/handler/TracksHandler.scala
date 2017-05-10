package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorClient
import com.soundcloud.publicApiStrangler.client.trackcoordinator.datatypes._
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.service.client.OkidokiClient
import com.twitter.finagle.http.Response
import com.twitter.util.{Future, NonFatal}
import play.api.libs.json.Json

/**
  * Overrides the public api endpoints for editing tracks
  * Reason for overriding is to re-route updating and deleting tracks through
  * track-coordinator, which implements the correct restrictions.
  */
class TracksHandler(userAuthentication: UserAuthentication,
                    trackCoordinator: TrackCoordinatorClient,
                    okidokiClient: OkidokiClient,
                    mothershipDispatcher: DispatchToMothershipHandler,
                    trackmetadataClient: TrackmetadataClient) {

  def handleDelete(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      trackCoordinator.deleteTrack(session, trackUrn(request)).map {
        case Success(()) => ResponseBuilder.ok()
        case NotFound => ResponseBuilder.notFound()
        case _ => ResponseBuilder.internalServerError()
      }
    }
  }

  def handlePut(request: HandlerRequest): Future[Response] =
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      val urn = trackUrn(request)
      trackmetadataClient.track(session, urn).flatMap {
        case Some(track) => {
          track.supply_chain_status match {
            // only allow updating manually uploaded tracks
            case Some("manual_upload") => mothershipDispatcher.dispatch(request)
            case Some(_) => Future.value(JsonResponseBuilder.unauthorized(Json.stringify(Json.obj("reason" -> "not allowed"))))
            case None => mothershipDispatcher.dispatch(request)
          }
        }
        case None => Future.value(ResponseBuilder.notFound())
      }.handle {
        case NonFatal(_) => ResponseBuilder.internalServerError()
      }
    }

  private def trackUrn(request: HandlerRequest): Urn = {
    val IdParamPattern = "(\\d+)".r
    new Urn(request.routeParams("trackId") match {
      case IdParamPattern(id) => s"soundcloud:tracks:$id"
    })
  }
}

