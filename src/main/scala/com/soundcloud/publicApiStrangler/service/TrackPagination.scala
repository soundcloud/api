package com.soundcloud.publicApiStrangler.service

import java.net.URL

import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.client.trackmetadata.Track
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone, LocalDateTime}

import scala.util.Try


case class TrackPagination(maybeLimit: Option[Int],
                           maybeOffset: Option[Int],
                           linkedPartitioning: Boolean,
                           createdAtFrom: Option[LocalDateTime],
                           createdAtTo: Option[LocalDateTime],
                           requestUrl: URL) {

  val offset = maybeOffset.getOrElse(0)
  val limit = maybeLimit.getOrElse(Int.MaxValue)


  def calculateTrackUrnPage(trackUrns: List[Urn]): Set[Urn] = {
    val start = offset
    val end = start + limit
    // sort by id desc
    trackUrns.sortBy(-_.getIdentifier.toInt).slice(start, end).toSet
  }

  def calculateFinalPage(tracks: List[Track]): List[Track] = {
    tracks
      .filter(t => createdAtFrom.map(t.created_at.isAfter(_)).getOrElse(true) &&
                   createdAtTo.map(t.created_at.isBefore(_)).getOrElse(true))
      .sortBy(-_.urn.getIdentifier.toInt)
  }

  def nextHref(totalTracks: Int): Option[String] = {
    if (linkedPartitioning == false) {
      None
    } else {
      val nextOffset = offset + limit

      if (totalTracks < nextOffset) {
        None
      } else {
        val params = requestUrl.getQuery.split("&").toList.map(_.split("=").toList).flatMap {
          case List(key, value) => Some(key, value)
          case List(key) => Some(key, "")
          case otherwise => Some(otherwise, "")
        }.toMap

        val nextParams = params ++ Map("limit" -> limit.toString, "offset" -> nextOffset.toString)

        val nextHref = List(requestUrl.toString.split("\\?").head, nextParams.map { case (k, v) => s"$k=$v" }.mkString("&")).mkString("?")
        Some(nextHref)
      }
    }
  }

}

object TrackPagination {
  def fromRequest(params: Map[String, String], requestUrl: URL) = {
    TrackPagination(
      Try(params.get("limit").map(_.toInt)).toOption.flatten,
      Try(params.get("offset").map(_.toInt)).toOption.flatten,
      params.get("linked_partitioning").isDefined,
      params.get("created_at[from]").flatMap(tryParseDate),
      params.get("created_at[to]").flatMap(tryParseDate),
      requestUrl)
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
