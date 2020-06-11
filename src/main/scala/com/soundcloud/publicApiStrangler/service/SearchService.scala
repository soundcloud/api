package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.http.client.{Params, StringParam}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.search.SearchClient
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentationsService,
  TracksCollection
}
import com.twitter.util.Future

class SearchService(
    trackRepresentationsService: TrackRepresentationsService,
    searchClient: SearchClient
) {

  def searchTracks(
      session: UserSession,
      params: Map[String, String],
      trackPagination: TrackPagination
  ): Future[TracksCollection] = {
    val mapParams = mapTrackParams(params) ++ Params(
      "filter.content_tier" -> "FREE",
      "filter.content_country" -> session.getGeo.getCountryCode
    )
    for {
      searchPage <- searchClient.searchTracks(session, mapParams)
      enrichedTracks <- trackRepresentationsService.tracks(
        session,
        searchPage.docs.map(doc => TrackRequest(doc.urn, None)).toList
      )
    } yield {
      TracksCollection(enrichedTracks, trackPagination.nextHref(searchPage.total_results.toInt))
    }
  }

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

  private def mapTrackParams(params: Params): Params = params.collect {
    case (k, v) if TrackParamMappings contains k => TrackParamMappings(k) -> v
    case ("filter", v) if v.value contains "downloadable" => "filter.downloadable" -> StringParam("true")
    case ("filter", v) if v.value contains "streamable" => "filter.streamable" -> StringParam("true")
  }
}
