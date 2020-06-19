package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.{LoggedInUserSession}
import com.soundcloud.publicApiStrangler.client.TimelineJsonClient
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.timeline._
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackRepresentation, TrackRepresentationsService}
import com.twitter.util.Future

class TimelineService(
    timelineJsonClient: TimelineJsonClient,
    trackRepresentationsService: TrackRepresentationsService,
    timelineResponseMapper: TimelineResponseMapper = new TimelineResponseMapper()
) {

  def fetchTimelineTracksForUser(
      session: LoggedInUserSession,
      cursor: Option[String],
      reverseCursor: Boolean,
      limit: Int,
      cursorEncoding: Option[String],
      pagination: CursorBasedPagination
  ): Future[Timeline] = {
    for {
      trackActivities <- timelineJsonClient.stream(session, cursor, limit, reverseCursor, cursorEncoding)
      timelineResponse = timelineResponseMapper(trackActivities)
      tracks <- fetchFullItemList(session, timelineResponse.events)
    } yield {
      val timelineItems = timelineResponse.events.flatMap(event => createTrackTimelineItem(tracks, event))
      Timeline(timelineItems, timelineResponse.meta, pagination)
    }
  }

  def fetchFullItemList(
      session: LoggedInUserSession,
      events: List[TimelineEvent]
  ): Future[List[TrackRepresentation]] = {
    val trackUrns = trackUrnsFromEvents(events)
    trackRepresentationsService
      .tracks(session, trackUrns.map(TrackRequest(_, None)))
  }

  private def trackUrnsFromEvents(events: List[TimelineEvent]): List[Urn] =
    (events).collect {
      case TimelineEvent(TrackTimelineEventType, _, urn, _, _) => urn
      case TimelineEvent(TrackRepostTimelineEventType, _, urn, _, _) => urn
      case TimelineEvent(TrackLikeTimelineEventType, _, urn, _, _) => urn
    }.distinct

  private def createTrackTimelineItem(
      tracks: List[TrackRepresentation],
      event: TimelineEvent
  ): Option[TrackTimelineItem] =
    tracks
      .find(_.track.urn.toString == event.urn.toString)
      .map(trackRep => new TrackTimelineItem(event.timestamp, trackRep))

  def fetchFollowingTracksForUser(
      session: LoggedInUserSession,
      cursor: Option[String],
      reverseCursor: Boolean,
      limit: Int,
      cursorEncoding: Option[String]
  ): Future[List[TrackRepresentation]] = {
    for {
      trackActivities <- timelineJsonClient.followingsTracks(session, cursor, limit, reverseCursor, cursorEncoding)
      timelineResponse = timelineResponseMapper(trackActivities)
      tracks <- fetchFullItemList(session, timelineResponse.events)
    } yield tracks
  }
}
