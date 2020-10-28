package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.support.TrackUrnUtil.getTrackUrn
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorClient
import com.soundcloud.publicApiStrangler.handler.representation.tracks.TrackRepresentationResponse.{
  handleCreateTrackResponseFromService,
  handleTrackRepresentationResponseFromService
}
import com.soundcloud.publicApiStrangler.handler.support.requestParser._
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackUpdateService
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

  def handleDelete(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      Try(getTrackUrn(request)) match {
        case Return(urn) =>
          trackCoordinator.deleteTrack(session, urn).map {
            case Good(()) => ResponseBuilder.ok()
            case Bad(NotFound(_)) => ResponseBuilder.notFound()
            case _ => ResponseBuilder.internalServerError()
          }
        case Throw(e) => Future.value(JsonResponseBuilder.badRequest(e.getMessage))
      }
    }
  }

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
        case Throw(e) => Future.value(JsonResponseBuilder.badRequest(e.getMessage))
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
    val (metatdataParams, artworkParams, assetParams) = multipartFormParams(request, TrackAssetDataUpdateRequest)
    request.method = Method.Put

    (artworkParams, metatdataParams, assetParams) match {
      case (None, None, None) => generateBadResponse
      case _ =>
        val response =
          trackUpdateService.updateTrack(
            artworkParams,
            assetParams,
            metatdataParams,
            urn,
            session
          )
        handleTrackRepresentationResponseFromService(response)
    }
  }

  private def updateTrackFromJsonRequest(request: HandlerRequest, session: UserSession, urn: Urn): Future[Response] = {
    val metadataUpdateParams = Json.parse(request.contentString).asOpt[TrackMetadataUpdateRequest]
    metadataUpdateParams match {
      case Some(_) =>
        val response = trackUpdateService.updateTrack(None, None, metadataUpdateParams, urn, session)
        handleTrackRepresentationResponseFromService(response)
      case _ => generateBadResponse
    }
  }

  private def updateTrackFromUrlEncodedRequest(
      request: HandlerRequest,
      session: UserSession,
      urn: Urn
  ): Future[Response] = {
    val (metadataParams, assetDataParams) = urlEncodedRequestParams(request, TrackAssetDataUpdateRequest)
    (metadataParams, assetDataParams) match {
      case (None, None) => generateBadResponse
      case _ =>
        val response =
          trackUpdateService.updateTrack(
            None,
            assetDataParams,
            metadataParams,
            urn,
            session
          )
        handleTrackRepresentationResponseFromService(response)
    }
  }

  def handleCreate(request: HandlerRequest): Future[Response] = {
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
    val (metadataParams, artworkParams, assetParams) = multipartFormParams(request, TrackAssetDataCreateRequest)
    assetParams match {
      case None => unprocessableEntityResponse
      case Some(asset) =>
        val createdTrack = trackUpdateService.createTrack(
          asset,
          artworkParams,
          metadataParams,
          session
        )
        handleCreateTrackResponseFromService(createdTrack)

    }
  }

  private def createTrackFromUrlEncodedRequest(
      request: HandlerRequest,
      session: UserSession
  ): Future[Response] = {
    val (metadataParams, assetDataParams) = urlEncodedRequestParams(request, TrackAssetDataCreateRequest)
    assetDataParams match {
      case None => unprocessableEntityResponse
      case Some(asset) =>
        val createdTrack = trackUpdateService.createTrack(
          asset,
          None,
          metadataParams,
          session
        )
        handleCreateTrackResponseFromService(createdTrack)
    }
  }

  private def multipartFormParams[T](
      request: HandlerRequest,
      assetParamsExtractor: TrackAssetRequestParams[T]
  ): (Option[TrackMetadataUpdateRequest], Option[TrackArtworkUpdateRequest], Option[T]) = {
    val metadataUpdateParams = paramsParser.parse(request)
    val albumArtworkUpdate = paramsParser.parseFilesFromRequest(request, "track[artwork_data]")
    val artworkDataUpdates = albumArtworkUpdate.map(TrackArtworkUpdateRequest)

    val extractedParams = metadataUpdateParams.map(extractTrackFieldsFromParams)
    val metadataUpdates = extractedParams.map(TrackMetadataUpdateRequest.fromForm).getOrElse(None)
    val assetDataUpdates = extractedParams.map(assetParamsExtractor.fromForm).getOrElse(None)
    (metadataUpdates, artworkDataUpdates, assetDataUpdates)
  }

  private def urlEncodedRequestParams[T](
      request: HandlerRequest,
      extractor: TrackAssetRequestParams[T]
  ): (Option[TrackMetadataUpdateRequest], Option[T]) = {
    val metadataUpdateParams = request.params
    val extractedParams = extractTrackFieldsFromParams(metadataUpdateParams)
    val assetDataUpdates = extractor.fromForm(extractedParams)
    val metadataUpdates = TrackMetadataUpdateRequest.fromForm(extractedParams)
    (metadataUpdates, assetDataUpdates)
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

  private def unprocessableEntityResponse: Future[Response] = {
    val body = """{"errors":[{"error_message":"Require uid and original_filename parameters."}]}"""
    Future.value(JsonResponseBuilder(status = Status.UnprocessableEntity, body = body).build)
  }
}
