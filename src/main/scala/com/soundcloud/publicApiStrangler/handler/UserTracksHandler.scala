package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.support.TrackUrnUtil.getTrackUrn
import com.soundcloud.publicApiStrangler.handler.representation.collection.CollectionResponse
import com.soundcloud.publicApiStrangler.handler.support.error.UnhandledOutcomeException
import com.soundcloud.publicApiStrangler.service.UserTracksService
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackRepresentation
import com.soundcloud.publicApiStrangler.support.UserUrnUtil.getUserUrn
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, Return, Throw, Try}
import play.api.libs.json.Json

class UserTracksHandler(
    userAuthentication: UserAuthentication,
    userTracksService: UserTracksService
) {

  def getTrackByMe(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, userUrn) =>
      Try(getTrackUrn(req)) match {
        case Return(urn) =>
          val userId = userUrn.identifier
          getTrack(userId, session, urn, req)
        case Throw(e) => Future.value(JsonResponseBuilder.badRequest(e.getMessage))
      }
    }
  }

  def getUserTracks(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      val userId = req.routeParams("userId")
      performGetTracks(req, session, userId)
    }
  }

  def getMeTracks(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, userUrn) =>
      performGetTracks(req, session, userUrn.identifier)
    }
  }

  private def getTrack(
      userId: String,
      session: UserSession,
      urn: Urn,
      req: HandlerRequest
  ): Future[Response] = {
    val secretToken = req.params.get("secret_token")

    performGetTrackByUser(userId, session, urn, secretToken)
      .map {
        case Good(trackRep) =>
          generateResponse(Status.Ok, Json.stringify(Json.toJson(trackRep)))
        case Bad(NotFound(_)) => JsonResponseBuilder.notFound(notFoundErrorString)
        case _ => throw new UnhandledOutcomeException
      }
  }

  private def performGetTrackByUser(
      userId: String,
      session: UserSession,
      urn: Urn,
      secretToken: Option[String]
  ): Future[Outcome[TrackRepresentation]] = {
    userTracksService
      .userTrack(
        urn,
        session,
        userId,
        secretToken
      )
      .map {
        case Some(track) => Good(track)
        case None => NotFound().bad
      }
  }

  private def performGetTracks(req: HandlerRequest, session: UserSession, userId: String): Future[Response] = {
    val hasLinkedPartitioning = req.params.contains("linked_partitioning")
    val pagination = CursorBasedPagination.build(req, Seq("linked_partitioning"))

    Try(getUserUrn(userId)) match {
      case Return(urn) =>
        val tracksCollection = userTracksService
          .userTracks(session, urn, pagination)
          .map(Good(_))
        CollectionResponse.handleCollectionResponse(tracksCollection, hasLinkedPartitioning)
      case Throw(e) => Future.value(JsonResponseBuilder.badRequest(e.getMessage))
    }
  }

  private def generateResponse(status: Status, rawContent: String): Response = {
    JsonResponseBuilder(status = status, body = rawContent).build
  }

  private val notFoundErrorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""
}
