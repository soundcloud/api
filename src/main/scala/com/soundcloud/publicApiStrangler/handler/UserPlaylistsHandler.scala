package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.outcome.Good
import com.soundcloud.publicApiStrangler.handler.representation.collection.CollectionResponse
import com.soundcloud.publicApiStrangler.service.UserPlaylistsService
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.twitter.finagle.http.Response
import com.twitter.util.{Future, Return, Try}

class UserPlaylistsHandler(
    userAuthentication: UserAuthentication,
    userPlaylistsService: UserPlaylistsService
) {

  private val numericRegexp = """\d+""".r

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

  private val notFoundErrorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""
}
