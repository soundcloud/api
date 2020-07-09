package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.TrackUrnUtil.trackUrn
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorClient
import com.soundcloud.publicApiStrangler.handler.representation.tracks.TrackRepresentationResponse.handleTrackRepresentationResponseFromService
import com.soundcloud.publicApiStrangler.handler.support.requestParser.{
  TrackArtworkUpdateRequest,
  TrackAssetDataUpdateRequest,
  TrackMetadataUpdateRequest
}
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackUpdateService
import com.soundcloud.publicApiStrangler.support.oauth.RailsLikeParamsParser
import com.twitter.finagle.http._
import com.twitter.util.Future
import play.api.libs.json.Json

/**
  * Overrides the public api endpoints for editing tracks
  * Reason for overriding is to re-route updating and deleting tracks through
  * track-coordinator, which implements the correct restrictions.
  */
class TracksHandler(
    userAuthentication: UserAuthentication,
    trackCoordinator: TrackCoordinatorClient,
    trackUpdateService: TrackUpdateService,
    paramsParser: RailsLikeParamsParser = new RailsLikeParamsParser
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

  def handleUpdateTrack(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      val urn = trackUrn(request)

      request.mediaType match {
        case Some(MediaType.MultipartForm) => updateTrackFromMultipartRequest(request, session, urn)
        case Some(MediaType.Json) => updateTrackFromJsonRequest(request, session, urn)
        case _ => generateBadResponse
      }
    }
  }

  def updateTrackFromMultipartRequest(request: HandlerRequest, session: UserSession, urn: Urn): Future[Response] = {

    /**
      *  Ugly, but we have to manually change the request method from PUT to POST, otherwise MultiPart.Decode will return None
      *  We change it back to Put after the parse call to avoid any potential side effects
      *  https://twitter.github.io/finagle/docs/com/twitter/finagle/http/exp/MultipartDecoder.html
      *  https://softwareengineering.stackexchange.com/a/319429
      */
    request.method = Method.Post
    val metadataUpdateParams = paramsParser.parse(request)
    val albumArtworkUpdate = paramsParser.parseFilesFromRequest(request, "track[artwork_data]")
    request.method = Method.Put

    val artworkDataUpdates = albumArtworkUpdate.map(TrackArtworkUpdateRequest)

    val extractedParams = metadataUpdateParams.map(extractTrackFieldsFromParams)
    val metadataUpdates = extractedParams.map(TrackMetadataUpdateRequest.fromMultipartForm).getOrElse(None)
    val assetDataUpdates = extractedParams.map(TrackAssetDataUpdateRequest.fromMultipartForm).getOrElse(None)

    (artworkDataUpdates, metadataUpdates, assetDataUpdates) match {
      case (None, None, None) => generateBadResponse
      case _ =>
        val response =
          trackUpdateService.updateTrack(
            artworkDataUpdates,
            assetDataUpdates,
            metadataUpdates,
            urn,
            session
          )
        handleTrackRepresentationResponseFromService(response)
    }
  }

  def updateTrackFromJsonRequest(request: HandlerRequest, session: UserSession, urn: Urn): Future[Response] = {
    val metadataUpdateParams = Json.parse(request.contentString).asOpt[TrackMetadataUpdateRequest]
    metadataUpdateParams match {
      case Some(_) =>
        val response = trackUpdateService.updateTrack(None, None, metadataUpdateParams, urn, session)
        handleTrackRepresentationResponseFromService(response)
      case _ => generateBadResponse
    }
  }

  private def extractTrackFieldsFromParams(multipartParams: Map[String, String]): Map[String, String] = {
    val trackPattern = """track\[(\S+)\]""".r

    multipartParams.foldLeft(Map[String, String]()) {
      case (acc, (key, value)) =>
        key match {
          case trackPattern(field) => acc + (field -> value)
          case _ => acc
        }
    }
  }

  private def generateBadResponse: Future[Response] = Future.value(JsonResponseBuilder.badRequest(invalidRequestString))
  private val invalidRequestString = """{"errors":[{"error_message":"400 - Invalid Request"}]}"""
}
