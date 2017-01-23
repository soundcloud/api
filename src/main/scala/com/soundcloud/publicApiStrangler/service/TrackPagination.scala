package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.client.trackmetadata.Track
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone, LocalDateTime}

import scala.util.Try


case class TrackPagination(limit: Option[Int],
                           offset: Option[Int],
                           linkedPartitioning: Boolean,
                           createdAtFrom: Option[LocalDateTime],
                           createdAtTo: Option[LocalDateTime]) {

  def calculateTrackUrnPage(trackUrns: List[Urn]): Set[Urn] = {
    val start = offset.getOrElse(0)
    val end = start + limit.getOrElse(Int.MaxValue)
    // sort by id desc
    trackUrns.sortBy(-_.getIdentifier.toInt).slice(start, end).toSet
  }

  def calculateFinalPage(tracks: List[Track]): List[Track] = {
    tracks
      .filter(t => createdAtFrom.map(t.created_at.isAfter(_)).getOrElse(true) &&
                   createdAtTo.map(t.created_at.isBefore(_)).getOrElse(true))
      .sortBy(-_.urn.getIdentifier.toInt)
  }

}

object TrackPagination {
  def fromRequest(params: Map[String, String]) = {
    TrackPagination(
      Try(params.get("limit").map(_.toInt)).toOption.flatten,
      Try(params.get("offset").map(_.toInt)).toOption.flatten,
      params.get("linked_partitioning").isDefined,
      params.get("created_at[from]").flatMap(tryParseDate),
      params.get("created_at[to]").flatMap(tryParseDate))
  }

  val oddPatterns = List(
    DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss Z"),
    DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))

  val attempts =
    oddPatterns.map(pattern => (value: String) => DateTime.parse(value, pattern)) ++
    List(
      (value: String) => DateTime.parse(value),
      (value: String) => new DateTime(value, DateTimeZone.UTC)
    )

  private def tryParseDate(value: String): Option[LocalDateTime] = {
    attempts.flatMap(parseFn => Try(parseFn(value)).toOption).headOption.map(normalizeToUTC)
  }

  private def normalizeToUTC(dateTime: DateTime): LocalDateTime =
    dateTime.toDateTime(DateTimeZone.UTC).toLocalDateTime
}
