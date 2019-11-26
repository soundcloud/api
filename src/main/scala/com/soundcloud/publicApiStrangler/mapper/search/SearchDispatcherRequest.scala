package com.soundcloud.publicApiStrangler.mapper.search

import com.soundcloud.jvmkit.module.http.client.{Params, StringParam}
import com.soundcloud.jvmkit.module.util.Path
import com.twitter.finagle.http.Request

case class SearchDispatcherRequest(searchPath: Path, paginationParams: Set[String], searchHeaders: Map[String, String])(
    val mapParams: Params => Params
)

// two param lists because we don't want to include the function
// in equality testing in tests

object SearchDispatcherRequest {
  val SearchParameters = Set("offset", "limit", "anon_user_id", "q", "sort", "user_id", "client_id")
  val SearchParameterPattern = """^filter\..+""".r

  val RequestIdHeader = "x-request-id"

  private val TrackParamMappings = Map(
    "q" -> "q",
    "offset" -> "offset",
    "limit" -> "limit",
    "order" -> "sort",
    "tags" -> "filter.tag",
    "license" -> "filter.license",
    "bpm" -> "filter.bpm",
    "bpm[from]" -> "filter.bpm[from]",
    "bpm[to]" -> "filter.bpm[to]",
    "duration" -> "filter.duration",
    "duration[from]" -> "filter.duration[from]",
    "duration[to]" -> "filter.duration[to]",
    "created_at" -> "filter.created_at",
    "created_at[from]" -> "filter.created_at[from]",
    "created_at[to]" -> "filter.created_at[to]",
    "ids" -> "filter.id",
    "genres" -> "filter.genre",
    "client_id" -> "client_id"
  )

  def mapTrackParams(params: Params): Params = params.collect {
    case (k, v) if TrackParamMappings contains k => TrackParamMappings(k) -> v
    case ("filter", v) if v.value contains "downloadable" => "filter.downloadable" -> StringParam("true")
    case ("filter", v) if v.value contains "streamable" => "filter.streamable" -> StringParam("true")
  }

  private val CommonParamMappings = Map(
    "q" -> "q",
    "offset" -> "offset",
    "limit" -> "limit",
    "order" -> "sort",
    "created_at" -> "filter.created_at",
    "created_at[from]" -> "filter.created_at[from]",
    "created_at[to]" -> "filter.created_at[to]",
    "ids" -> "filter.id",
    "client_id" -> "client_id"
  )

  private val PlaylistParamMappings = Map(
    "genres" -> "filter.genre",
    "tags" -> "filter.tag"
  )

  def mapCommonParams(params: Params): Params = mapParams(params, CommonParamMappings)

  def mapPlaylistParams(params: Params): Params = mapParams(params, CommonParamMappings ++ PlaylistParamMappings)

  def mapParams(params: Params, paramMappings: Map[String, String]): Params = params.collect {
    case (k, v) if paramMappings contains k => (paramMappings(k), v)
  }

  val trackSearch: Request => SearchDispatcherRequest = request =>
    raw(SearchRepository.TracksPath, request, TrackParamMappings.keySet + "filter", mapTrackParams)

  val userSearch: Request => SearchDispatcherRequest = request =>
    raw(SearchRepository.UsersPath, request, CommonParamMappings.keySet, mapCommonParams)

  val playlistSearch: Request => SearchDispatcherRequest = request =>
    raw(
      SearchRepository.PlaylistsPath,
      request,
      CommonParamMappings.keySet ++ PlaylistParamMappings.keySet,
      mapPlaylistParams
    )

  def raw(
      path: Path,
      request: Request,
      paginationParams: Set[String],
      mapParams: Params => Params
  ): SearchDispatcherRequest = {
    val headers = request.headerMap.filterKeys(_.toLowerCase == RequestIdHeader).toMap
    SearchDispatcherRequest(path, paginationParams, headers)(mapParams)
  }
}
