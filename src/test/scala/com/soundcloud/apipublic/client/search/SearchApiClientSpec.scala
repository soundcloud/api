package com.soundcloud.apipublic.client.search

import com.google.protobuf.timestamp.Timestamp
import com.soundcloud.apipublic.handler.support.requestParser.{AccessParams, AccessParamsExtractor}
import com.soundcloud.jvmkit.module.outcome.{ApplicationError, HttpResponseFields, HttpServiceError}
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps.JvmkitSessionExt
import com.soundcloud.jvmkit.module.util.session.{AnonymousUserSession, UserSessionBuilder}
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.finagle.http.{ParamMap, Status}
import com.twitter.util.{Await, Future}
import proto.soundcloud.search.api.SearchFilters.{BPMDynamicRange, CreatedAtDynamicRange, CreatedAtRange, DurationRange}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import proto.soundcloud.search.api.{
  Aggregation,
  QueryStats,
  SearchClientProtobuf,
  SearchFilters,
  SearchOffsetPagination,
  SimpleQueryResponse,
  SimpleSearchRequest,
  SimpleSearchResponse,
  Entity => ProtoEntity,
  UnresolvedEntity => ProtoUnresolvedEntity
}

class SearchApiClientSpec extends Specification with Mockito {
  import com.soundcloud.apipublic.handler.search.ParamsExtractor._

  trait Context extends Scope {
    val mockClient = smartMock[SearchClientProtobuf]
    val mockExceptionCollector: ExceptionCollector = smartMock[ExceptionCollector]
    val client = new SearchApiClient(mockClient, mockExceptionCollector)
    val userSession = (new UserSessionBuilder).build.asInstanceOf[AnonymousUserSession]

    lazy val access: AccessParams = AccessParamsExtractor.unapply(rawParams)
    lazy val query = "foo"
    lazy val queryUrn = Urn("soundcloud", "search", "1234")
    lazy val rawParams = ParamMap("q" -> "foo", "limit" -> limit.get.toString, "offset" -> offset.get.toString)
    lazy val executionTime = 1000
    lazy val pagination: Option[SearchOffsetPagination] = Some(SearchOffsetPagination(offset = offset, limit = limit))
    lazy val offset: Option[Int] = Some(0)
    lazy val limit: Option[Int] = Some(1)

    lazy val expected = SearchResponse(
      query = query,
      query_urn = queryUrn,
      offset = offset.get,
      limit = limit.get,
      total_results = 0,
      query_time_in_millis = executionTime,
      docs = Seq.empty,
      facets = None
    )

    protected def mapUnresolvedEntity(unresolvedEntity: Urn): ProtoEntity = {
      ProtoEntity(
        ProtoEntity.Entity.UnresolvedEntity(
          ProtoUnresolvedEntity(
            urn = unresolvedEntity.toString
          )
        )
      )
    }

    lazy val results: Seq[ProtoEntity] = Seq.empty

    lazy val response: Future[SimpleSearchResponse] = Future.value(
      SimpleSearchResponse(
        stats = Some(
          QueryStats(
            executionTimeMs = executionTime,
            totalResults = results.size
          )
        ),
        results = results,
        query = Some(
          SimpleQueryResponse(
            queryUrn = Some(queryUrn.toString),
            text = query,
            pagination = pagination
          )
        ),
        aggregations = responseAggregates
      )
    )

    lazy val filters: SearchFilters = SearchFilters()

    lazy val aggregations: Seq[Aggregation.Aggregation] = Seq.empty

    lazy val responseAggregates: Seq[Aggregation] = Seq.empty

    lazy val request: SimpleSearchRequest = SimpleSearchRequest(
      userSession = Some(userSession.asProtoSession),
      anonymousId = Some(any[String]()),
      text = query,
      pagination = pagination,
      filters = Some(filters),
      aggregations = aggregations
    )

    mockClient.simpleSearch(request) returns response
  }

  trait TracksContext extends Context {
    override lazy val filters: SearchFilters = SearchFilters(
      contentType = SearchFilters.ContentType.TRACKS
    )

    lazy val result: Either[ApplicationError, SearchResponse] =
      Await.result(
        client.searchTracks(session = userSession, params = rawParams.asTracksParams, access = access).value
      )
  }

  trait PlaylistsContext extends Context {
    override lazy val filters: SearchFilters = SearchFilters(
      contentType = SearchFilters.ContentType.PLAYLISTS
    )

    lazy val result: Either[ApplicationError, SearchResponse] = Await.result(
      client.searchPlaylists(session = userSession, params = rawParams.asPlaylistParams, access = access).value
    )
  }

  trait UsersContext extends Context {
    override lazy val filters: SearchFilters = SearchFilters(
      contentType = SearchFilters.ContentType.USERS
    )

    lazy val result: Either[ApplicationError, SearchResponse] = Await.result(
      client.searchUsers(session = userSession, params = rawParams.asUsersParams, access = access).value
    )
  }

