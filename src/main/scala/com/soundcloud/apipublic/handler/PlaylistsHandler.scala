package com.soundcloud.apipublic.handler

import cats.implicits._
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.periskop.client.Severity
import com.soundcloud.apipublic.client.playlists.PlaylistDeletionClient
import com.soundcloud.apipublic.handler.support.error.UnhandledOutcomeException
import com.soundcloud.apipublic.handler.support.requestParser.{AccessParamsExtractor, PlaylistFormParamsExtractor}
import com.soundcloud.apipublic.service.PlaylistsService
import com.soundcloud.apipublic.service.pagination.OffsetBasedPagination
import com.soundcloud.apipublic.service.playlists.representation.PlaylistCreateOrUpdate
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.support.PlaylistUrnUtil.getPlaylistUrn
import com.soundcloud.apipublic.support.{ErrorResponse, PlaylistUrnUtil}
import com.twitter.finagle.http._
import com.twitter.util.{Future, Return, Throw, Try}
import play.api.libs.json.{JsObject, Json}

class PlaylistsHandler(
    userAuthentication: UserAuthentication,
    playlistDeletionClient: PlaylistDeletionClient,
    playlistsService: PlaylistsService,
    baseUrl: String,
    exceptionCollector: ExceptionCollector,
    playlistFormParamsExtractor: PlaylistFormParamsExtractor = new PlaylistFormParamsExtractor
) {

  def handleCreate(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      val result = for {
        artworkRequest <- playlistFormParamsExtractor.artworkDataFromRequest(request).outcomeF
        parsedPlaylist <- playlistFromRequest(request).outcomeF
        createdPlaylist <- playlistsService.createPlaylist(session, parsedPlaylist, artworkRequest)
      } yield createdPlaylist
      result.value.map {
        case Good(playlist) =>
          JsonResponseBuilder(
            status = Status.Created,
            headers = Map("location" -> playlistLocation(playlist.id.toString)),
            body = Json.stringify(Json.toJson(playlist))
          ).build
        case Bad(NotValid(msg)) => {
          exceptionCollector.addMessage(
            "unprocessable-playlist-create",
            request.contentString,
            Severity.Info,
            true
          )
          ErrorResponse(Status.UnprocessableEntity, msg.mkString(","))
        }
        case Bad(NotAuthorized(_)) => ErrorResponse.forbidden()
        case _ => ErrorResponse(Status.InternalServerError)
      }
    }
  }

  def handleUpdate(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      val urn = PlaylistUrnUtil.getPlaylistUrn(request)
      val result = for {
        artworkRequest <- playlistFormParamsExtractor.artworkDataFromRequest(request).outcomeF
        playlistUpdate <- playlistFromRequest(request).outcomeF
        updatedPlaylist <- playlistsService.updatePlaylist(session, urn, playlistUpdate, artworkRequest)
      } yield updatedPlaylist

      result.value.map {
        case Good(playlist) => JsonResponseBuilder.ok(Json.stringify(Json.toJson(playlist)))
        case Bad(NotValid(msg)) => {
          exceptionCollector.addMessage(
            "unprocessable-playlist-update",
            request.contentString,
            Severity.Info,
            true
          )
          ErrorResponse(Status.UnprocessableEntity, msg.mkString(","))
        }
        case Bad(NotFound(_)) => ErrorResponse.notFound()
        case Bad(NotAuthorized(_)) => ErrorResponse.forbidden()
        case _ => ErrorResponse(Status.InternalServerError)
      }
    }
  }

  def handleDelete(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      Try(getPlaylistUrn(request)) match {
        case Return(urn) =>
          playlistDeletionClient.deletePlaylist(session, urn).map {
            case Good(_) =>
              JsonResponseBuilder.ok(body = Json.stringify(Json.obj("status" -> statusDescription(Status.Ok))))
            case Bad(NotFound(_)) => ErrorResponse.notFound()
            case Bad(_) => ErrorResponse(Status.InternalServerError)
          }
        case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
      }
    }
  }

  def handleFetchPlaylist(request: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      val candidateSecretToken = request.params.get("secret_token")
      val hasLinkedPartitioning = request.params.get("linked_partitioning")
      val showTracks = request.params.getBoolean("show_tracks")
      val access = AccessParamsExtractor.unapply(request.params)
      val pagination =
        hasLinkedPartitioning.map(_ =>
          OffsetBasedPagination.build(request, Seq("linked_partitioning", "access", "show_tracks", "secret_token"))
        )

      Try(getPlaylistUrn(request)) match {
        case Return(urn) =>
          playlistsService.fetchPlaylist(session, urn, candidateSecretToken, access, pagination, showTracks).map {
            case Good(playlist) => JsonResponseBuilder.ok(body = Json.stringify(Json.toJson(playlist)))
            case Bad(NotFound(_)) => ErrorResponse.notFound()
            case _ => throw new UnhandledOutcomeException
          }
        case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
      }
    }
  }

  def handleFetchPlaylistTracks(request: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      val candidateSecretToken = request.params.get("secret_token")
      val hasLinkedPartitioning = request.params.get("linked_partitioning")
      val access = AccessParamsExtractor.unapply(request.params)
      val pagination =
        hasLinkedPartitioning.map(_ =>
          OffsetBasedPagination.build(request, Seq("linked_partitioning", "access", "secret_token"))
        )

      Try(getPlaylistUrn(request)) match {
        case Return(urn) =>
          playlistsService.fetchPlaylistTracks(session, urn, candidateSecretToken, access, pagination).map {
            case Good(tracks) =>
              JsonResponseBuilder.ok(body = Collection.getRepresentation(tracks, hasLinkedPartitioning.isDefined))
            case Bad(NotFound(_)) => ErrorResponse.notFound()
            case _ => throw new UnhandledOutcomeException
          }
        case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
      }
    }
  }

  private def playlistFromRequest(request: HandlerRequest) = {
    request.mediaType match {
      case Some(MediaType.MultipartForm) | Some(MediaType.WwwForm) =>
        playlistFormParamsExtractor.playlistFromFormRequest(request)
      case Some(MediaType.Json) => tryParsePlaylistWriteRequestOutcome(request)
      case _ => NotValid("").bad
    }
  }

  private def playlistLocation(urnIdentifier: String): String = {
    s"$baseUrl/playlists/$urnIdentifier"
  }

  private def tryParsePlaylistWriteRequestOutcome(request: Request): Outcome[PlaylistCreateOrUpdate] = {
    val tryJson = Try(Json.parse(request.getContentString()))

    tryJson
      .flatMap { json =>
        Try((json \ "playlist").asOpt[JsObject].fold(json.as[PlaylistCreateOrUpdate])(_.as[PlaylistCreateOrUpdate]))

      }
      .outcome
      .leftMap(_ => NotValid("Could not parse JSON request body."))
  }

  private def statusDescription(status: Status): String = {
    s"${status.code} - ${com.twitter.finagle.http.Status(status.code).reason}"
  }

}
