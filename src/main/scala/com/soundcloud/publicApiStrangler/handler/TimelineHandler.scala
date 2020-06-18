package com.soundcloud.publicApiStrangler.handler

import java.util.UUID

import com.soundcloud.bff.nextbff.UntypedJson
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.LoggedInUserSession
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.handler.support.UnhandledOutcomeException
import com.soundcloud.publicApiStrangler.mapper.timeline._
import com.soundcloud.publicApiStrangler.mapper.timeline.publicApi.ActivitiesWithOriginMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.representation.Timeline
import com.soundcloud.publicApiStrangler.service.TimelineService
import com.soundcloud.publicApiStrangler.service.pagination.{CursorBasedPagination, Pagination}
import com.soundcloud.publicApiStrangler.service.timeline.{Timeline => SimpleTimeline}
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackRepresentation
import com.soundcloud.publicApiStrangler.support._
import com.twitter.finagle.http.Response
import com.twitter.util.Future
import play.api.libs.json.Json

class TimelineHandler(
    userAuthentication: UserAuthentication,
    publicActivitiesMapper: ActivitiesWithOriginMapper,
    pagination: CursorPagination,
    timelineService: TimelineService
) {

  def renderPublicActivities(request: HandlerRequest): Future[Response] = {
    renderActivities(request, publicActivitiesMapper)
  }

  private def renderActivities(request: HandlerRequest, mapper: TimelineMapper): Future[Response] =
    userAuthentication.withLoggedInUser(request) { (session: LoggedInUserSession, userUrn: Urn) =>
      pagination.withPage(request, userUrn) { page =>
        mapper.materialize(session, page).map {
          case Some(info) => JsonResponseBuilder.ok(UntypedJson.write(info.asInstanceOf[Timeline]))
          case None => ResponseBuilder.notFound()
        }
      }
    }

  def renderPublicTrackActivities(request: HandlerRequest): Future[Response] = {
    renderTrackActivities(request)
  }

  private def renderTrackActivities(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session: LoggedInUserSession, _) =>
      val pagination = Pagination.buildCursorBasedPagination(request, Seq("linked_partitioning"))

      val (cursor, reverseCursor) = pagination.extraParams.get("uuid[to]") match {
        case Some(uuid) => (Some(UUID.fromString(uuid)), true)
        case _ => (pagination.cursor.map(UUID.fromString), false)
      }

      val limit = pagination.pageSize

      performGetTrackActivities(session, cursor.map(_.toString), reverseCursor, limit, pagination)
        .map {
          case Good(timeline) => {
            JsonResponseBuilder.ok(timeline.getRepresentation())
          }
          case Bad(NotFound(_)) => JsonResponseBuilder.notFound(notFoundErrorString)
          case _ => throw new UnhandledOutcomeException
        }
    }
  }

  private def performGetTrackActivities(
      session: LoggedInUserSession,
      cursor: Option[String],
      reverseCursor: Boolean,
      limit: Int,
      pagination: CursorBasedPagination
  ): Future[Outcome[SimpleTimeline]] = {
    timelineService
      .fetchTimelineTracksForUser(session, cursor, reverseCursor, limit, Some("uuid"), pagination)
      .map {
        case timeline: SimpleTimeline => Good(timeline)
        case _ => NotFound().bad
      }
  }

  def renderFollowingTracks(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session: LoggedInUserSession, _) =>
      val pagination = Pagination.buildCursorBasedPagination(request, Seq("linked_partitioning"))

      val (cursor, reverseCursor) = pagination.extraParams.get("uuid[to]") match {
        case Some(uuid) => (Some(UUID.fromString(uuid)), true)
        case _ => (pagination.cursor.map(UUID.fromString), false)
      }

      val limit = pagination.pageSize
      performGetFollowingTrackActivities(session, cursor.map(_.toString), reverseCursor, limit)
        .map {
          case Good(tracks) => JsonResponseBuilder.ok(Json.stringify(Json.toJson(tracks)))
          case Bad(NotFound(_)) => JsonResponseBuilder.notFound(notFoundErrorString)
          case _ => throw new UnhandledOutcomeException
        }
    }
  }

  private def performGetFollowingTrackActivities(
      session: LoggedInUserSession,
      cursor: Option[String],
      reverseCursor: Boolean,
      limit: Int
  ): Future[Outcome[List[TrackRepresentation]]] = {
    timelineService
      .fetchFollowingTracksForUser(session, cursor, reverseCursor, limit, cursorEncoding = Some("uuid"))
      .map {
        case tracks: List[TrackRepresentation] => Good(tracks)
        case _ => NotFound().bad
      }
  }

  private val notFoundErrorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""
}
