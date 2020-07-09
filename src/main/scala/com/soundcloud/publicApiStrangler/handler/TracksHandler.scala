package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.TrackUrnUtil.trackUrn
import com.soundcloud.publicApiStrangler.client.mothership.OkidokiClient
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorClient
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.twitter.finagle.http.Response
import com.twitter.util.Future
import org.slf4j.Logger
import play.api.libs.json.Json

import scala.util.control.NonFatal

/**
  * Overrides the public api endpoints for editing tracks
  * Reason for overriding is to re-route updating and deleting tracks through
  * track-coordinator, which implements the correct restrictions.
  */
class TracksHandler(
    userAuthentication: UserAuthentication,
    trackCoordinator: TrackCoordinatorClient,
    okidokiClient: OkidokiClient,
    mothershipDispatcher: DispatchToMothershipHandler,
    trackmetadataClient: TrackmetadataClient,
    logger: Logger = SoundCloudLoggerFactory.getLogger(getClass)
) {
  def handleDelete(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      trackCoordinator.deleteTrack(session, trackUrn(request)).map {
        case Good(()) => ResponseBuilder.ok()
        case Bad(NotFound(_)) => ResponseBuilder.notFound()
        case _ => ResponseBuilder.internalServerError()
      }
    }
  }

  def handlePut(request: HandlerRequest): Future[Response] =
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      val urn = trackUrn(request)
      logger.info(
        s"PUT /tracks${urn.identifier} | request media type ${request.mediaType} | request body: ${request.contentString} "
      )
      trackmetadataClient
        .track(session, urn)
        .flatMap {
          case Some(track) => {
            track.supply_chain_status match {
              // only allow updating manually uploaded tracks
              case Some("manual_upload") => mothershipDispatcher.dispatch(request)
              case Some(_) =>
                Future.value(JsonResponseBuilder.unauthorized(Json.stringify(Json.obj("reason" -> "not allowed"))))
              case None => mothershipDispatcher.dispatch(request)
            }
          }
          case None => Future.value(ResponseBuilder.notFound())
        }
        .handle {
          case NonFatal(_) => ResponseBuilder.internalServerError()
        }
    }
}