  "tracks" >> {
    "should successfully search tracks" in new TracksContext {
      Right(expected) === result
    }

    "should filter tracks by multiple genres" in new TracksContext {
      override lazy val rawParams: ParamMap = ParamMap(
        ("q", "foo"),
        ("limit", limit.get.toString),
        ("offset", offset.get.toString),
        ("genres", "hello,world")
      )

      override lazy val filters: SearchFilters = SearchFilters(
        contentType = SearchFilters.ContentType.TRACKS,
        genres = Seq("hello", "world")
      )

      Right(expected) === result
    }

    "should filter tracks by bpm" in new TracksContext {
      override lazy val rawParams: ParamMap = ParamMap(
        ("q", "foo"),
        ("limit", limit.get.toString),
        ("offset", offset.get.toString),
        ("bpm[from]", "10"),
        ("bpm[to]", "20")
      )

      override lazy val filters: SearchFilters = SearchFilters(
        contentType = SearchFilters.ContentType.TRACKS,
        bpm = Some(BPMDynamicRange(Some(10), Some(20)))
      )

      Right(expected) === result
    }

    "should filter tracks with tags" in new TracksContext {
      override lazy val rawParams: ParamMap = ParamMap(
        ("q", "foo"),
        ("limit", limit.get.toString),
        ("offset", offset.get.toString),
        ("tags", "hello,world")
      )

      override lazy val filters: SearchFilters = SearchFilters(
        contentType = SearchFilters.ContentType.TRACKS,
        hashtags = Seq("hello", "world")
      )

      Right(expected) === result
    }

    "should filter tracks with created_at" in new TracksContext {
      override lazy val rawParams: ParamMap = ParamMap(
        ("q", "foo"),
        ("limit", limit.get.toString),
        ("offset", offset.get.toString),
        ("created_at[from]", "2020-12-24 00:00:00"),
        ("created_at[to]", "2020-12-26 00:00:00")
      )
      override lazy val filters: SearchFilters = SearchFilters(
        contentType = SearchFilters.ContentType.TRACKS,
        createdAtRange = Some(
          CreatedAtRange().withDynamicRange(
            CreatedAtDynamicRange(
              from = Some(Timestamp(1608768000)),
              to = Some(Timestamp(1608940800))
            )
          )
        ).asInstanceOf[Option[SearchFilters.CreatedAtRange]]
      )

      Right(expected) === result
    }

    "should filter tracks with custom duration" in new TracksContext {
      override lazy val rawParams: ParamMap = ParamMap(
        ("q", "foo"),
        ("limit", limit.get.toString),
        ("offset", offset.get.toString),
        ("duration[from]", "20"),
        ("duration[to]", "30")
      )
      override lazy val filters: SearchFilters = SearchFilters(
        contentType = SearchFilters.ContentType.TRACKS,
        durationRange = Some(DurationRange().withDynamicRange(SearchFilters.DurationDynamicRange(Some(20), Some(30))))
      )

      Right(expected) === result
    }

    "should filter tracks with fixed duration" in new TracksContext {
      override lazy val rawParams: ParamMap = ParamMap(
        ("q", "foo"),
        ("limit", limit.get.toString),
        ("offset", offset.get.toString),
        ("duration", "SHORT")
      )
      override lazy val filters: SearchFilters = SearchFilters(
        contentType = SearchFilters.ContentType.TRACKS,
        durationRange = Some(DurationRange().withFixedRange(SearchFilters.DurationFixedRange.SHORT_DURATION))
      )

      Right(expected) === result
    }

    "should filter tracks with ids" in new TracksContext {
      override lazy val rawParams: ParamMap = ParamMap(
        ("q", "foo"),
        ("limit", limit.get.toString),
        ("offset", offset.get.toString),
        ("ids", "1,2,3")
      )
      override lazy val filters: SearchFilters = SearchFilters(
        contentType = SearchFilters.ContentType.TRACKS,
        ids = Seq("1", "2", "3")
      )

      Right(expected) === result
    }
  }

  "playlists" >> {
    "should search playlists" in new PlaylistsContext {
      Right(expected) === result
    }

    "should filter playlists by multiple genres" in new PlaylistsContext {
      override lazy val rawParams: ParamMap = ParamMap(
        ("q", "foo"),
        ("limit", limit.get.toString),
        ("offset", offset.get.toString),
        ("genres", "hello,world")
      )

      override lazy val filters: SearchFilters = SearchFilters(
        contentType = SearchFilters.ContentType.PLAYLISTS,
        genres = Seq("hello", "world")
      )

      Right(expected) === result
    }

    "should filter playlists with tags" in new PlaylistsContext {
      override lazy val rawParams: ParamMap = ParamMap(
        ("q", "foo"),
        ("limit", limit.get.toString),
        ("offset", offset.get.toString),
        ("tags", "hello,world")
      )

      override lazy val filters: SearchFilters = SearchFilters(
        contentType = SearchFilters.ContentType.PLAYLISTS,
        hashtags = Seq("hello", "world")
      )

      Right(expected) === result
    }

  }

  "users" >> {
    "should search users" in new UsersContext {
      Right(expected) === result
    }

    "should fail with BadRequest when client returns invalid argument" in new UsersContext {
      override lazy val response: Future[SimpleSearchResponse] = Future.exception(
        TwinagleException(ErrorCode.InvalidArgument, "some error")
      )

      result must beLeft(HttpServiceError(HttpResponseFields(Status.BadRequest.code, Some("some error"))))
    }

    "should fail with InternalError when client returns an unhandled error" in new UsersContext {
      override lazy val response: Future[SimpleSearchResponse] = Future.exception(
        TwinagleException(ErrorCode.Aborted, "some error")
      )

      result must beLeft(HttpServiceError(HttpResponseFields(Status.InternalServerError.code, Some("some error"))))
    }

    "should filter users with ids" in new UsersContext {
      override lazy val rawParams: ParamMap = ParamMap(
        ("q", "foo"),
        ("limit", limit.get.toString),
        ("offset", offset.get.toString),
        ("ids", "1,2,3")
      )

      override lazy val filters: SearchFilters = SearchFilters(
        contentType = SearchFilters.ContentType.USERS,
        ids = Seq("1", "2", "3")
      )

      Right(expected) === result
    }
  }
}
