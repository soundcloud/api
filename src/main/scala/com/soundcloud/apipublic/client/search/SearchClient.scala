package com.soundcloud.apipublic.client.search

import com.soundcloud.apipublic.client.search.SearchDurationFilter.{Long, Short, Epic, Medium}
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import play.api.libs.json.Json
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import org.joda.time.DateTime

case class Doc(urn: Urn)

object Doc {
  implicit val format = Json.format[Doc]
}

case class Facet(value: String, count: Long, filter: String)

object Facet {
  implicit val format = Json.format[Facet]
}

case class FacetGroup(name: String, facets: Seq[Facet])

object FacetGroup {
  implicit val format = Json.format[FacetGroup]
}

sealed trait SearchDurationFilter {
  val value: String
}
object SearchDurationFilter {
  case object Long extends SearchDurationFilter { override val value: String = "LONG" }
  case object Epic extends SearchDurationFilter { override val value: String = "EPIC" }
  case object Short extends SearchDurationFilter { override val value: String = "SHORT" }
  case object Medium extends SearchDurationFilter { override val value: String = "MEDIUM" }
}

object SearchDurationFilters {
  def fromString(string: String): Option[SearchDurationFilter] = string match {
    case Long.`value` => Some(Long)
    case Epic.`value` => Some(Epic)
    case Short.`value` => Some(Short)
    case Medium.`value` => Some(Medium)
    case _ => None
  }

  def mustFromString(string: String): SearchDurationFilter =
    fromString(string).getOrElse(throw new IllegalArgumentException(s"Unknown search duration filter: $string"))
}

case class SearchResponse(
    query: String,
    query_urn: Urn,
    offset: Int,
    limit: Int,
    total_results: Long,
    query_time_in_millis: Int,
    docs: Seq[Doc],
    facets: Option[Seq[FacetGroup]]
)

object SearchResponse {
  implicit val format = Json.format[SearchResponse]
}

case class UsersParams(
    q: String,
    limit: Int,
    offset: Option[Int] = None,
    order: Option[String] = None,
    createdAt: Option[String] = None,
    createdAtFrom: Option[DateTime] = None,
    createdAtTo: Option[DateTime] = None,
    ids: Option[List[Long]] = None,
    clientId: Option[String] = None,
    place: Option[String] = None
)

case class PlaylistsParams(
    q: String,
    limit: Int,
    offset: Option[Int] = None,
    order: Option[String] = None,
    createdAt: Option[String] = None,
    createdAtFrom: Option[DateTime] = None,
    createdAtTo: Option[DateTime] = None,
    ids: Option[List[Long]] = None,
    clientId: Option[String] = None,
    genres: Option[List[String]] = None,
    tags: Option[List[String]] = None,
    showTracks: Option[Boolean] = None
)

case class TracksParams(
    q: String,
    limit: Int,
    offset: Option[Int] = None,
    order: Option[String] = None,
    contentTier: Option[String] = None,
    contentCountry: Option[String] = None,
    createdAt: Option[String] = None,
    createdAtFrom: Option[DateTime] = None,
    createdAtTo: Option[DateTime] = None,
    downloadable: Option[Boolean] = None,
    streamable: Option[Boolean] = None,
    bpm: Option[String] = None,
    bpmFrom: Option[Int] = None,
    bpmTo: Option[Int] = None,
    duration: Option[SearchDurationFilter] = None,
    durationFrom: Option[Int] = None,
    durationTo: Option[Int] = None,
    license: Option[String] = None,
    genres: Option[List[String]] = None,
    tags: Option[List[String]] = None,
    ids: Option[List[Long]] = None,
    clientId: Option[String] = None,
    place: Option[String] = None
)

case class SearchQueryParams(
    q: String,
    limit: Int,
    offset: Option[Int] = None,
    order: Option[String] = None,
    contentTier: Option[String] = None,
    contentCountry: Option[String] = None,
    createdAt: Option[String] = None,
    createdAtFrom: Option[DateTime] = None,
    createdAtTo: Option[DateTime] = None,
    downloadable: Option[Boolean] = None,
    streamable: Option[Boolean] = None,
    bpm: Option[String] = None,
    bpmFrom: Option[Int] = None,
    bpmTo: Option[Int] = None,
    duration: Option[SearchDurationFilter] = None,
    durationFrom: Option[Long] = None,
    durationTo: Option[Long] = None,
    license: Option[String] = None,
    genres: Option[List[String]] = None,
    tags: Option[List[String]] = None,
    ids: Option[List[Long]] = None,
    clientId: Option[String] = None,
    place: Option[String] = None
)

trait SearchClient {

  def searchTracks(
      session: UserSession,
      params: TracksParams,
      headers: Headers = Headers.empty,
      access: AccessParams
  ): OutcomeF[SearchResponse]

  def searchPlaylists(
      session: UserSession,
      params: PlaylistsParams,
      headers: Headers = Headers.empty,
      access: AccessParams
  ): OutcomeF[SearchResponse]

  def searchUsers(
      session: UserSession,
      params: UsersParams,
      headers: Headers = Headers.empty,
      access: AccessParams
  ): OutcomeF[SearchResponse]

  def search(
      session: UserSession,
      path: Path,
      params: SearchQueryParams,
      headers: Headers = Headers.empty,
      access: AccessParams
  ): OutcomeF[SearchResponse]
}
