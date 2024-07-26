package com.soundcloud.apipublic.client.search
import com.google.protobuf.timestamp.Timestamp
import com.soundcloud.apipublic.authorization.policies.Access
import com.soundcloud.apipublic.client.search.SearchApiClient.ScSystem
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import org.joda.time.DateTime
import proto.soundcloud.search.api.SearchFilters.{CreatedAtRange, DurationRange}
import proto.soundcloud.search.api.{SearchFilters, SearchOffsetPagination, SimpleSearchRequest}

import java.util.UUID

object ProtoMappers {
  private def generateAnonymousID: Option[String] = Some("fake-" + UUID.randomUUID().toString)

  private def mapCreatedAtFixed(f: String): SearchFilters.CreatedAtRange =
    CreatedAtRange().withFixedRange(f.toUpperCase() match {
      case "LAST_HOUR" => SearchFilters.CreatedAtFixedRange.LAST_HOUR_CREATED_AT
      case "LAST_DAY" => SearchFilters.CreatedAtFixedRange.LAST_DAY_CREATED_AT
      case "LAST_WEEK" => SearchFilters.CreatedAtFixedRange.LAST_WEEK_CREATED_AT
      case "LAST_TWO_WEEK" => SearchFilters.CreatedAtFixedRange.LAST_TWO_WEEKS_CREATED_AT
      case "LAST_MONTH" => SearchFilters.CreatedAtFixedRange.LAST_MONTH_CREATED_AT
      case "LAST_THREE_MONTHS" => SearchFilters.CreatedAtFixedRange.LAST_THREE_MONTHS_CREATED_AT
      case "LAST_SIX_MONTHS" => SearchFilters.CreatedAtFixedRange.LAST_SIX_MONTHS_CREATED_AT
      case "LAST_YEAR" => SearchFilters.CreatedAtFixedRange.LAST_YEAR_CREATED_AT
      case _ => SearchFilters.CreatedAtFixedRange.ANY_CREATED_AT

    })

  private def mapCreatedAtDynamic(
      from: Option[DateTime],
      to: Option[DateTime]
  ): Option[SearchFilters.CreatedAtRange] =
    ((from, to) match {
      case (Some(f), Some(t)) =>
        Some(
          SearchFilters
            .CreatedAtDynamicRange(from = Some(Timestamp(f.getMillis / 1000)), to = Some(Timestamp(t.getMillis / 1000)))
        )
      case (Some(f), _) =>
        Some(SearchFilters.CreatedAtDynamicRange(from = Some(Timestamp(f.getMillis / 1000)), to = None))
      case (_, Some(t)) =>
        Some(SearchFilters.CreatedAtDynamicRange(from = None, to = Some(Timestamp(t.getMillis / 1000))))
      case _ => None
    }).map(CreatedAtRange().withDynamicRange(_))

  private def mapDurationFixed(f: SearchDurationFilter): SearchFilters.DurationRange =
    DurationRange().withFixedRange(f match {
      case SearchDurationFilter.Long => SearchFilters.DurationFixedRange.LONG_DURATION
      case SearchDurationFilter.Epic => SearchFilters.DurationFixedRange.EPIC_DURATION
      case SearchDurationFilter.Short => SearchFilters.DurationFixedRange.SHORT_DURATION
      case SearchDurationFilter.Medium => SearchFilters.DurationFixedRange.MEDIUM_DURATION
    })

  private def mapDurationDynamic(
      from: Option[Int],
      to: Option[Int]
  ): Option[SearchFilters.DurationRange] =
    ((from, to) match {
      case (Some(f), Some(t)) =>
        Some(
          SearchFilters
            .DurationDynamicRange(
              fromMs = Some(f.toLong),
              toMs = Some(t.toLong)
            )
        )
      case (Some(f), _) =>
        Some(SearchFilters.DurationDynamicRange(fromMs = Some(f.toLong), toMs = None))
      case (_, Some(t)) =>
        Some(SearchFilters.DurationDynamicRange(fromMs = None, toMs = Some(t.toLong)))
      case _ => None
    }).map(DurationRange().withDynamicRange(_))

  private def mapBPMDynamic(from: Option[Int], to: Option[Int]): SearchFilters.BPMDynamicRange =
    SearchFilters.BPMDynamicRange(from, to)

  implicit class PlaylistsParamsToProto(params: PlaylistsParams) {
    def toSimpleSearchRequest(session: UserSession, access: AccessParams): SimpleSearchRequest = SimpleSearchRequest(
      scSystem = Some(ScSystem),
      userSession = Some(session.asProtoSession),
      anonymousId = if (session.isAnonymous) generateAnonymousID else None,
      text = params.q,
      filters = Some(
        SearchFilters(
          contentType = SearchFilters.ContentType.PLAYLISTS,
          createdAtRange = params.createdAt
            .map(mapCreatedAtFixed)
            .orElse(mapCreatedAtDynamic(params.createdAtFrom, params.createdAtTo)),
          contentCountry = access.access.find(_ == Access.Blocked).map(_ => "ANY"),
          ids = params.ids.map(_.toSeq).getOrElse(Seq.empty),
          genres = params.genres.map(_.map(_.toLowerCase).toSeq).getOrElse(Seq.empty),
          hashtags = params.tags.map(_.map(_.toLowerCase).toSeq).getOrElse(Seq.empty)
        )
      ),
      pagination = Some(
        SearchOffsetPagination(
          offset = params.offset,
          limit = Some(params.limit)
        )
      )
    )
  }

