package com.soundcloud.publicApiStrangler

import com.soundcloud.bff._
import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.media.{MediaUrlsRepository, WaveformUrlsRepository}
import com.soundcloud.bff.services.JsonService
import com.soundcloud.follows.FollowsComponent
import com.soundcloud.jvmkit.config.ConfigConvention
import com.soundcloud.publicApiStrangler.authorization.{AuthorizeHttpResponse, ContentAuthorizationFilter}
import com.soundcloud.publicApiStrangler.controller._
import com.soundcloud.publicApiStrangler.features.{RolloutBuilder, RolloutController}
import com.soundcloud.publicApiStrangler.headers.DefaultResponseHeadersFilter
import com.soundcloud.publicApiStrangler.mapper.liebling.LikeCountMapper
import com.soundcloud.publicApiStrangler.mapper.purchaselink.TrackPurchaseLinkMapper
import com.soundcloud.publicApiStrangler.mapper.search.{PlaylistTracksMapper, SearchEntityMapper, SearchMapper, SearchRepository}
import com.soundcloud.publicApiStrangler.mapper.similarsounds.SimilarSoundsMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.e1.{ActivitiesMapper, StreamMapper}
import com.soundcloud.publicApiStrangler.mapper.timeline.publicApi.ActivitiesWithOriginMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper}
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{TrackStreamJsonResponseMapper, TrackStreamRedirectResponseMapper}
import com.soundcloud.publicApiStrangler.mapper.waveform.WaveformMapper
import com.soundcloud.publicApiStrangler.rateLimiting._
import com.soundcloud.publicApiStrangler.support._
import com.soundcloud.publicApiStrangler.zookeeper.ZookeeperClientFactory
import com.soundcloud.ratelimiting.whitelisting.{WhitelistZookeeperPath, ApplicationLevelWhitelistProxy}
import com.soundcloud.scalakit.ResourceName
import com.soundcloud.service.component._
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.filter.ExceptionFilter

