package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.module.rollout.RolloutFeature
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.publicApiStrangler.handler._
import com.soundcloud.publicApiStrangler.mapper.search.{SearchMapper, SearchRepository}
import com.soundcloud.publicApiStrangler.mapper.similarsounds.SimilarSoundsMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.e1.{ActivitiesMapper, StreamMapper}
import com.soundcloud.publicApiStrangler.mapper.timeline.publicApi.ActivitiesWithOriginMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper, FollowingsTracksMapper}
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{
  TrackStreamJsonResponseMapper,
  TrackStreamRedirectResponseMapper
}
import com.soundcloud.publicApiStrangler.service.media.DownloadService
import com.soundcloud.publicApiStrangler.service.oauth.AuthorizationService
import com.soundcloud.publicApiStrangler.support.CursorPagination
import com.soundcloud.publicApiStrangler.support.oauth.{RailsLikeParamsParser, TokenExchangeRequestParser}

class Handlers(telemetry: Telemetry, clients: Clients, exceptionCollector: ExceptionCollector) {
  import clients._
  val mothershipDispatcher = new DispatchToMothershipHandler(userAuthentication, publicApiClient)

  val timelineHandler: TimelineHandler = {
    val entitySummaryMapper = new EntitySummaryMapper(okidokiClient, repostsClient, baseUrl)
    val entityMapper = new EntityMapper(
      okidokiClient,
      lieblingClient,
      followCountsClient,
      repostsClient,
      baseUrl,
      entitySummaryMapper
    )
    val streamMapper = new StreamMapper(timelineClient, entityMapper, entitySummaryMapper)
    val activitiesMapper = new ActivitiesMapper(timelineClient, entityMapper, entitySummaryMapper)
    val publicActivitiesMapper = new ActivitiesWithOriginMapper(timelineClient, entityMapper, entitySummaryMapper)
    val followingsTracksMapper = new FollowingsTracksMapper(timelineClient, entityMapper, entitySummaryMapper)
    val pagination = new CursorPagination(baseUrl)
    new TimelineHandler(
      userAuthentication,
      streamMapper,
      activitiesMapper,
      publicActivitiesMapper,
      followingsTracksMapper,
      pagination
    )
  }

  val trackStreamsHandler: TrackStreamsHandler = {
    val trackStreamUrlToJsonResponseMapper = new TrackStreamJsonResponseMapper
    val trackStreamUrlToRedirectMapper = new TrackStreamRedirectResponseMapper

    new TrackStreamsHandler(
      userAuthentication,
      trackStreamUrlToJsonResponseMapper,
      trackStreamUrlToRedirectMapper,
      streamService,
      trackAccessRecorderService
    )
  }

  val trackDownloadHandler: TrackDownloadHandler = new TrackDownloadHandler(
    userAuthentication,
    new DownloadService(tracksClient)
  )

  val tracksHandler = new TracksHandler(
    userAuthentication,
    trackCoordinatorClient,
    okidokiClient,
    mothershipDispatcher,
    trackmetadataClient
  )

  val singleTrackHandler =
    new SingleTrackHandler(userAuthentication, tracksService, legacyTracksService, telemetry, exceptionCollector)

  val trackMothershipDispatcherWithCounts =
    new TrackMothershipDispatcherWithCounts(userAuthentication, mothershipDispatcher, stitchClient)

  val userRelatedMothershipDispatcher = new UserRelatedMothershipDispatcher(
    userAuthentication,
    mothershipDispatcher,
    followCountsClient,
    lieblingClient,
    enrichLikesCounts,
    repostsClient
  )

  val userTracksHandler = new UserTracksHandler(
    userAuthentication,
    legacyTracksService,
    telemetry,
    baseUrl
  )

  val userFollowHandler =
    new UserFollowHandler(userAuthentication, okidokiClient, followsClient, followCountsClient, repostsClient, baseUrl)

  val searchHandler: SearchHandler = {
    val searchRepository = new SearchRepository(searchService)
    val searchMapper = new SearchMapper(searchRepository, searchEntityMapper, baseUrl)
    val mothershipCounter = telemetry.counter(
      "search_mothership_fallback_total",
      "Number of requests to search endpoints with missing/invalid query parameters that get propagated to Mothership",
      "path"
    )

    new SearchHandler(
      userAuthentication,
      mothershipDispatcher,
      mothershipCounter,
      followCountsClient,
      searchMapper,
      baseUrl,
      lieblingClient,
      userRelatedMothershipDispatcher,
      trackMothershipDispatcherWithCounts
    )
  }

  val similarSoundsHandler: SimilarSoundsHandler = {
    val similarSoundsMapper = new SimilarSoundsMapper(systemPlaylistsClient, searchEntityMapper)
    new SimilarSoundsHandler(
      userAuthentication,
      similarSoundsMapper,
      baseUrl
    )
  }

  val repostersHandler = new RepostersHandler(
    userAuthentication,
    repostsClient,
    richOkidokiClient,
    followCountsClient,
    lieblingClient,
    enrichLikesCounts
  )

  val playlistsHandler = new PlaylistsHandler(userAuthentication, playlistDeletionClient)

  val repostsHandler = new RepostsHandler(userAuthentication, repostsClient)

  val tokenExchangeRequestMapper = new TokenExchangeRequestParser(new RailsLikeParamsParser())
  val tokenExchangeHandler =
    new TokenExchangeHandler(
      mothershipDispatcher.dispatchUnauthenticated,
      telemetry,
      tokenExchangeRequestMapper.parse,
      new AuthorizationService(clients.authorizationClient, exceptionCollector)
    )
  val instrumentTokenExchangeRequest = RolloutFeature("instrument_token_exchange_requests")

  val tokenExchangeRolloutHandler = new RolloutHandler(
    () => rolloutClient.isActive(instrumentTokenExchangeRequest),
    mothershipDispatcher.dispatchUnauthenticated,
    tokenExchangeHandler.instrumentedMothershipDispatch
  )
}
