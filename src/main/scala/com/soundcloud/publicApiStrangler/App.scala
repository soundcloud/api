package com.soundcloud.publicApiStrangler

import com.soundcloud.bff._
import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.media.{MediaUrlsRepository, WaveformUrlsRepository}
import com.soundcloud.bff.services.JsonService
import com.soundcloud.follows.FollowsComponent
import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.admin.{AdminRoute, RequestMethod}
import com.soundcloud.jvmkit.config.ConfigConvention
import com.soundcloud.jvmkit.rollout.RolloutBuilder
import com.soundcloud.publicApiStrangler.authorization.{TrackPolicyApplicator, AuthorizeHttpResponse, ContentAuthorizationFilter}
import com.soundcloud.publicApiStrangler.controller._
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
import com.soundcloud.publicApiStrangler.support._
import com.soundcloud.publicApiStrangler.zookeeper.CuratorFrameworkFactory
import com.soundcloud.ratelimiting.facade._
import com.soundcloud.scalakit.Urn
import com.soundcloud.service.component._
import com.twitter.finagle.CancelledRequestException
import com.twitter.finagle.http.Response
import org.eclipse.jetty.server.Handler
import org.jboss.netty.handler.codec.http.{HttpResponseStatus, HttpVersion}
import com.soundcloud.trackcoordinator.client.TrackCoordinatorComponent

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
  with SimilarSoundsComponent
  with TrackCoordinatorComponent {

  private val bffApplication = BffApplication(Urn("soundcloud", "systems", "public-api-strangler"), config.getApplicationResourceName)

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

  // Whitelist source: http://redash.int.s-cloud.net/queries/632/source
  private val whitelistedClients: Set[Urn] = Set(
    "soundcloud:applications:124",    // SoundCloud iOS
    "soundcloud:applications:3152",   // SoundCloud Android
    "soundcloud:applications:3273",   // Mobile Soundcloud
    "soundcloud:applications:3537",   // SoundCloud Desktop
    "soundcloud:applications:43164",  // SoundCloud Player Widget
    "soundcloud:applications:46941",  // SoundCloud.com
    "soundcloud:applications:60973",  // SoundCloud Flash Widget
    "soundcloud:applications:65097",  // MobileWeb3
    "soundcloud:applications:66151",  // MobileWeb production
    "soundcloud:applications:90575",  // SoundCloud Visual Embed Player
    "soundcloud:applications:99561",  // SoundCloud Kik Messenger Card
    "soundcloud:applications:120502", // Twitter Partner
    "soundcloud:applications:135495", // Mobile Web App
    "soundcloud:applications:167582"  // HEOS by Denon (Production)
  ).map(new Urn(_))
  private val authorizeContent = new AuthorizeHttpResponse(contentAuthorizationService, userAuthentication, waveformUrlsRepo, TrackPolicyApplicator(whitelistedClients))

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

  private val curatorFrameworkFactory = new CuratorFrameworkFactory
  private val curatorFramework = curatorFrameworkFactory.create(config)

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

  private val tracksController = new TracksController(userAuthentication,
                                                      trackCoordinatorClient,
                                                      okidokiClient,
                                                      mothershipDispatcher)

  lazy val rolloutClient = new RolloutBuilder(config, telemetry).build

  private val rateLimitingFacade = new RateLimitingFacade(
    bffApplication,
    curatorFramework,
    userAuthentication,
    config,
    telemetry,
    memcachedClient,
    rolloutClient
  )

  private val userFollowController = new UserFollowController(
    userAuthentication,
    mothershipDispatcher,
    okidokiClient,
    followsClient,
    baseUrl,
    rolloutClient
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
    new SearchController(userAuthentication, searchMapper, baseUrl, rolloutClient, mothershipDispatcher)
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
      baseUrl
    )
  }

  override val fallbackHandler = Some(mothershipDispatcher)

  override def exceptionHandler: PartialFunction[Throwable, Response] = {
    case ex: CancelledRequestException =>
      Response(HttpVersion.HTTP_1_1, new HttpResponseStatus(499, "Client Closed Request"))
  }

  override lazy val additionalFilters = List(
    new AcceptOnlyJsonRequestFilter,
    new ContentAuthorizationFilter(authorizeContent),
    rateLimitingFacade.filter,
    new DefaultResponseHeadersFilter,
    new StaticFilesFilter
  )

  override val controllers = Set(
    timelineController,
    trackStreamsController,
    userFollowController,
    searchController,
    similarSoundsController,
    rateLimitingFacade.rateLimitStatusController,
    tracksController
  )

  override val customAdminHandlers: Seq[(AdminRoute, Handler)] = Seq(
    new AdminRoute(RequestMethod.GET, "/-/rate-limiting-diagnostics") ->
      rateLimitingFacade.rateLimitingDiagnosticsAdminHandler
  )
}
