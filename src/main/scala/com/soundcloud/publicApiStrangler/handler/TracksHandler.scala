package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorClient
import com.soundcloud.publicApiStrangler.handler.representation.tracks.TrackRepresentationResponse.{
  handleCreateTrackResponseFromService,
  handleUpdateTrackResponseFromService
}
import com.soundcloud.publicApiStrangler.handler.support.error.UnhandledOutcomeException
import com.soundcloud.publicApiStrangler.handler.support.requestParser._
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackUpdateService
import com.soundcloud.publicApiStrangler.support.ErrorResponse
import com.soundcloud.publicApiStrangler.support.TrackUrnUtil.getTrackUrn
import com.soundcloud.publicApiStrangler.support.oauth.RailsLikeParamsParser
import com.twitter.finagle.http._
import com.twitter.util.{Future, Return, Throw, Try}
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

  /**
    * Methods for track deletion
    */
  def handleDeleteTrack(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      Try(getTrackUrn(request)) match {
        case Return(urn) =>
          trackCoordinator.deleteTrack(session, urn).map {
            case Good(()) => ResponseBuilder.ok()
            case Bad(NotFound(_)) => ResponseBuilder.notFound()
            case _ => ResponseBuilder.internalServerError()
          }
        case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
      }
    }
  }

  /**
    * Methods for track update
    */
  def handleUpdateTrack(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      Try(getTrackUrn(request)) match {
        case Return(urn) =>
          request.mediaType match {
            case Some(MediaType.MultipartForm) => updateTrackFromMultipartRequest(request, session, urn)
            case Some(MediaType.Json) => updateTrackFromJsonRequest(request, session, urn)
            case Some(MediaType.WwwForm) => updateTrackFromUrlEncodedRequest(request, session, urn)
            case _ => generateBadResponse
          }
        case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
      }
    }
  }

  private def updateTrackFromMultipartRequest(
      request: HandlerRequest,
      session: UserSession,
      urn: Urn
  ): Future[Response] = {

    /**
      *  Ugly, but we have to manually change the request method from PUT to POST, otherwise MultiPart.Decode will return None
      *  We change it back to Put after the parse call to avoid any potential side effects
      *  https://twitter.github.io/finagle/docs/com/twitter/finagle/http/exp/MultipartDecoder.html
      *  https://softwareengineering.stackexchange.com/a/319429
      */
    request.method = Method.Post
    val extractedParams = paramsParser.parse(request).map(extractTrackFieldsFromParams)

    val artwork = paramsParser.parseFilesFromRequest(request, "track[artwork_data]").map(TrackArtworkUpdateRequest)
    val assetParams = extractedParams.map(TrackAssetDataUpdateRequest.fromForm).getOrElse(None)
    val metadataParams =
      extractedParams.map(TrackMetadataUpdateRequest.fromForm).getOrElse(None)

    request.method = Method.Put

    (artwork, metadataParams, assetParams) match {
      case (None, None, None) => generateBadResponse
      case _ => updateTrack(artwork, assetParams, metadataParams, urn, session)
    }
  }

  private def updateTrackFromUrlEncodedRequest(
      request: HandlerRequest,
      session: UserSession,
      urn: Urn
  ): Future[Response] = {
    val extractedParams = extractTrackFieldsFromParams(request.params)
    val assetData = TrackAssetDataUpdateRequest.fromForm(extractedParams)
    val metadata = TrackMetadataUpdateRequest.fromForm(extractedParams)

    (metadata, assetData) match {
      case (None, None) => generateBadResponse
      case _ => updateTrack(None, assetData, metadata, urn, session)
    }
  }

  private def updateTrackFromJsonRequest(request: HandlerRequest, session: UserSession, urn: Urn): Future[Response] = {
    val metadata = Json.parse(request.contentString).asOpt[TrackMetadataUpdateRequest]
    metadata match {
      case Some(_) => updateTrack(None, None, metadata, urn, session)
      case _ => generateBadResponse
    }
  }

  private def updateTrack(
      artwork: Option[TrackArtworkUpdateRequest],
      assetData: Option[TrackAssetDataUpdateRequest],
      metadata: Option[TrackMetadataUpdateRequest],
      trackUrn: Urn,
      session: UserSession
  ): Future[Response] = {
    val response =
      trackUpdateService.updateTrack(
        artwork,
        assetData,
        metadata,
        trackUrn,
        session
      )

    handleUpdateTrackResponseFromService(response)
  }

  /**
    * Methods for track creation
    */
  def handleCreateTrack(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      request.mediaType match {
        case Some(MediaType.MultipartForm) => createTrackFromMultipartRequest(request, session)
        case Some(MediaType.WwwForm) => createTrackFromUrlEncodedRequest(request, session)
        case _ => generateBadResponse
      }
    }
  }

  private def createTrackFromMultipartRequest(
      request: HandlerRequest,
      session: UserSession
  ): Future[Response] = {
    val extractedParams = paramsParser.parse(request).map(extractTrackFieldsFromParams)

    val artwork = paramsParser.parseFilesFromRequest(request, "track[artwork_data]").map(TrackArtworkUpdateRequest)
    val assetData = extractedParams.map(TrackAssetDataCreateRequest.fromForm).getOrElse(None)
    val metadata =
      extractedParams.map(TrackMetadataCreateRequest.fromForm).getOrElse(NotValid("Invalid request").bad)

    createTrack(metadata, assetData, artwork, session)
  }

  private def createTrackFromUrlEncodedRequest(
      request: HandlerRequest,
      session: UserSession
  ): Future[Response] = {
    val extractedParams = extractTrackFieldsFromParams(request.params)
    val assetData = TrackAssetDataCreateRequest.fromForm(extractedParams)
    val metadata = TrackMetadataCreateRequest.fromForm(extractedParams)
    createTrack(metadata, assetData, None, session)
  }

  private def createTrack(
      maybeMetadata: Outcome[TrackMetadataCreateRequest],
      maybeAssetData: Option[TrackAssetDataCreateRequest],
      artwork: Option[TrackArtworkUpdateRequest],
      session: UserSession
  ): Future[Response] = {
    (maybeAssetData, maybeMetadata) match {
      case (Some(assetData), Good(metadata)) =>
        val createdTrack = trackUpdateService.createTrack(
          assetData,
          artwork,
          metadata,
          session
        )
        handleCreateTrackResponseFromService(createdTrack)
      case (None, _) => invalidAssetDataResponse
      case (_, Bad(NotValid(errors))) => Future.value(ErrorResponse.badRequest(errors.head))
      case _ => throw new UnhandledOutcomeException
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

  private def generateBadResponse: Future[Response] =
    Future.value(ErrorResponse.badRequest("400 - Invalid Request"))

  private def invalidAssetDataResponse: Future[Response] =
    Future.value(ErrorResponse(Status.UnprocessableEntity, "unable to process provided asset_data"))
}
