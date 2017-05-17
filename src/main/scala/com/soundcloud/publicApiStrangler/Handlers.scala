package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.module.rollout.BasicRolloutFeature
import com.soundcloud.publicApiStrangler.authorization.PublicApiSiloing
import com.soundcloud.publicApiStrangler.handler._
import com.soundcloud.publicApiStrangler.mapper.search.{SearchMapper, SearchRepository}
import com.soundcloud.publicApiStrangler.mapper.similarsounds.SimilarSoundsMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.e1.{ActivitiesMapper, StreamMapper}
import com.soundcloud.publicApiStrangler.mapper.timeline.publicApi.ActivitiesWithOriginMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper, FollowingsTracksMapper}
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{TrackStreamJsonResponseMapper, TrackStreamRedirectResponseMapper}
import com.soundcloud.publicApiStrangler.media.MediaUrlsRepository
import com.soundcloud.publicApiStrangler.support.{CursorPagination, DispatchToMothershipHandler, TrackStreamHandler}

trait Handlers extends Clients {

  val mothershipDispatcher = new DispatchToMothershipHandler(publicApiClient)

  val timelineHandler = {
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

  val trackStreamsHandler = {
    val trackStreamUrlToJsonResponseMapper = new TrackStreamJsonResponseMapper
    val trackStreamUrlToRedirectMapper = new TrackStreamRedirectResponseMapper

    val mediaUrlsRepository = new MediaUrlsRepository(mediaService)
    val trackStreamSnipHandler = new TrackStreamHandler(mothershipDispatcher, contentAuthorizationRules, mediaUrlsRepository)
    val rolloutCheckForSiloingFunc = {
      val siloingEnabledFeature = BasicRolloutFeature("app-siloing-enabled")
      () => rolloutClient.isActive(siloingEnabledFeature)
    }
    val publicApiSiloing = new PublicApiSiloing(rolloutCheckForSiloingFunc, blacklistOfAppIdsForUserSiloing, moduleTelemetry)

    new TrackStreamsHandler(
      userAuthentication,
      trackStreamUrlToJsonResponseMapper,
      trackStreamUrlToRedirectMapper,
      trackStreamSnipHandler,
      publicApiSiloing
    )
  }

  val tracksHandler = new TracksHandler(userAuthentication,
    trackCoordinatorClient,
    okidokiClient,
    mothershipDispatcher,
    trackmetadataClient)

  val singleTrackHandler = new SingleTrackHandler(userAuthentication, tracksService, moduleTelemetry)

  val trackMothershipDispatcherWithCounts = new TrackMothershipDispatcherWithCounts(userAuthentication, mothershipDispatcher, stitchClient)

  val userRelatedMothershipDispatcher = new UserRelatedMothershipDispatcher(
    userAuthentication,
    mothershipDispatcher,
    followCountsClient,
    lieblingClient,
    enrichLikesCounts,
    repostsClient
  )

  val userTracksHandler = {
    val trackMothershipDispatcherWithCounts = new TrackMothershipDispatcherWithCounts(
      userAuthentication,
      mothershipDispatcher,
      stitchClient)

    val shouldUseTrackMetadata = BasicRolloutFeature("track_metadata_for_user_tracks")

    new UserTracksHandler(
      userAuthentication,
      trackMothershipDispatcherWithCounts,
      tracksService,
      moduleTelemetry,
      () => rolloutClient.isActive(shouldUseTrackMetadata),
      baseUrl
    )
  }

  val userFollowHandler = new UserFollowHandler(userAuthentication, okidokiClient, followsClient, followCountsClient, repostsClient, baseUrl)

  val searchHandler = {
    val searchRepository = new SearchRepository(searchService)
    val searchMapper = new SearchMapper(searchRepository, searchEntityMapper, baseUrl)
    val mothershipCounter = moduleTelemetry.counter(
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

  val similarSoundsHandler = {
    val similarSoundsMapper = new SimilarSoundsMapper(similarSoundsClient, searchEntityMapper)
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

  val spamWarningsHandler = new SpamWarningsHandler(userAuthentication, sketchyClient)
}
