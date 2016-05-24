package com.soundcloud.publicApiStrangler

import com.soundcloud.bff._
import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.finagle.Request
import com.soundcloud.bff.media.{MediaUrlsRepository, WaveformUrlsRepository}
import com.soundcloud.bff.services.JsonService
import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.admin.{AdminRoute, RequestMethod}
import com.soundcloud.jvmkit.config.{ConfigConvention, DataSensitivity}
import com.soundcloud.jvmkit.rollout.{BasicRolloutFeature, RolloutBuilder}
import com.soundcloud.publicApiStrangler.authorization._
import com.soundcloud.publicApiStrangler.client._
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.controller.SearchController._
import com.soundcloud.publicApiStrangler.controller._
import com.soundcloud.publicApiStrangler.headers.DefaultResponseHeadersFilter
import com.soundcloud.publicApiStrangler.mapper.liebling.LikeCountMapper
import com.soundcloud.publicApiStrangler.mapper.purchaselink.TrackPurchaseLinkMapper
import com.soundcloud.publicApiStrangler.mapper.search.{PlaylistTracksMapper, SearchEntityMapper, SearchMapper, SearchRepository}
import com.soundcloud.publicApiStrangler.mapper.similarsounds.SimilarSoundsMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.e1.{ActivitiesMapper, StreamMapper}
import com.soundcloud.publicApiStrangler.mapper.timeline.publicApi.ActivitiesWithOriginMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper, FollowingsTracksMapper}
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{TrackStreamJsonResponseMapper, TrackStreamRedirectResponseMapper}
import com.soundcloud.publicApiStrangler.mapper.waveform.WaveformMapper
import com.soundcloud.publicApiStrangler.support._
import com.soundcloud.publicApiStrangler.zookeeper.CuratorFrameworkFactory
import com.soundcloud.ratelimiting.facade._
import com.soundcloud.ratelimiting.internal.core.RateLimitClassifier
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.cache.MemcachedClient
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, ServiceEntryPoint}
import org.eclipse.jetty.server.Handler