object App
  extends BffInjectionBasedApp
  with BazookaConfigComponent
  with AuthenticatorComponent
  with OkidokiComponent
  with TimelineComponent
  with LieblingComponent
  with PublicApiClientComponent
  with FollowsComponent
  with GatekeeperComponent
  with SimilarSoundsComponent {

  private val userAuthentication = createUserAuthentication

  private val okidokiService = JsonService(
    ServiceConfig("okidoki", config.get(ResourceName("OKIDOKI"), ConfigConvention.SRV_RECORD), config)
  )
  private val mediaService = JsonService(
    ServiceConfig("mediaservice", config.get(ResourceName("MEDIASERVICE"), ConfigConvention.SRV_RECORD), config)
  )
  private val authsyService = JsonService(
    ServiceConfig("authsy", config.get(ResourceName("AUTHSY"), ConfigConvention.SRV_RECORD), config)
  )

  private val searchService = JsonService(
    ServiceConfig("search", config.get(ResourceName("SEARCH"), ConfigConvention.SRV_RECORD), config)
  )

  private val contentAuthorizationService = new ContentAuthorizationService(authsyService)

  private val waveformUrlsRepo = new WaveformUrlsRepository(okidokiService, mediaService)
  private val authorizeContent = new AuthorizeHttpResponse(contentAuthorizationService, userAuthentication, waveformUrlsRepo)

  private val mothershipDispatcher = new DispatchToMothershipHandler(publicApiClient)

  private val baseUrl = config.get("APP_BASE_URL", true)

  private val timelineController = {
    val entitySummaryMapper = new EntitySummaryMapper(okidokiClient, baseUrl)
    val entityMapper = new EntityMapper(okidokiClient, lieblingClient, baseUrl, entitySummaryMapper)
    val streamMapper = new StreamMapper(timelineClient, entityMapper, entitySummaryMapper)
    val activitiesMapper = new ActivitiesMapper(timelineClient, entityMapper, entitySummaryMapper)
    val publicActivitiesMapper = new ActivitiesWithOriginMapper(timelineClient, entityMapper, entitySummaryMapper)
    val pagination = new CursorPagination(baseUrl)
    new TimelineController(userAuthentication, streamMapper, activitiesMapper, publicActivitiesMapper, pagination)
  }

  private val zookeeperClientFactory = new ZookeeperClientFactory
  private val zookeeperClient = zookeeperClientFactory.create(config)

  private val whitelistingProxy = new ApplicationLevelWhitelistProxy(zookeeperClient, WhitelistZookeeperPath.Base / config.getApplicationName)

  private val prometheusLabelsSafeGuard = new PrometheusLabelsSafeGuard(cache, config, config.getApplicationResourceName)
  private val rateLimitMetrics = new RateLimitMetrics(prometheusLabelsSafeGuard, config)
  private val rateLimitEventListeners = new TelemetryRateLimitEventListener(rateLimitMetrics) :: Nil

  private val rateLimiter = RateLimiter.from(cache, config, rateLimitEventListeners)

  private val groupController = {
    val forwardHandler = new ForwardRequestHandler(publicApiClient)
    new GroupController(userAuthentication, gatekeeperClient, forwardHandler)
  }

  private val trackStreamsController = {
    val trackStreamUrlToJsonResponseMapper = new TrackStreamJsonResponseMapper
    val trackStreamUrlToRedirectMapper = new TrackStreamRedirectResponseMapper

    val mediaUrlsRepository = new MediaUrlsRepository(okidokiService, mediaService)
    val trackStreamSnipHandler = new TrackStreamSnipHandler(mothershipDispatcher, contentAuthorizationService, mediaUrlsRepository)
    new TrackStreamsController(
      userAuthentication,
      trackStreamUrlToJsonResponseMapper,
      trackStreamUrlToRedirectMapper,
      mothershipDispatcher,
      trackStreamSnipHandler)
  }

  val rollout = RolloutBuilder.build(zookeeperClient, config.getApplicationName)
  private val rolloutController = new RolloutController(rollout)

  private val userFollowController = new UserFollowController(
    userAuthentication,
    mothershipDispatcher,
    okidokiClient,
    followsClient,
    baseUrl,
    rollout
  )

  private val searchController = {
    val baseUrl = config.get("APP_BASE_URL", true)
    val waveformUrlsRepo = new WaveformUrlsRepository(okidokiService, mediaService)
    val entityMapper = new SearchEntityMapper(
      okidokiClient,
      baseUrl,
      contentAuthorizationService,
      new WaveformMapper(waveformUrlsRepo),
      new TrackPurchaseLinkMapper(okidokiClient),
      new LikeCountMapper(lieblingClient),
      new PlaylistTracksMapper(okidokiClient, baseUrl),
      new EntitySummaryMapper(okidokiClient, baseUrl)
    )
    val searchRepository = new SearchRepository(searchService)
    val searchMapper = new SearchMapper(searchRepository, entityMapper, baseUrl)
    new SearchController(userAuthentication, searchMapper, baseUrl, rollout, mothershipDispatcher)
  }

  private val similarSoundsController = {
    val baseUrl = config.get("APP_BASE_URL", true)
    val waveformUrlsRepo = new WaveformUrlsRepository(okidokiService, mediaService)
    val searchEntityMapper = new SearchEntityMapper(
      okidokiClient,
      baseUrl,
      contentAuthorizationService,
      new WaveformMapper(waveformUrlsRepo),
      new TrackPurchaseLinkMapper(okidokiClient),
      new LikeCountMapper(lieblingClient),
      new PlaylistTracksMapper(okidokiClient, baseUrl),
      new EntitySummaryMapper(okidokiClient, baseUrl)
    )
    val similarSoundsMapper = new SimilarSoundsMapper(similarSoundsClient, searchEntityMapper)

    new SimilarSoundsController(
      userAuthentication,
      similarSoundsMapper,
      baseUrl,
      rollout,
      mothershipDispatcher
    )
  }

  override val fallbackHandler = Some(mothershipDispatcher)

  override lazy val additionalFilters = List(
    new ExceptionFilter[Request],
    new AcceptOnlyJsonRequestFilter(Set("/crossdomain.xml")),
    new ContentAuthorizationFilter(authorizeContent),
    new DefaultResponseHeadersFilter
  )

  override val controllers = Set(
    timelineController,
    rolloutController,
    groupController,
    trackStreamsController,
    userFollowController,
    searchController,
    similarSoundsController
  )
}
