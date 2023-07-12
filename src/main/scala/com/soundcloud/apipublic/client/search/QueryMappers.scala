package com.soundcloud.apipublic.client.search

import com.soundcloud.jvmkit.module.http.client.{ListParam, Params, StringParam}

object QueryMappers {
  implicit class PlaylistsParamsToQueryParams(params: PlaylistsParams) {
    def toParams: Params =
      Map(
        "q" -> Some(params.q),
        "limit" -> Some(params.limit.toString),
        "offset" -> params.offset.map(_.toString),
        "sort" -> params.order,
        "clientId" -> params.clientId,
        "filter.created_at" -> params.createdAt,
        "filter.created_at[from]" -> params.createdAtFrom.map(_.formatted("yyyy-MM-dd HH:mm:ss")),
        "filter.created_at[to]" -> params.createdAtTo.map(_.formatted("yyyy-MM-dd HH:mm:ss"))
      ).collect { case (k, Some(v)) => k -> StringParam(v) } ++ Map(
        "filter.id" -> params.ids,
        "filter.tag" -> params.tags,
        "filter.genre" -> params.genres
      ).collect { case (k, Some(v)) => k -> ListParam(v) }
  }

  implicit class TracksParamsToQueryParams(params: TracksParams) {
    def toParams: Params =
      Map(
        "q" -> Some(params.q),
        "limit" -> Some(params.limit.toString),
        "offset" -> params.offset.map(_.toString),
        "sort" -> params.order,
        "clientId" -> params.clientId,
        "downloadable" -> params.downloadable.map(v => if (v) "true" else "false"),
        "streamable" -> params.streamable.map(v => if (v) "true" else "false"),
        "filter.place" -> params.place,
        "filter.created_at" -> params.createdAt,
        "filter.created_at[from]" -> params.createdAtFrom.map(_.formatted("yyyy-MM-dd HH:mm:ss")),
        "filter.created_at[to]" -> params.createdAtTo.map(_.formatted("yyyy-MM-dd HH:mm:ss")),
        "filter.bpm" -> params.bpm,
        "filter.bpm_at[from]" -> params.bpmFrom.map(_.toString),
        "filter.bpm_at[to]" -> params.bpmTo.map(_.toString),
        "filter.duration" -> params.duration.map(_.value),
        "filter.duration_at[from]" -> params.durationFrom.map(_.toString),
        "filter.duration_at[to]" -> params.durationTo.map(_.toString),
        "filter.license" -> params.license.map(_.mkString(",")),
        "filter.content_tier" -> params.contentTier,
        "filter.content_country" -> params.contentCountry
      ).collect { case (k, Some(v)) => k -> StringParam(v) } ++ Map(
        "filter.id" -> params.ids,
        "filter.tag" -> params.tags,
        "filter.genre" -> params.genres
      ).collect { case (k, Some(v)) => k -> ListParam(v) }
  }

  implicit class SearchQueryParamsToQueryParams(params: SearchQueryParams) {
    def toParams: Params =
      Map(
        "q" -> Some(params.q),
        "limit" -> Some(params.limit.toString),
        "offset" -> params.offset.map(_.toString),
        "sort" -> params.order,
        "clientId" -> params.clientId,
        "downloadable" -> params.downloadable.map(v => if (v) "true" else "false"),
        "streamable" -> params.streamable.map(v => if (v) "true" else "false"),
        "filter.place" -> params.place,
        "filter.created_at" -> params.createdAt,
        "filter.created_at[from]" -> params.createdAtFrom.map(_.formatted("yyyy-MM-dd HH:mm:ss")),
        "filter.created_at[to]" -> params.createdAtTo.map(_.formatted("yyyy-MM-dd HH:mm:ss")),
        "filter.bpm" -> params.bpm,
        "filter.bpm_at[from]" -> params.bpmFrom.map(_.toString),
        "filter.bpm_at[to]" -> params.bpmTo.map(_.toString),
        "filter.duration" -> params.duration.map(_.value),
        "filter.duration_at[from]" -> params.durationFrom.map(_.toString),
        "filter.duration_at[to]" -> params.durationTo.map(_.toString),
        "filter.license" -> params.license.map(_.mkString(",")),
        "filter.content_tier" -> params.contentTier,
        "filter.content_country" -> params.contentCountry
      ).collect { case (k, Some(v)) => k -> StringParam(v) } ++ Map(
        "filter.id" -> params.ids,
        "filter.tag" -> params.tags,
        "filter.genre" -> params.genres
      ).collect { case (k, Some(v)) => k -> ListParam(v) }
  }

  implicit class UsersParamsToQueryParams(params: UsersParams) {
    def toParams: Params =
      Map(
        "q" -> Some(params.q),
        "limit" -> Some(params.limit.toString),
        "offset" -> params.offset.map(_.toString),
        "sort" -> params.order,
        "clientId" -> params.clientId,
        "filter.place" -> params.place,
        "filter.created_at" -> params.createdAt,
        "filter.created_at[from]" -> params.createdAtFrom.map(_.toString),
        "filter.created_at[to]" -> params.createdAtTo.map(_.toString)
      ).collect { case (k, Some(v)) => k -> StringParam(v) } ++ Map(
        "filter.id" -> params.ids
      ).collect { case (k, Some(v)) => k -> ListParam(v) }
  }
}
