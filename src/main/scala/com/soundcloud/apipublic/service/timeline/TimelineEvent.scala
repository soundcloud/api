package com.soundcloud.apipublic.service.timeline

import com.soundcloud.jvmkit.module.util.Urn

sealed trait TimelineEventType
case object TrackTimelineEventType extends TimelineEventType
case object TrackRepostTimelineEventType extends TimelineEventType
case object PlaylistRepostTimelineEventType extends TimelineEventType
case object PlaylistTimelineEventType extends TimelineEventType

case class TimelineEvent(
    eventType: TimelineEventType,
    timestamp: String,
    urn: Urn,
    actor: Urn,
    cursor: Option[String] = None
)
