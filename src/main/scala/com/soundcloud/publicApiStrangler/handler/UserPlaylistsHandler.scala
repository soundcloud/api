package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.handler.representation.collection.CollectionResponse
import com.soundcloud.publicApiStrangler.handler.support.error.UnhandledOutcomeException
import com.soundcloud.publicApiStrangler.service.UserPlaylistsService
import com.soundcloud.publicApiStrangler.service.pagination.{CursorBasedPagination, OffsetBasedPagination}
import com.soundcloud.publicApiStrangler.support.PlaylistUrnUtil.playlistUrn
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, Return, Try}
import play.api.libs.json.Json

class UserPlaylistsHandler(
    userAuthentication: UserAuthentication,
    userPlaylistsService: UserPlaylistsService
) {

  private val numericRegexp = """\d+""".r

  def getUserPlaylist(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      val userId = req.routeParams("userId")
      performGetPlaylist(req, session, userId)
    }
  }

  def getMePlaylist(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, userUrn) =>
      performGetPlaylist(req, session, userUrn.identifier)
    }
  }

  private def performGetPlaylist(req: HandlerRequest, session: UserSession, userId: String): Future[Response] = {
    val hasLinkedPartitioning = req.params.get("linked_partitioning")
    val pagination =
      hasLinkedPartitioning.map(_ => OffsetBasedPagination.build(req, Seq("linked_partitioning")))
    val secretToken = req.params.get("secret_token")

    Try(Urn("soundcloud", "users", userId)) match {
      case Return(urn @ Urn(_, _, numericRegexp())) =>
        userPlaylistsService
          .userPlaylist(session, playlistUrn(req), secretToken, pagination, urn.identifier)
          .map {
            case Good(playlist) =>
              generateResponse(Status.Ok, Json.stringify(Json.toJson(playlist)))
            case Bad(NotFound(_)) => JsonResponseBuilder.notFound(notFoundErrorString)
            case _ => throw new UnhandledOutcomeException
          }
      case _ => Future.value(JsonResponseBuilder.notFound(notFoundErrorString))
    }
  }

  def getUserPlaylists(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      val userId = req.routeParams("userId")
      performGetPlaylists(req, session, userId)
    }
  }

  def getMePlaylists(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, userUrn) =>
      performGetPlaylists(req, session, userUrn.identifier)
    }
  }

  private def performGetPlaylists(req: HandlerRequest, session: UserSession, userId: String): Future[Response] = {
    val hasLinkedPartitioning = req.params.contains("linked_partitioning")
    val pagination = CursorBasedPagination.build(req, Seq("linked_partitioning"))

    Try(Urn("soundcloud", "users", userId)) match {
      case Return(urn @ Urn(_, _, numericRegexp())) =>
        val playlistsCollection =
          userPlaylistsService
            .userPlaylists(session, urn, pagination)
            .map(Good(_))
        CollectionResponse.handleCollectionResponse(playlistsCollection, hasLinkedPartitioning)
      case _ => Future.value(JsonResponseBuilder.notFound(notFoundErrorString))
    }
  }

  private def generateResponse(status: Status, rawContent: String): Response = {
    JsonResponseBuilder(status = status, body = rawContent).build
  }

  private val notFoundErrorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""
}
