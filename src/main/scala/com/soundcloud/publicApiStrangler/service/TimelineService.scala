package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.LoggedInUserSession
import com.soundcloud.publicApiStrangler.client.TimelineJsonClient
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.handler.support.requestParser.AccessParams
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.playlists.PlaylistRequest
import com.soundcloud.publicApiStrangler.service.playlists.representation.Playlist
import com.soundcloud.publicApiStrangler.service.timeline._
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackRepresentation, TrackRepresentationsService}
import com.twitter.util.Future

class TimelineService(
    timelineJsonClient: TimelineJsonClient,
    trackRepresentationsService: TrackRepresentationsService,
    playlistsService: PlaylistsService,
    timelineResponseMapper: TimelineResponseMapper = new TimelineResponseMapper()
) {

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
      tracks <- getTrackRepresentations(session, timelineResponse.events)
    } yield tracks
  }

  def fetchTimelineForUser(
      session: LoggedInUserSession,
      cursor: Option[String],
      reverseCursor: Boolean,
      limit: Int,
      cursorEncoding: Option[String],
      pagination: CursorBasedPagination
  ): Future[Timeline] = {
    for {
      timelineResponse <- fetchTimelineObjects(session, cursor, reverseCursor, limit, cursorEncoding)
      tracks <- getTrackRepresentations(session, timelineResponse.events)
      playlists <- getPlaylistRepresentations(session, timelineResponse.events)
    } yield {
      val timelineItems = createTimelineItems(timelineResponse.events, tracks, playlists)
      Timeline(timelineItems, timelineResponse.meta, pagination)
    }
  }

  def fetchTimelineTracksForUser(
      session: LoggedInUserSession,
      cursor: Option[String],
      reverseCursor: Boolean,
      limit: Int,
      cursorEncoding: Option[String],
      pagination: CursorBasedPagination
  ): Future[Timeline] = {
    for {
      timelineResponse <- fetchTimelineObjects(session, cursor, reverseCursor, limit, cursorEncoding)
      tracks <- getTrackRepresentations(session, timelineResponse.events)
    } yield {
      val trackTimelineItems = timelineResponse.events.flatMap(event => createTrackTimelineItem(tracks, event))
      Timeline(trackTimelineItems, timelineResponse.meta, pagination)
    }
  }

  private def createTimelineItems(
      events: List[TimelineEvent],
      tracks: List[TrackRepresentation],
      playlists: List[Playlist]
  ): List[TimelineItem] = {
    events.flatMap(event => {
      event.eventType match {
        case TrackTimelineEventType | TrackRepostTimelineEventType =>
          createTrackTimelineItem(tracks, event)
        case PlaylistRepostTimelineEventType | PlaylistTimelineEventType => createPlaylistTimelineItem(playlists, event)
      }
    })
  }

  private def fetchTimelineObjects(
      session: LoggedInUserSession,
      cursor: Option[String],
      reverseCursor: Boolean,
      limit: Int,
      cursorEncoding: Option[String]
  ): Future[TimelineResponse] = {
    for {
      activities <- timelineJsonClient.stream(session, cursor, limit, reverseCursor, cursorEncoding)
      timelineResponse = timelineResponseMapper(activities)
    } yield timelineResponse
  }

  private def getTrackRepresentations(
      session: LoggedInUserSession,
      events: List[TimelineEvent]
  ): Future[List[TrackRepresentation]] = {
    val trackUrns = trackUrnsFromEvents(events)
    trackRepresentationsService
      .tracks(session, trackUrns.map(TrackRequest(_, None)), AccessParams.explicitAccess)
  }

  private def getPlaylistRepresentations(
      session: LoggedInUserSession,
      events: List[TimelineEvent]
  ): Future[List[Playlist]] = {
    val playlistUrns = playlistUrnsFromEvents(events)
    playlistsService.fetchPlaylistsMetadataOnly(session, playlistUrns.map(PlaylistRequest(_, None)))

  }

  private def trackUrnsFromEvents(events: List[TimelineEvent]): List[Urn] =
    events.collect {
      case TimelineEvent(TrackTimelineEventType, _, urn, _, _) => urn
      case TimelineEvent(TrackRepostTimelineEventType, _, urn, _, _) => urn
    }.distinct

  private def playlistUrnsFromEvents(events: List[TimelineEvent]): List[Urn] =
    events.collect {
      case TimelineEvent(PlaylistTimelineEventType, _, urn, _, _) => urn
      case TimelineEvent(PlaylistRepostTimelineEventType, _, urn, _, _) => urn
    }.distinct

  private def createTrackTimelineItem(
      tracks: List[TrackRepresentation],
      event: TimelineEvent
  ): Option[TrackTimelineItem] =
    tracks
      .find(_.visibleTrack.urn.toString == event.urn.toString)
      .map(trackRep =>
        event.eventType match {
          case TrackRepostTimelineEventType =>
            new TrackTimelineItem(
              createdAt = event.timestamp,
              timelineItemType = "track:repost",
              track = trackRep
            )
          case _ =>
            new TrackTimelineItem(
              createdAt = event.timestamp,
              timelineItemType = "track",
              track = trackRep
            )
        }
      )

  private def createPlaylistTimelineItem(
      playlists: List[Playlist],
      event: TimelineEvent
  ): Option[PlaylistTimelineItem] =
    playlists
      .find(_.id == event.urn.identifier.toLong)
      .map(playlist =>
        event.eventType match {
          case PlaylistRepostTimelineEventType =>
            new PlaylistTimelineItem(
              createdAt = event.timestamp,
              timelineItemType = "playlist:repost",
              playlist = playlist
            )
          case _ =>
            new PlaylistTimelineItem(
              createdAt = event.timestamp,
              timelineItemType = "playlist",
              playlist = playlist
            )
        }
      )

}
