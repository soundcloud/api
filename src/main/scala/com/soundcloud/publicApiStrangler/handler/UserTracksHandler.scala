package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.handler.representation.collection.CollectionResponse
import com.soundcloud.publicApiStrangler.handler.support.requestParser.{AccessParams, AccessParamsExtractor}
import com.soundcloud.publicApiStrangler.service.UserTracksService
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.support.ErrorResponse
import com.soundcloud.publicApiStrangler.support.UserUrnUtil.getUserUrn
import com.twitter.finagle.http.Response
import com.twitter.util.{Future, Return, Throw, Try}

class UserTracksHandler(
    userAuthentication: UserAuthentication,
    userTracksService: UserTracksService
) {

  def getUserTracks(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      val userId = req.routeParams("userId")
      val access = AccessParamsExtractor.unapply(req.params)

      performGetTracks(req, session, userId, access)
    }
  }

  def getMeTracks(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, userUrn) =>
      performGetTracks(req, session, userUrn.identifier, AccessParams.explicitAccess)
    }
  }

  private def performGetTracks(
      req: HandlerRequest,
      session: UserSession,
      userId: String,
      access: AccessParams
  ): Future[Response] = {
    val hasLinkedPartitioning = req.params.contains("linked_partitioning")
    val pagination = CursorBasedPagination.build(req, Seq("linked_partitioning"))

    Try(getUserUrn(userId)) match {
      case Return(urn) =>
        val tracksCollection = userTracksService
          .userTracks(session, urn, access, pagination)
          .map(Good(_))
        CollectionResponse.handleCollectionResponse(tracksCollection, hasLinkedPartitioning)
      case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
    }
  }
}
