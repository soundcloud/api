package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.client.playlists.PlaylistDeletionClient
import com.soundcloud.publicApiStrangler.handler.support.error.UnhandledOutcomeException
import com.soundcloud.publicApiStrangler.service.PlaylistsService
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.playlists.representation.Playlist
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.Json

class PlaylistsHandler(
    userAuthentication: UserAuthentication,
    playlistDeletionClient: PlaylistDeletionClient,
    playlistsService: PlaylistsService,
    mothershipDispatcher: DispatchToMothershipHandler,
    telemetry: Telemetry
) {
  private val usagePermalinkFetchPlaylistsCounter =
    telemetry.counter(
      "usage_permalink_fetching_playlist",
      "Count of total number of usage of permalink to fetch playlists",
      "client_id"
    )

  def handleDelete(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      playlistDeletionClient.deletePlaylist(session, getPlaylistUrn(request)).map {
        case Good(status) =>
          JsonResponseBuilder(status = status, body = Json.stringify(Json.obj("status" -> statusDescription(status)))).build
        case Bad(_) => ResponseBuilder.internalServerError()
      }
    }
  }

  def handleGet(request: HandlerRequest): Future[Response] = {
    val IdParamPattern = "(\\d+)".r
    val playlistUrn = request.routeParams("id")

    userAuthentication.withUserSession(request) { session =>
      val clientAppId = Option(session.getAgent).map(_.identifier).getOrElse("unknown")
      playlistUrn match {
        case IdParamPattern(_) => None
        case _ =>
          usagePermalinkFetchPlaylistsCounter.labels(clientAppId).inc()
      }
      mothershipDispatcher.dispatch(request)
    }
  }

  def handleFetchPlaylist(request: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      val playlistUrn = getPlaylistUrn(request)
      val candidateSecretToken = request.params.get("secret_token")
      val hasLinkedPartitioning = request.params.get("linked_partitioning")
      val pagination =
        hasLinkedPartitioning.map(_ => OffsetBasedPagination.build(request, Seq("linked_partitioning")))

      fetchPlaylist(session, playlistUrn, candidateSecretToken, pagination).map {
        case Good(playlist) => JsonResponseBuilder.ok(body = Json.stringify(Json.toJson(playlist)))
        case Bad(NotFound(_)) => JsonResponseBuilder.notFound(generateErrorBody("not found"))
        case _ => throw new UnhandledOutcomeException
      }
    }
  }

  private def fetchPlaylist(
      session: UserSession,
      playlistUrn: Urn,
      candidateSecretToken: Option[String],
      pagination: Option[OffsetBasedPagination]
  ): Future[Outcome[Playlist]] = {
    playlistsService
      .fetchPlaylist(session, playlistUrn, candidateSecretToken, pagination)
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
