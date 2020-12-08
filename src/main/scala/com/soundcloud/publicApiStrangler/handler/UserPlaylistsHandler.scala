package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.handler.representation.collection.CollectionResponse
import com.soundcloud.publicApiStrangler.handler.support.error.UnhandledOutcomeException
import com.soundcloud.publicApiStrangler.service.UserPlaylistsService
import com.soundcloud.publicApiStrangler.service.pagination.{CursorBasedPagination, OffsetBasedPagination}
import com.soundcloud.publicApiStrangler.support.PlaylistUrnUtil.getPlaylistUrn
import com.soundcloud.publicApiStrangler.support.UserUrnUtil.getUserUrn
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, Return, Throw, Try}
import play.api.libs.json.Json

class UserPlaylistsHandler(
    userAuthentication: UserAuthentication,
    userPlaylistsService: UserPlaylistsService
) {

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

    Try(getUserUrn(userId)) match {
      case Return(userUrn) =>
        Try(getPlaylistUrn(req)) match {
          case Return(playlistUrn) =>
            userPlaylistsService
              .userPlaylist(session, playlistUrn, secretToken, pagination, userUrn.identifier)
              .map {
                case Good(playlist) =>
                  generateResponse(Status.Ok, Json.stringify(Json.toJson(playlist)))
                case Bad(NotFound(_)) => JsonResponseBuilder.notFound(notFoundErrorString)
                case _ => throw new UnhandledOutcomeException
              }
          case Throw(e) => Future.value(JsonResponseBuilder.badRequest(e.getMessage))
        }
      case Throw(e) => Future.value(JsonResponseBuilder.badRequest(e.getMessage))
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

    Try(getUserUrn(userId)) match {
      case Return(urn) =>
        val playlistsCollection =
          userPlaylistsService
            .userPlaylists(session, urn, pagination)
            .map(Good(_))
        CollectionResponse.handleCollectionResponse(playlistsCollection, hasLinkedPartitioning)
      case Throw(e) => Future.value(JsonResponseBuilder.badRequest(e.getMessage))
    }
  }

  private def generateResponse(status: Status, rawContent: String): Response = {
    JsonResponseBuilder(status = status, body = rawContent).build
  }

  private val notFoundErrorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""
}
