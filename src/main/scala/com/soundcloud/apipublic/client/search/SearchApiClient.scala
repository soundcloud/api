package com.soundcloud.apipublic.client.search

import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.periskop.client.Severity
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.finagle.http.Status
import com.twitter.util.{Return, Throw}
import proto.soundcloud.search.api.Aggregation.Aggregation
import proto.soundcloud.search.api.{
  SearchClientProtobuf,
  SimpleSearchRequest,
  SimpleSearchResponse,
  Aggregation => AggregationProto
}

class SearchApiClient(client: SearchClientProtobuf, exceptionCollector: ExceptionCollector) extends SearchClient {
  import ProtoMappers._

  override def searchTracks(
      session: UserSession,
      params: TracksParams,
      headers: Headers = Headers.empty,
      access: AccessParams
  ): OutcomeF[SearchResponse] = {
    doSearch(params.toSimpleSearchRequest(session, access), params.limit)
  }

  override def searchPlaylists(
      session: UserSession,
      params: PlaylistsParams,
      headers: Headers = Headers.empty,
      access: AccessParams
  ): OutcomeF[SearchResponse] =
    doSearch(params.toSimpleSearchRequest(session, access), params.limit)

  override def searchUsers(
      session: UserSession,
      params: UsersParams,
      headers: Headers = Headers.empty,
      access: AccessParams
  ): OutcomeF[SearchResponse] =
    doSearch(params.toSimpleSearchRequest(session, access), params.limit)

  override def search(
      session: UserSession,
      path: Path,
      params: SearchQueryParams,
      headers: Headers = Headers.empty,
      access: AccessParams
  ): OutcomeF[SearchResponse] =
    doSearch(params.toSimpleSearchRequest(session, access), params.limit)

  private def extractQueryUrn(res: SimpleSearchResponse): Option[Urn] =
    for {
      query <- res.query
      rawQueryUrn <- query.queryUrn
      queryUrn <- Urn.parse(rawQueryUrn).toOption
    } yield queryUrn

  private def extractResults(res: SimpleSearchResponse): Seq[Doc] =
    for {
      results <- res.top ++ res.results
      entity <- results.entity.unresolvedEntity
      urn <- Urn.parse(entity.urn).toOption
    } yield Doc(urn)

  private def extractFacets(res: SimpleSearchResponse): Seq[FacetGroup] =
    for {
      aggregation <- res.aggregations
      facet <- mapAggregationToFacet(aggregation)
    } yield facet

  private def mapAggregationToFacet(aggregation: AggregationProto): Option[FacetGroup] = aggregation.name match {
    case Aggregation.MODEL =>
      Some(
        FacetGroup(
          name = "model",
          facets = aggregation.buckets
            .map(v =>
              Facet(
                value = v._1,
                count = v._2,
                // The filter parameter for models is not valid on this API,
                // the dispatcher returns filter.model=[sound|person|set],
                // but the this API doesn't allow filtering through content type via parameter,
                // plus it shouldn't be the responsibility of the client or the value added service
                // to indicate how to filter via parameters should be used in a query
                // therefore I'm removing them
                filter = ""
              )
            )
            .toSeq
        )
      )
    case _ => None
  }

  private def doSearch(req: SimpleSearchRequest, limit: Int): OutcomeF[SearchResponse] =
    client
      .simpleSearch(req)
      .map(res =>
        SearchResponse(
          query = res.query.map(_.text).getOrElse(""),
          query_urn = extractQueryUrn(res).getOrElse(Urn("soundcloud", "search", "unassigned")),
          offset = res.query.flatMap(_.pagination.flatMap(_.offset)).getOrElse(0),
          limit = req.pagination.flatMap(_.limit).getOrElse(limit),
          total_results = res.stats.map(_.totalResults).getOrElse(0L),
          query_time_in_millis = res.stats.map(_.executionTimeMs.toInt).getOrElse(0),
          docs = extractResults(res),
          facets = Option(extractFacets(res)).filter(_.nonEmpty)
        )
      )
      .liftToTry
      .map {
        case Throw(e: TwinagleException) if e.code == ErrorCode.InvalidArgument =>
          exceptionCollector.add(e, Severity.Warning, collectRequestBody = true)
          HttpServiceError(HttpResponseFields(Status.BadRequest.code, Some(e.getMessage))).bad
        case Throw(e) =>
          exceptionCollector.add(e, Severity.Error, collectRequestBody = true)
          HttpServiceError(HttpResponseFields(Status.InternalServerError.code, Some(e.getMessage))).bad
        case Return(r) => r.good
      }
      .outcomeF
}
