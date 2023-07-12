package com.soundcloud.apipublic.handler.search

import com.soundcloud.apipublic.client.search.{PlaylistsParams, SearchDurationFilters, TracksParams, UsersParams}
import com.twitter.finagle.http.ParamMap
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.language.implicitConversions

object ParamsExtractor {
  val isNumber = "\\d+\\.?\\d+"
  val sourceFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
  val searchDefaultLimit = 50

  def mapCreatedAt(v: String): DateTime = DateTime.parse(v, sourceFormat)

  class ParamsToPlaylistParams(params: ParamMap) {
    def asPlaylistParams: PlaylistsParams = PlaylistsParams(
      q = params.getOrElse("q", ""),
      limit = params.get("limit").map(_.toInt).getOrElse(searchDefaultLimit),
      offset = params.get("offset").map(_.toInt),
      order = params.get("order"),
      createdAt = params.get("created_at"),
      createdAtFrom = params.get("created_at[from]").map(mapCreatedAt),
      createdAtTo = params.get("created_at[to]").map(mapCreatedAt),
      ids = params.get("ids").map(_.split(",").toList),
      clientId = params.get("client_id"),
      genres = params.get("genres").map(_.split(",").toList),
      tags = params.get("tags").map(_.split(",").toList),
      showTracks = params.get("show_tracks").map(_.toBoolean)
    )
  }

  class ParamsToUserParams(params: ParamMap) {
    def asUsersParams: UsersParams = UsersParams(
      q = params.getOrElse("q", ""),
      limit = params.get("limit").map(_.toInt).getOrElse(searchDefaultLimit),
      offset = params.get("offset").map(_.toInt),
      order = params.get("order"),
      createdAt = params.get("created_at"),
      createdAtFrom = params.get("created_at[from]").map(mapCreatedAt),
      createdAtTo = params.get("created_at[to]").map(mapCreatedAt),
      ids = params.get("ids").map(_.split(",").toList),
      clientId = params.get("client_id"),
      place = params.get("place")
    )
  }

  class ParamsToTrackParams(params: ParamMap) {
    def asTracksParams: TracksParams = TracksParams(
      q = params.getOrElse("q", ""),
      limit = params.get("limit").map(_.toInt).getOrElse(searchDefaultLimit),
      offset = params.get("offset").map(_.toInt),
      order = params.get("order"),
      createdAt = params.get("created_at"),
      createdAtFrom = params.get("created_at[from]").map(mapCreatedAt),
      createdAtTo = params.get("created_at[to]").map(mapCreatedAt),
      bpm = params.get("bpm"),
      bpmFrom = params.get("bpm[from]").map(_.toInt),
      bpmTo = params.get("bpm[to]").map(_.toInt),
      duration = params.get("duration").map(SearchDurationFilters.mustFromString),
      durationFrom = params.get("duration[from]").map(_.toInt),
      durationTo = params.get("duration[to]").map(_.toInt),
      genres = params.get("genres").map(_.split(",").toList),
      tags = params.get("tags").map(_.split(",").toList),
      ids = params.get("ids").map(_.split(",").toList),
      license = params.get("license"),
      clientId = params.get("client_id"),
      place = params.get("place"),
      downloadable = params.get("filter").map(_ == "streamable"),
      streamable = params.get("filter").map(_ == "downloadable"),
      contentTier = params.get("content_tier"),
      contentCountry = params.get("content_country")
    )
  }

  implicit def paramsToUsersParams(params: ParamMap): ParamsToUserParams =
    new ParamsToUserParams(params)

  implicit def paramsToTracksParams(params: ParamMap): ParamsToTrackParams =
    new ParamsToTrackParams(params)

  implicit def paramsToPlaylistParams(params: ParamMap): ParamsToPlaylistParams =
    new ParamsToPlaylistParams(params)
}
