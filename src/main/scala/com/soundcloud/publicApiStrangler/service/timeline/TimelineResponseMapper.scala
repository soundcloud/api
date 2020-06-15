package com.soundcloud.publicApiStrangler.service.timeline

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.Locale

import com.soundcloud.jvmkit.module.util.Urn
import com.twitter.util.{Return, Throw, Try}
import play.api.libs.json.{JsObject, JsValue}

class TimelineResponseMapper {
  def apply(json: JsValue): TimelineResponse =
    TimelineResponse(
      (json \ "events").as[List[JsObject]].flatMap(mapEvent),
      mapMeta((json \ "meta").as[JsValue])
    )

  def mapEvent(json: JsObject) = {
    val timestamp = rfc3339ToGMTFormat((json \ "timestamp").as[String])
    val urn = Urn.parse((json \ "urn").as[String]).get
    val actor = Urn.parse((json \ "actor").as[String]).get
    val cursor = (json \ "cursor").asOpt[String]

    (json \ "type").as[String] match {
      case "track" => Some(TimelineEvent(TrackTimelineEventType, timestamp, urn, actor))
      case "track:like" => Some(TimelineEvent(TrackLikeTimelineEventType, timestamp, urn, actor, cursor))
      case "users-tracks-likes" => Some(TimelineEvent(TrackLikeTimelineEventType, timestamp, urn, actor, cursor))
      case "likes" => Some(TimelineEvent(TrackLikeTimelineEventType, timestamp, urn, actor, cursor))
      case "track:repost" => Some(TimelineEvent(TrackRepostTimelineEventType, timestamp, urn, actor, cursor))
      case _ => None
    }
  }

  private def mapMeta(json: JsValue) =
    TimelineMeta(
      (json \ "previous_page_cursor").asOpt[String],
      (json \ "next_page_cursor").asOpt[String]
    )

  private def rfc3339ToGMTFormat(dateTime: String): String = {
    Try {
      Instant.parse(dateTime)
    } match {
      case Return(parsed) => gmtDateTimeFormat.format(parsed)
      case Throw(_) => dateTime
    }
  }

  private val gmtDateTimeFormat =
    DateTimeFormatter
      .ofPattern("yyyy/MM/dd HH:mm:ss Z")
      .withLocale(Locale.US)
      .withZone(ZoneId.of("UTC"))
}