object App
  extends BffInjectionBasedApp
  with AppConfigComponent
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

  private val subscriptionsService = JsonService(
    ServiceConfig("user_subscriptions", config.get(ResourceName("USER_SUBSCRIPTIONS"), ConfigConvention.SRV_RECORD), config)
  )

  private val stitch4followsService = JsonService(
    ServiceConfig("stitch4follows", config.get(ResourceName("STITCH4FOLLOWS"), ConfigConvention.SRV_RECORD), config)
  )

  private val gobblyClient = new GobblyClient(
    JsonClient(
      ResourceName("gobbly"),
      ServiceEntryPoint(config.get(ResourceName("GOBBLY"), ConfigConvention.SRV_RECORD)),
      config,
      telemetry)
  )

  private val followCountsClient = new FollowCountsClient(stitch4followsService, config)

  private val contentAuthorizationRules = new ContentAuthorizationRules(
    new ContentAuthorizationService(authsyService),
    new SubscriptionsService(subscriptionsService))


  private val waveformUrlsRepo = new WaveformUrlsRepository(okidokiService, mediaService)

  // Whitelist source: http://redash.int.s-cloud.net/queries/632/source
  private val whitelistedClients: Set[Urn] = Set(
    "soundcloud:systems:soundcloud", // Agent returned by Authenticator for those with _soundcloud_session cookie
    "soundcloud:applications:124", // SoundCloud iOS
    "soundcloud:applications:3152", // SoundCloud Android
    "soundcloud:applications:3273", // Mobile Soundcloud
    "soundcloud:applications:3537", // SoundCloud Desktop
    "soundcloud:applications:43164", // SoundCloud Player Widget
    "soundcloud:applications:46941", // SoundCloud.com
    "soundcloud:applications:60973", // SoundCloud Flash Widget
    "soundcloud:applications:65097", // MobileWeb3
    "soundcloud:applications:66151", // MobileWeb production
    "soundcloud:applications:90575", // SoundCloud Visual Embed Player
    "soundcloud:applications:99561", // SoundCloud Kik Messenger Card
    "soundcloud:applications:120502", // Twitter Partner
    "soundcloud:applications:135495", // Mobile Web App
    "soundcloud:applications:167582", // HEOS by Denon (Production)

    // other whitelisted apps
    "soundcloud:applications:59007",
    "soundcloud:applications:62023",
    "soundcloud:applications:265616",
    "soundcloud:applications:265183"
  ).map(new Urn(_))
  private val authorizeContent = new AuthorizeHttpResponse(contentAuthorizationRules, userAuthentication, waveformUrlsRepo, TrackPolicyApplicator(whitelistedClients))

  private val mothershipDispatcher = new DispatchToMothershipHandler(publicApiClient)

  private val baseUrl = config.get("APP_BASE_URL", DataSensitivity.NON_SENSITIVE)

  private val timelineController = {
    val entitySummaryMapper = new EntitySummaryMapper(okidokiClient, baseUrl)
    val entityMapper = new EntityMapper(
      okidokiClient, lieblingClient, followCountsClient, baseUrl,
      entitySummaryMapper)
    val streamMapper = new StreamMapper(timelineClient, entityMapper, entitySummaryMapper)
    val activitiesMapper = new ActivitiesMapper(timelineClient, entityMapper, entitySummaryMapper)
    val publicActivitiesMapper = new ActivitiesWithOriginMapper(timelineClient, entityMapper, entitySummaryMapper)
    val followingsTracksMapper = new FollowingsTracksMapper(timelineClient, entityMapper, entitySummaryMapper)
    val pagination = new CursorPagination(baseUrl)
    new TimelineController(userAuthentication, streamMapper, activitiesMapper, publicActivitiesMapper, followingsTracksMapper, pagination)
  }

  private val curatorFrameworkFactory = new CuratorFrameworkFactory
  private val curatorFramework = curatorFrameworkFactory.create(config)

  private val trackStreamsController = {
    val trackStreamUrlToJsonResponseMapper = new TrackStreamJsonResponseMapper
    val trackStreamUrlToRedirectMapper = new TrackStreamRedirectResponseMapper

    val mediaUrlsRepository = new MediaUrlsRepository(mediaService)
    val trackStreamSnipHandler = new TrackStreamHandler(mothershipDispatcher, contentAuthorizationRules, mediaUrlsRepository)
    new TrackStreamsController(
      userAuthentication,
      trackStreamUrlToJsonResponseMapper,
      trackStreamUrlToRedirectMapper,
      trackStreamSnipHandler)
  }

  private val tracksController = new TracksController(userAuthentication,
    trackCoordinatorClient,
    okidokiClient,
    mothershipDispatcher,
    gobblyClient)

  lazy val rolloutClient = new RolloutBuilder(config, telemetry).build("public-api-strangler")
  override lazy val rollout = Some(rolloutClient)

  lazy val memcachedClient = MemcachedClient(config)

  private val searchParams = defaultParams ++ trackParams ++ playlistParams
  private val rateLimitZKBucket = "search"

  def searchRequests: RateLimitClassifier.rateLimitClassifier = {
    case req: Request if searchParams.find(x => req.params.contains(x)).isDefined => true
  }
  private val rateLimitingFacade = new RateLimitingFacade(
    bffApplication,
    curatorFramework,
    userAuthentication,
    config,
    telemetry,
    memcachedClient,
    rolloutClient,
    Some(new RateLimitClassifier(rateLimitZKBucket, searchRequests))
  )

  private val userFollowController = new UserFollowController(
    userAuthentication,
    mothershipDispatcher,
    okidokiClient,
    followsClient,
    followCountsClient,
    baseUrl
  )

  private val searchEntityMapper = new SearchEntityMapper(
    okidokiClient,
    followCountsClient,
    baseUrl,
    contentAuthorizationRules,
    new WaveformMapper(waveformUrlsRepo),
    new TrackPurchaseLinkMapper(okidokiClient),
    new LikeCountMapper(lieblingClient),
    new PlaylistTracksMapper(okidokiClient, baseUrl),
    new EntitySummaryMapper(okidokiClient, baseUrl)
  )

  private val searchController = {
    val searchRepository = new SearchRepository(searchService)
    val searchMapper = new SearchMapper(searchRepository, searchEntityMapper, baseUrl)
    new SearchController(
      userAuthentication,
      mothershipDispatcher,
      followCountsClient,
      feature => rolloutClient.isActive(BasicRolloutFeature(s"search_avoid_mothership_for_$feature")),
      searchMapper,
      baseUrl
    )
  }

  private val similarSoundsController = {
    val similarSoundsMapper = new SimilarSoundsMapper(similarSoundsClient, searchEntityMapper)
    new SimilarSoundsController(
      userAuthentication,
      similarSoundsMapper,
      baseUrl
    )
  }

  private val likesController = new LikesController(
    userAuthentication,
    mothershipDispatcher,
    followCountsClient
  )

  private val friendsController = new FriendsController(
    userAuthentication,
    mothershipDispatcher,
    followCountsClient
  )

  private val groupUsersController = new GroupUsersController(
    userAuthentication,
    mothershipDispatcher,
    followCountsClient
  )

  private val suggestedUsersController = new SuggestedUsersController(
    userAuthentication,
    mothershipDispatcher,
    followCountsClient
  )

  private val repostersController = new RepostersController(
    userAuthentication,
    mothershipDispatcher,
    followCountsClient
  )

  private val userController = new UserController(
    userAuthentication,
    mothershipDispatcher,
    followCountsClient
  )

  override val fallbackHandler = Some(mothershipDispatcher)

  private val limitOffsetEnabled = () => rolloutClient.isActive(BasicRolloutFeature("offset_limit"))
  private val limitOffsetPaths = Seq(
    """/e1/me/likes""",
    """/e1/me/playlist_likes""",
    """/e1/me/playlist_likes/ids""",
    """/e1/me/track_likes""",
    """/e1/me/track_likes/ids""",

    """/e1/users/\d+/likes""",
    """/e1/users/\d+/playlist_likes""",
    """/e1/users/\d+/playlist_likes/ids""",
    """/e1/users/\d+/track_likes""",
    """/e1/users/\d+/track_likes/ids""",

    """/me/favorites""",
    """/me/favorites/ids""",

    """/tracks/\d+/favoriters""",

    """/users/\d+/favorites""",
    """/users/\d+/favorites/ids"""
  )
  private val limitOffset = 200


  override lazy val additionalFilters = List(
    new AcceptOnlyJsonRequestFilter,
    new OffsetLimitRequestFilter(limitOffsetEnabled, limitOffsetPaths, limitOffset),
    new CookieHeaderRemovalFilter,
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
    tracksController,
    likesController,
    friendsController,
    groupUsersController,
    suggestedUsersController,
    repostersController,
    userController
  )

  override val customAdminHandlers: Seq[(AdminRoute, Handler)] = Seq(
    new AdminRoute(RequestMethod.GET, "/-/rate-limiting-diagnostics") ->
      rateLimitingFacade.rateLimitingDiagnosticsAdminHandler
  )
}
