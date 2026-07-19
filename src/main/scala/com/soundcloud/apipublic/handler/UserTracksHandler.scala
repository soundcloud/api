package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.handler.representation.collection.CollectionResponse
import com.soundcloud.apipublic.handler.support.requestParser.{AccessParams, AccessParamsExtractor}
import com.soundcloud.apipublic.service.UserTracksService
import com.soundcloud.apipublic.service.pagination.CursorBasedPagination
import com.soundcloud.apipublic.support.ErrorResponse
import com.soundcloud.apipublic.support.UserUrnUtil.getUserUrn
import com.twitter.finagle.http.Response
import com.twitter.util.{Future, Return, Throw, Try}
import proto.soundcloud.profiles.api.ChronoDirection

class UserTracksHandler(
    userAuthentication: UserAuthentication,
    userTracksService: UserTracksService,
    baseUrl: String
) {

  def getUserTracks(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      val userId = req.routeParams("userId")
      val access = AccessParamsExtractor.unapply(req.params)

      performGetTracks(req, session, userId, access, false)
    }
  }

  def getMeTracks(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, userUrn) =>
      performGetTracks(req, session, userUrn.identifier, AccessParams.explicitAccess, true)
    }
  }

  private def performGetTracks(
      req: HandlerRequest,
      session: UserSession,
      userId: String,
      access: AccessParams,
      isPrivate: Boolean
  ): Future[Response] = {
    val hasLinkedPartitioning = req.params.contains("linked_partitioning")
    val pagination = CursorBasedPagination.build(
      baseUrl,
      req,
      Seq("linked_partitioning", "access", "sort", "direction", "order")
    )
    val direction = chronoDirectionParam(req)

    Try(getUserUrn(userId)) match {
      case Return(urn) =>
        val tracksCollection = userTracksService
          .userTracks(session, urn, access, pagination, direction)
          .map(Good(_))
        CollectionResponse.handleCollectionResponse(tracksCollection, hasLinkedPartitioning, isPrivate)
      case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
    }
  }

  private def chronoDirectionParam(req: HandlerRequest): ChronoDirection =
    req.params
      .get("sort")
      .orElse(req.params.get("direction"))
      .orElse(req.params.get("order"))
      .flatMap {
        case "asc" => Some(ChronoDirection.asc)
        case "desc" => Some(ChronoDirection.desc)
        case _ => None
      }
      .getOrElse(ChronoDirection.desc)
}
