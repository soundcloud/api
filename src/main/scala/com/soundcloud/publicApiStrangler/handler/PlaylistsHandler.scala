package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.client.playlists.PlaylistDeletionClient
import com.soundcloud.publicApiStrangler.handler.support.error.UnhandledOutcomeException
import com.soundcloud.publicApiStrangler.service.PlaylistsService
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.playlists.representation.Collection
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.Json

class PlaylistsHandler(
    userAuthentication: UserAuthentication,
    playlistDeletionClient: PlaylistDeletionClient,
    playlistsService: PlaylistsService
) {
  def handleDelete(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      playlistDeletionClient.deletePlaylist(session, getPlaylistUrn(request)).map {
        case Good(status) =>
          JsonResponseBuilder(status = status, body = Json.stringify(Json.obj("status" -> statusDescription(status)))).build
        case Bad(_) => ResponseBuilder.internalServerError()
      }
    }
  }

  def handleFetchPlaylist(request: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      val playlistUrn = getPlaylistUrn(request)
      val candidateSecretToken = request.params.get("secret_token")
      val hasLinkedPartitioning = request.params.get("linked_partitioning")
      val pagination =
        hasLinkedPartitioning.map(_ => OffsetBasedPagination.build(request, Seq("linked_partitioning")))

      playlistsService.fetchPlaylist(session, playlistUrn, candidateSecretToken, pagination).map {
        case Good(playlist) => JsonResponseBuilder.ok(body = Json.stringify(Json.toJson(playlist)))
        case Bad(NotFound(_)) => JsonResponseBuilder.notFound(generateErrorBody("not found"))
        case _ => throw new UnhandledOutcomeException
      }
    }
  }

  def handleFetchPlaylistTracks(request: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      val playlistUrn = getPlaylistUrn(request)
      val candidateSecretToken = request.params.get("secret_token")
      val hasLinkedPartitioning = request.params.get("linked_partitioning")
      val pagination =
        hasLinkedPartitioning.map(_ => OffsetBasedPagination.build(request, Seq("linked_partitioning")))

      playlistsService.fetchPlaylistTracks(session, playlistUrn, candidateSecretToken, pagination).map {
        case Good(tracks) =>
          JsonResponseBuilder.ok(body = Collection.getRepresentation(tracks, hasLinkedPartitioning.isDefined))
        case Bad(NotFound(_)) => JsonResponseBuilder.notFound(generateErrorBody("not found"))
        case _ => throw new UnhandledOutcomeException
      }

    }
  }

  private def getPlaylistUrn(request: HandlerRequest): Urn = {
    val IdParamPattern = "(\\d+)".r
    val playlistUrn = request.routeParams("id")
    playlistUrn match {
      case IdParamPattern(id) => Urn("soundcloud", "playlists", id)
      case _ => throw new IllegalArgumentException(s"Invalid track id: ${playlistUrn}")
    }
  }

  private def statusDescription(status: Status): String = {
    s"${status.code} - ${com.twitter.finagle.http.Status(status.code).reason}"
  }

  private def generateErrorBody(message: String): String =
    Json.stringify(Json.obj("error" -> message))
}
