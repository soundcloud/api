package com.soundcloud.publicApiStrangler.service.trackrepresentation

import java.net.URL

import com.soundcloud.jvmkit.module.util.Urn
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}

import scala.util.Try

case class TrackPagination(
    maybeLimit: Option[Int],
    maybeOffset: Option[Int],
    linkedPartitioning: Boolean,
    createdAtFrom: Option[DateTime],
    createdAtTo: Option[DateTime],
    requestUrl: URL
) {
  val offset = maybeOffset.getOrElse(0)

  // defaults to 50 if not provided, max at 200 to mimic mothership's behaviour
  val limit = Math.min(maybeLimit.getOrElse(50), 200)

  def calculateTrackUrnPage(trackUrns: List[Urn]): Set[Urn] = {
    val start = offset
    val end = start + limit + limit // over-fetch to compensate for client filtering
    // sort by id desc
    trackUrns.sortBy(-_.identifier.toLong).slice(start, end).toSet
  }

  def calculateFinalPage(tracks: List[TrackRepresentation]): List[TrackRepresentation] = {
    tracks
      .filter(t =>
        createdAtFrom.forall(t.createdAt.toDateTime.isAfter(_)) &&
          createdAtTo.forall(t.createdAt.toDateTime.isBefore(_))
      )
      .sortBy(-_.id)
      .slice(0, limit)
  }

  def nextHref(totalTracks: Int): Option[String] = {
    if (!linkedPartitioning) {
      None
    } else {
      val nextOffset = offset + limit

      if (totalTracks < nextOffset) {
        None
      } else {
        val params = requestUrl.getQuery
          .split("&")
          .toList
          .filter(s => !s.contains("client_id"))
          .map(_.split("=").toList)
          .flatMap {
            case List(key, value) => Some((key, value))
            case List(key) => Some((key, ""))
            case otherwise => Some((otherwise, ""))
          }
          .toMap

        val nextParams = params ++ Map("limit" -> limit.toString, "offset" -> nextOffset.toString)

        val nextHref =
          List(requestUrl.toString.split("\\?").head, nextParams.map { case (k, v) => s"$k=$v" }.mkString("&"))
            .mkString("?")
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
      params.contains("linked_partitioning"),
      params.get("created_at[from]").flatMap(tryParseDate),
      params.get("created_at[to]").flatMap(tryParseDate),
      requestUrl
    )
  }

  val oddPatterns = List(
    DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss Z"),
    DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"),
    DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z")
  )

  val attempts =
    oddPatterns.map(pattern => (value: String) => DateTime.parse(value, pattern)) ++
      List(
        (value: String) => DateTime.parse(value),
        (value: String) => new DateTime(value, DateTimeZone.UTC)
      )

  private def tryParseDate(value: String): Option[DateTime] = {
    attempts.flatMap(parseFn => Try(parseFn(value)).toOption).headOption.map(normalizeToUTC)
  }

  private def normalizeToUTC(dateTime: DateTime): DateTime =
    dateTime.toDateTime(DateTimeZone.UTC).toDateTime
}
