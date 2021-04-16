package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.client.playlists.PlaylistDeletionClient
import com.soundcloud.publicApiStrangler.handler.support.error.UnhandledOutcomeException
import com.soundcloud.publicApiStrangler.handler.support.requestParser.AccessParamsExtractor
import com.soundcloud.publicApiStrangler.service.PlaylistsService
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.support.ErrorResponse
import com.soundcloud.publicApiStrangler.support.PlaylistUrnUtil.getPlaylistUrn
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, Return, Throw, Try}
import play.api.libs.json.Json

class PlaylistsHandler(
    userAuthentication: UserAuthentication,
    playlistDeletionClient: PlaylistDeletionClient,
    playlistsService: PlaylistsService
) {
  def handleDelete(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      Try(getPlaylistUrn(request)) match {
        case Return(urn) =>
          playlistDeletionClient.deletePlaylist(session, urn).map {
            case Good(status) =>
              JsonResponseBuilder(
                status = status,
                body = Json.stringify(Json.obj("status" -> statusDescription(status)))
              ).build
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
      val pagination =
        hasLinkedPartitioning.map(_ => OffsetBasedPagination.build(request, Seq("linked_partitioning")))
      val access = AccessParamsExtractor.unapply(request.params)
      Try(getPlaylistUrn(request)) match {
        case Return(urn) =>
          playlistsService.fetchPlaylist(session, urn, candidateSecretToken, access, pagination).map {
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
      val pagination =
        hasLinkedPartitioning.map(_ => OffsetBasedPagination.build(request, Seq("linked_partitioning")))
      val access = AccessParamsExtractor.unapply(request.params)
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

  private def statusDescription(status: Status): String = {
    s"${status.code} - ${com.twitter.finagle.http.Status(status.code).reason}"
  }

}
