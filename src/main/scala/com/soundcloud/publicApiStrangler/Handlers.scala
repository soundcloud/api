package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.module.rollout.RolloutFeature
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.publicApiStrangler.handler._
import com.soundcloud.publicApiStrangler.mapper.search.{SearchMapper, SearchRepository}
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{
  TrackStreamJsonResponseMapper,
  TrackStreamRedirectResponseMapper
}
import com.soundcloud.publicApiStrangler.service.media.DownloadService
import com.soundcloud.publicApiStrangler.service.oauth.AuthorizationService
import com.soundcloud.publicApiStrangler.support.oauth.{RailsLikeParamsParser, TokenExchangeRequestParser}

class Handlers(telemetry: Telemetry, clients: Clients, exceptionCollector: ExceptionCollector) {
  import clients._
  val mothershipDispatcher = new DispatchToMothershipHandler(userAuthentication, publicApiClient)

  val timelineHandler: TimelineHandler = new TimelineHandler(
    userAuthentication,
    timelineService
  )

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
    trackUpdateService
  )

  val singleTrackHandler =
    new SingleTrackHandler(userAuthentication, tracksService)

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
    userTracksService
  )

  val userFollowHandler =
    new UserFollowHandler(userAuthentication, okidokiClient, followsClient, followCountsClient, repostsClient, baseUrl)

  val searchHandler: SearchHandler = {
    val searchRepository = new SearchRepository(searchJsonClient)
    val searchMapper = new SearchMapper(searchRepository, searchEntityMapper, baseUrl)
    new SearchHandler(
      userAuthentication,
      searchMapper,
      baseUrl,
      userRelatedMothershipDispatcher,
      searchService
    )
  }

  val similarTracksHandler: SimilarTracksHandler = {
    new SimilarTracksHandler(
      userAuthentication,
      similarTracksService,
      baseUrl
    )
  }

  val playlistsHandler =
    new PlaylistsHandler(userAuthentication, playlistDeletionClient, playlistService)

  val userPlaylistsHandler = new UserPlaylistsHandler(userAuthentication, userPlaylistsService)

  val repostsHandler = new RepostsHandler(userAuthentication, repostsClient)

  val likesHandler = new LikesHandler(userAuthentication, likesService, baseUrl)

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
