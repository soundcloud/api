package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.module.rollout.BasicRolloutFeature
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.authorization.PublicApiSiloing
import com.soundcloud.publicApiStrangler.handler._
import com.soundcloud.publicApiStrangler.mapper.search.{SearchMapper, SearchRepository}
import com.soundcloud.publicApiStrangler.mapper.similarsounds.SimilarSoundsMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.e1.{ActivitiesMapper, StreamMapper}
import com.soundcloud.publicApiStrangler.mapper.timeline.publicApi.ActivitiesWithOriginMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper, FollowingsTracksMapper}
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{TrackStreamJsonResponseMapper, TrackStreamRedirectResponseMapper}
import com.soundcloud.publicApiStrangler.service.media.{DownloadService, StreamService}
import com.soundcloud.publicApiStrangler.support.CursorPagination

class Handlers(telemetry: Telemetry, clients: Clients) {
  import clients._

  val mothershipDispatcher = new DispatchToMothershipHandler(publicApiClient)

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
    new TimelineHandler(userAuthentication, streamMapper, activitiesMapper, publicActivitiesMapper, followingsTracksMapper, pagination)
  }

  val trackStreamsHandler: TrackStreamsHandler = {
    val trackStreamUrlToJsonResponseMapper = new TrackStreamJsonResponseMapper
    val trackStreamUrlToRedirectMapper = new TrackStreamRedirectResponseMapper

    val rolloutCheckForSiloingFunc = {
      val siloingEnabledFeature = BasicRolloutFeature("app-siloing-enabled")
      () => rolloutClient.isActive(siloingEnabledFeature)
    }
    val publicApiSiloing = new PublicApiSiloing(rolloutCheckForSiloingFunc, blacklistOfAppIdsForUserSiloing, telemetry)

    val streamService = new StreamService(tracksClient, mediaServiceClient)

    new TrackStreamsHandler(
      userAuthentication,
      trackStreamUrlToJsonResponseMapper,
      trackStreamUrlToRedirectMapper,
      streamService,
      trackAccessRecorderService,
      publicApiSiloing
    )
  }

  val trackDownloadHandler: TrackDownloadHandler = new TrackDownloadHandler(
    userAuthentication,
    trackAccessRecorderService,
    new DownloadService(tracksClient, mediaServiceClient))

  val tracksHandler = new TracksHandler(userAuthentication,
    trackCoordinatorClient,
    okidokiClient,
    mothershipDispatcher,
    trackmetadataClient)

  val singleTrackHandler = new SingleTrackHandler(userAuthentication, tracksService, telemetry)

  val trackMothershipDispatcherWithCounts = new TrackMothershipDispatcherWithCounts(userAuthentication, mothershipDispatcher, stitchClient)

  val userRelatedMothershipDispatcher = new UserRelatedMothershipDispatcher(
    userAuthentication,
    mothershipDispatcher,
    followCountsClient,
    lieblingClient,
    enrichLikesCounts,
    repostsClient
  )

  val userTracksHandler: UserTracksHandler = {
    val trackMothershipDispatcherWithCounts = new TrackMothershipDispatcherWithCounts(
      userAuthentication,
      mothershipDispatcher,
      stitchClient)

    val shouldUseTrackMetadata = BasicRolloutFeature("track_metadata_for_user_tracks")

    new UserTracksHandler(
      userAuthentication,
      trackMothershipDispatcherWithCounts,
      tracksService,
      telemetry,
      () => rolloutClient.isActive(shouldUseTrackMetadata),
      baseUrl
    )
  }

  val userFollowHandler = new UserFollowHandler(userAuthentication, okidokiClient, followsClient, followCountsClient, repostsClient, baseUrl)

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

  val repostersHandler = new RepostersHandler(userAuthentication,
    repostsClient,
    richOkidokiClient,
    followCountsClient,
    lieblingClient,
    enrichLikesCounts)

  val playlistsHandler = new PlaylistsHandler(userAuthentication, playlistDeletionClient)

  val repostsHandler = new RepostsHandler(userAuthentication, repostsClient)
}