  implicit class TracksParamsToProto(params: TracksParams) {
    def toSimpleSearchRequest(session: UserSession, access: AccessParams): SimpleSearchRequest = SimpleSearchRequest(
      scSystem = Some(ScSystem),
      userSession = Some(session.asProtoSession),
      anonymousId = if (session.isAnonymous) generateAnonymousID else None,
      text = params.q,
      filters = Some(
        SearchFilters(
          contentType = SearchFilters.ContentType.TRACKS,
          createdAtRange = params.createdAt
            .map(mapCreatedAtFixed)
            .orElse(mapCreatedAtDynamic(from = params.createdAtFrom, to = params.createdAtTo)),
          bpm =
            if (params.bpmFrom.isEmpty && params.bpmTo.isEmpty) None
            else Some(mapBPMDynamic(params.bpmFrom, params.bpmTo)),
          ids = params.ids.map(_.toSeq).getOrElse(Seq.empty),
          genres = params.genres.map(_.map(_.toLowerCase).toSeq).getOrElse(Seq.empty),
          hashtags = params.tags.map(_.map(_.toLowerCase).toSeq).getOrElse(Seq.empty),
          contentCountry = access.access.find(_ == Access.Blocked).map(_ => "ANY").orElse(params.contentCountry),
          durationRange =
            params.duration.map(mapDurationFixed).orElse(mapDurationDynamic(params.durationFrom, params.durationTo)),
          contentTier = params.contentTier
            .map(_.toUpperCase)
            .map {
              case "FREE" => SearchFilters.ContentTier.FREE_TIER
              case "SUB_HIGH_TIER" => SearchFilters.ContentTier.SUB_HIGH_TIER
              case _ => SearchFilters.ContentTier.ANY_TIER
            }
            .getOrElse(SearchFilters.ContentTier.ANY_TIER),
          downloadable = params.downloadable
            .map {
              case true => SearchFilters.Downloadable.ONLY_DOWNLOADABLE
              case false => SearchFilters.Downloadable.ANY_DOWNLOADABLE
            }
            .getOrElse(SearchFilters.Downloadable.ANY_DOWNLOADABLE),
          streamable = params.streamable
            .map {
              case true => SearchFilters.Streamable.ONLY_STREAMABLE
              case false => SearchFilters.Streamable.ANY_STREAMABLE
            }
            .getOrElse(SearchFilters.Streamable.ANY_STREAMABLE)
        )
      ),
      pagination = Some(
        SearchOffsetPagination(
          offset = params.offset,
          limit = Some(params.limit)
        )
      )
    )
  }

  implicit class SearchQueryParamsToProto(params: SearchQueryParams) {
    def toSimpleSearchRequest(session: UserSession, access: AccessParams): SimpleSearchRequest = SimpleSearchRequest(
      scSystem = Some(ScSystem),
      userSession = Some(session.asProtoSession),
      anonymousId = if (session.isAnonymous) generateAnonymousID else None,
      text = params.q,
      filters = Some(
        SearchFilters(
          contentType = SearchFilters.ContentType.TRACKS,
          createdAtRange = params.createdAt
            .map(mapCreatedAtFixed)
            .orElse(mapCreatedAtDynamic(params.createdAtFrom, params.createdAtTo)),
          bpm =
            if (params.bpmFrom.isEmpty && params.bpmTo.isEmpty) None
            else Some(mapBPMDynamic(params.bpmFrom, params.bpmTo)),
          ids = params.ids.map(_.toSeq).getOrElse(Seq.empty),
          genres = params.genres.map(_.map(_.toLowerCase).toSeq).getOrElse(Seq.empty),
          hashtags = params.tags.map(_.map(_.toLowerCase).toSeq).getOrElse(Seq.empty),
          contentCountry = access.access.find(_ == Access.Blocked).map(_ => "ANY").orElse(params.contentCountry),
          contentTier = params.contentTier
            .map(_.toUpperCase)
            .map {
              case "FREE" => SearchFilters.ContentTier.FREE_TIER
              case "SUB_HIGH_TIER" => SearchFilters.ContentTier.SUB_HIGH_TIER
              case _ => SearchFilters.ContentTier.ANY_TIER
            }
            .getOrElse(SearchFilters.ContentTier.ANY_TIER),
          downloadable = params.downloadable
            .map {
              case true => SearchFilters.Downloadable.ONLY_DOWNLOADABLE
              case false => SearchFilters.Downloadable.ANY_DOWNLOADABLE
            }
            .getOrElse(SearchFilters.Downloadable.ANY_DOWNLOADABLE),
          streamable = params.streamable
            .map {
              case true => SearchFilters.Streamable.ONLY_STREAMABLE
              case false => SearchFilters.Streamable.ANY_STREAMABLE
            }
            .getOrElse(SearchFilters.Streamable.ANY_STREAMABLE)
        )
      ),
      pagination = Some(
        SearchOffsetPagination(
          offset = params.offset,
          limit = Some(params.limit)
        )
      )
    )
  }

  implicit class UsersParamsToProto(params: UsersParams) {
    def toSimpleSearchRequest(session: UserSession, access: AccessParams): SimpleSearchRequest = SimpleSearchRequest(
      scSystem = Some(ScSystem),
      userSession = Some(session.asProtoSession),
      anonymousId = if (session.isAnonymous) generateAnonymousID else None,
      text = params.q,
      filters = Some(
        SearchFilters(
          contentType = SearchFilters.ContentType.USERS,
          createdAtRange = params.createdAt
            .map(mapCreatedAtFixed)
            .orElse(mapCreatedAtDynamic(params.createdAtFrom, params.createdAtTo)),
          ids = params.ids.map(_.toSeq).getOrElse(Seq.empty),
          contentCountry = access.access.find(_ == Access.Blocked).map(_ => "ANY")
        )
      ),
      pagination = Some(
        SearchOffsetPagination(
          offset = params.offset,
          limit = Some(params.limit)
        )
      )
    )
  }
}
