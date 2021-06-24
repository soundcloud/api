package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.handler.representation.collection.CollectionResponse
import com.soundcloud.publicApiStrangler.handler.support.error.UnhandledOutcomeException
import com.soundcloud.publicApiStrangler.handler.support.requestParser.{AccessParams, AccessParamsExtractor}
import com.soundcloud.publicApiStrangler.service.UserPlaylistsService
import com.soundcloud.publicApiStrangler.service.pagination.{CursorBasedPagination, OffsetBasedPagination}
import com.soundcloud.publicApiStrangler.support.ErrorResponse
import com.soundcloud.publicApiStrangler.support.PlaylistUrnUtil.getPlaylistUrn
import com.soundcloud.publicApiStrangler.support.UserUrnUtil.getUserUrn
import com.twitter.finagle.http.Response
import com.twitter.util.{Future, Return, Throw, Try}
import play.api.libs.json.Json

class UserPlaylistsHandler(
    userAuthentication: UserAuthentication,
    userPlaylistsService: UserPlaylistsService
) {

  def getUserPlaylist(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      val userId = req.routeParams("userId")
      val hasLinkedPartitioning = req.params.get("linked_partitioning")
      val pagination = hasLinkedPartitioning.map(_ => OffsetBasedPagination.build(req, Seq("linked_partitioning")))
      val secretToken = req.params.get("secret_token")
      val access = AccessParamsExtractor.unapply(req.params)
      val showTracks = req.params.getBoolean("show_tracks")

      Try(getUserUrn(userId)) match {
        case Return(userUrn) =>
          Try(getPlaylistUrn(req)) match {
            case Return(playlistUrn) =>
              userPlaylistsService
                .userPlaylist(session, playlistUrn, secretToken, pagination, userUrn.identifier, access, showTracks)
                .map {
                  case Good(playlist) =>
                    JsonResponseBuilder.ok(Json.stringify(Json.toJson(playlist)))
                  case Bad(NotFound(_)) => ErrorResponse.notFound("404 - Not Found")
                  case _ => throw new UnhandledOutcomeException
                }
            case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
          }
        case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
      }
    }
  }

  def getUserPlaylists(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      val userId = req.routeParams("userId")
      val access = AccessParamsExtractor.unapply(req.params)

      performGetPlaylists(req, session, userId, access)
    }
  }

  def getMePlaylists(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, userUrn) =>
      performGetPlaylists(req, session, userUrn.identifier, AccessParams.explicitAccess)
    }
  }

  private def performGetPlaylists(
      req: HandlerRequest,
      session: UserSession,
      userId: String,
      access: AccessParams
  ): Future[Response] = {
    val hasLinkedPartitioning = req.params.contains("linked_partitioning")
    val pagination = CursorBasedPagination.build(req, Seq("linked_partitioning"))
    val showTracks = req.params.getBoolean("show_tracks")

    Try(getUserUrn(userId)) match {
      case Return(urn) =>
        val playlistsCollection =
          userPlaylistsService
            .userPlaylists(session, urn, access, pagination, showTracks)
            .map(Good(_))
        CollectionResponse.handleCollectionResponse(playlistsCollection, hasLinkedPartitioning)
      case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
    }
  }
}
