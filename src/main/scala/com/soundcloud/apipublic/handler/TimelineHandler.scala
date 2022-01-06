package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.session.LoggedInUserSession
import com.soundcloud.apipublic.handler.support.error.UnhandledOutcomeException
import com.soundcloud.apipublic.handler.support.requestParser.{AccessParams, AccessParamsExtractor}
import com.soundcloud.apipublic.service.TimelineService
import com.soundcloud.apipublic.service.pagination.CursorBasedPagination
import com.soundcloud.apipublic.service.timeline.{Timeline => SimpleTimeline}
import com.soundcloud.apipublic.service.trackrepresentation.TrackRepresentation
import com.soundcloud.apipublic.support.ErrorResponse
import com.twitter.finagle.http.Response
import com.twitter.util.Future
import play.api.libs.json.Json

/**
  * Note: the PAS API refers to 'activities' in the endpoint syntax, but internally we call the /stream endpoint from Timeline,
  * hence the naming of the handler methods
  */
class TimelineHandler(
    userAuthentication: UserAuthentication,
    timelineService: TimelineService
) {

  def renderPublicStream(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session: LoggedInUserSession, _) =>
      val pagination = CursorBasedPagination.build(request, Seq("linked_partitioning", "access"))

      val (cursor, reverseCursor) = extractCursor(pagination)
      val limit = pagination.pageSize
      val access = AccessParamsExtractor.unapply(request.params)

      performGetAllStreamItems(session, access, cursor, reverseCursor, limit, pagination)
        .map {
          case Good(timeline) => JsonResponseBuilder.ok(timeline.getRepresentation())
          case Bad(NotFound(_)) => ErrorResponse.notFound()
          case _ => throw new UnhandledOutcomeException
        }
    }
  }

  private def extractCursor(pagination: CursorBasedPagination): (Option[String], Boolean) =
    pagination.extraParams.get("uuid[to]") match {
      case Some(uuid) => (Some(uuid), true)
      case _ => (pagination.cursor, false)
    }

  def renderTrackStream(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session: LoggedInUserSession, _) =>
      val pagination = CursorBasedPagination.build(request, Seq("linked_partitioning", "access"))

      val (cursor, reverseCursor) = extractCursor(pagination)
      val limit = pagination.pageSize
      val access = AccessParamsExtractor.unapply(request.params)

      performGetTrackStreamItems(session, access, cursor, reverseCursor, limit, pagination)
        .map {
          case Good(timeline) => JsonResponseBuilder.ok(timeline.getRepresentation())
          case Bad(NotFound(_)) => ErrorResponse.notFound()
          case _ => throw new UnhandledOutcomeException
        }
    }
  }

  private def performGetAllStreamItems(
      session: LoggedInUserSession,
      access: AccessParams,
      cursor: Option[String],
      reverseCursor: Boolean,
      limit: Int,
      pagination: CursorBasedPagination
  ): Future[Outcome[SimpleTimeline]] = {
    timelineService
      .fetchTimelineForUser(session, access, cursor, reverseCursor, limit, pagination)
      .map {
        case timeline: SimpleTimeline => Good(timeline)
        case _ => NotFound().bad
      }
  }

  private def performGetTrackStreamItems(
      session: LoggedInUserSession,
      access: AccessParams,
      cursor: Option[String],
      reverseCursor: Boolean,
      limit: Int,
      pagination: CursorBasedPagination
  ): Future[Outcome[SimpleTimeline]] = {
    timelineService
      .fetchTimelineTracksForUser(session, access, cursor, reverseCursor, limit, pagination)
      .map {
        case timeline: SimpleTimeline => Good(timeline)
        case _ => NotFound().bad
      }
  }

  def renderFollowingTracks(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session: LoggedInUserSession, _) =>
      val pagination = CursorBasedPagination.build(request, Seq("linked_partitioning", "access"))

      val (cursor, reverseCursor) = extractCursor(pagination)
      val limit = pagination.pageSize
      val access = AccessParamsExtractor.unapply(request.params)

      performGetFollowingTrackActivities(session, access, cursor, reverseCursor, limit)
        .map {
          case Good(tracks) => JsonResponseBuilder.ok(Json.stringify(Json.toJson(tracks)))
          case Bad(NotFound(_)) => ErrorResponse.notFound()
          case _ => throw new UnhandledOutcomeException
        }
    }
  }

  private def performGetFollowingTrackActivities(
      session: LoggedInUserSession,
      access: AccessParams,
      cursor: Option[String],
      reverseCursor: Boolean,
      limit: Int
  ): Future[Outcome[List[TrackRepresentation]]] = {
    timelineService
      .fetchFollowingTracksForUser(session, access, cursor, reverseCursor, limit)
      .map {
        case tracks: List[TrackRepresentation] => Good(tracks)
        case _ => NotFound().bad
      }
  }
}
