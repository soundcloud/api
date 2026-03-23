package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.outcome.{Bad, Good}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.client.reposts.RepostsClient._
import com.soundcloud.apipublic.handler.representation.collection.CollectionResponse
import com.soundcloud.apipublic.handler.support.requestParser.{AccessParams, AccessParamsExtractor}
import com.soundcloud.apipublic.service.RepostsService
import com.soundcloud.apipublic.service.pagination.CursorBasedPagination
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.support.ErrorResponse
import com.soundcloud.apipublic.support.PlaylistUrnUtil.getPlaylistUrn
import com.soundcloud.apipublic.support.TrackUrnUtil.getTrackUrn
import com.soundcloud.apipublic.support.UserUrnUtil.getUserUrn
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, Return, Try}

class RepostsHandler(
    userAuthentication: UserAuthentication,
    repostsService: RepostsService,
    baseUrl: String
) {

  def createTracksRepost(request: HandlerRequest): Future[Response] =
    execute(request, getTrackUrn, repostsService.createTracksRepost)

  def deleteTracksRepost(request: HandlerRequest): Future[Response] =
    execute(request, getTrackUrn, repostsService.deleteTracksRepost)

  def getTracksReposters(request: HandlerRequest): Future[Response] =
    userAuthentication.withUserSession(request) { session =>
      val pagination = CursorBasedPagination.build(baseUrl, request)

      Try(getTrackUrn(request)) match {
        case Return(urn) =>
          repostsService
            .getTrackReposters(session, urn, pagination)
            .map {
              case Good(userCollection) => JsonResponseBuilder.ok(Collection.getRepresentation(userCollection, true))
              case Bad(com.soundcloud.jvmkit.module.outcome.NotFound(_)) => ErrorResponse.notFound()
              case Bad(_) => ErrorResponse(Status.InternalServerError)
            }
        case _ => Future.value(ErrorResponse.badRequest())
      }
    }

  def createPlaylistsRepost(request: HandlerRequest): Future[Response] =
    execute(request, getPlaylistUrn, repostsService.createPlaylistsRepost)

  def deletePlaylistsRepost(request: HandlerRequest): Future[Response] =
    execute(request, getPlaylistUrn, repostsService.deletePlaylistsRepost)

  def getMeTrackReposts(request: HandlerRequest): Future[Response] =
    userAuthentication.withLoggedInUser(request) { (session, userUrn) =>
      val access = AccessParamsExtractor.unapply(request.params, predefinedAccess = AccessParams.explicitAccess)
      performGetTrackReposts(request, session, userUrn, access)
    }

  def getUserTrackReposts(request: HandlerRequest): Future[Response] =
    userAuthentication.withUserSession(request) { session =>
      val userId = request.routeParams("userId")
      val access = AccessParamsExtractor.unapply(request.params)
      getUserUrn(userId) match {
        case userUrn: Urn => performGetTrackReposts(request, session, userUrn, access)
        case _ => Future.value(ErrorResponse.badRequest())
      }
    }

  private def performGetTrackReposts(
      request: HandlerRequest,
      session: UserSession,
      userUrn: Urn,
      access: AccessParams
  ): Future[Response] = {
    val hasLinkedPartitioning = request.params.contains("linked_partitioning")
    val pagination = CursorBasedPagination.build(baseUrl, request, Seq("linked_partitioning", "access"))
    val outcome = repostsService.getTrackReposts(session, userUrn, access, pagination)
    CollectionResponse.handleCollectionResponse(outcome, hasLinkedPartitioning, isPrivate = true)
  }

  def getMePlaylistReposts(request: HandlerRequest): Future[Response] =
    userAuthentication.withLoggedInUser(request) { (session, userUrn) =>
      performGetPlaylistReposts(request, session, userUrn)
    }

  def getUserPlaylistReposts(request: HandlerRequest): Future[Response] =
    userAuthentication.withUserSession(request) { session =>
      val userId = request.routeParams("userId")
      getUserUrn(userId) match {
        case userUrn: Urn => performGetPlaylistReposts(request, session, userUrn)
        case _ => Future.value(ErrorResponse.badRequest())
      }
    }

  private def performGetPlaylistReposts(
      request: HandlerRequest,
      session: UserSession,
      userUrn: Urn
  ): Future[Response] = {
    val hasLinkedPartitioning = request.params.contains("linked_partitioning")
    val pagination = CursorBasedPagination.build(baseUrl, request, Seq("linked_partitioning"))
    val outcome = repostsService.getPlaylistReposts(session, userUrn, pagination)
    CollectionResponse.handleCollectionResponse(outcome, hasLinkedPartitioning, isPrivate = true)
  }

  def getPlaylistsReposters(request: HandlerRequest): Future[Response] =
    userAuthentication.withUserSession(request) { session =>
      val pagination = CursorBasedPagination.build(baseUrl, request)

      Try(getPlaylistUrn(request)) match {
        case Return(urn) =>
          repostsService
            .getPlaylistReposters(session, urn, pagination)
            .map(userCollection => {
              JsonResponseBuilder.ok(Collection.getRepresentation(userCollection, true))
            })
        case _ => Future.value(ErrorResponse.badRequest())
      }
    }

  private def execute(
      request: HandlerRequest,
      extractUrn: HandlerRequest => Urn,
      serviceCall: (UserSession, Urn) => Future[Result]
  ): Future[Response] =
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      Try(extractUrn(request)) match {
        case Return(urn) => serviceCall(session, urn).map(renderResult)
        case _ => Future.value(ErrorResponse.badRequest())
      }
    }

  private def renderResult(result: Result): Response = result match {
    case Created => ResponseBuilder.created()
    case Deleted => ResponseBuilder.ok()
    case AlreadyExists => ResponseBuilder.ok()
    case NotFound => ErrorResponse.notFound()
    case Forbidden => ErrorResponse.forbidden()
    case SpamBlocked => ErrorResponse(Status.TooManyRequests)
    case Failed => ErrorResponse(Status.InternalServerError)
  }
}
