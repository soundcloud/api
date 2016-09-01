package com.soundcloud.publicApiStrangler

import com.soundcloud.bff._
import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.filter.SessionCache
import com.soundcloud.bff.media.{MediaUrlsRepository, WaveformUrlsRepository}
import com.soundcloud.bff.services.JsonService
import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.admin.{AdminRoute, RequestMethod}
import com.soundcloud.jvmkit.config.{ConfigConvention, DataSensitivity}
import com.soundcloud.jvmkit.rollout.{BasicRolloutFeature, Rollout, RolloutBuilder}
import com.soundcloud.publicApiStrangler.authorization._
import com.soundcloud.publicApiStrangler.client._
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.client.pubmese.PubmeseClient
import com.soundcloud.publicApiStrangler.client.stitch.StitchClient
import com.soundcloud.publicApiStrangler.controller._
import com.soundcloud.publicApiStrangler.headers.DefaultResponseHeadersFilter
import com.soundcloud.publicApiStrangler.mapper.liebling.LikeCountMapper
import com.soundcloud.publicApiStrangler.mapper.purchaselink.TrackPurchaseLinkMapper
import com.soundcloud.publicApiStrangler.mapper.search.{SearchEntityMapper, SearchMapper, SearchRepository}
import com.soundcloud.publicApiStrangler.mapper.similarsounds.SimilarSoundsMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.e1.{ActivitiesMapper, StreamMapper}
import com.soundcloud.publicApiStrangler.mapper.timeline.publicApi.ActivitiesWithOriginMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper, FollowingsTracksMapper}
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{TrackStreamJsonResponseMapper, TrackStreamRedirectResponseMapper}
import com.soundcloud.publicApiStrangler.mapper.waveform.WaveformMapper
import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.TrackmetadataClient
import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.{ResponseComparison, SingleTrackController, TrackRepresentationsService}
import com.soundcloud.publicApiStrangler.support._
import com.soundcloud.publicApiStrangler.zookeeper.CuratorFrameworkFactory
import com.soundcloud.ratelimiting.facade._
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.cache.MemcachedClient
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, ServiceEntryPoint}
import com.soundcloud.service.client.{MoshimoshiClient, OkidokiClient}
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

  val pubmeseJsonClient =
    JsonClient(
      ResourceName("pubmese"),
      ServiceEntryPoint(config.get(ResourceName("PUBMESE"), ConfigConvention.ADDRESS)),
      config,
      telemetry
    )
  val pubmeseClient = new PubmeseClient(pubmeseJsonClient)

  val stitchJsonClient =
    JsonClient(
      ResourceName("stitch"),
      ServiceEntryPoint(config.get(ResourceName("STITCH"), ConfigConvention.ADDRESS)),
      config,
      telemetry
    )
  val stitchClient = new StitchClient(stitchJsonClient)

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

  private val trackmetadataClient = TrackmetadataClient(config, telemetry)

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
    "soundcloud:applications:288860",
    "soundcloud:applications:271862",
    "soundcloud:applications:59007",
    "soundcloud:applications:62023",
    "soundcloud:applications:265616",
    "soundcloud:applications:265183"
  ).map(new Urn(_))

  private val blacklistOfAppIdsForUserSiloing: Set[Urn] =
    config.get("APP_SILOING_BLACKLIST_APPS", DataSensitivity.NON_SENSITIVE)
      .split(",")
      .map(appId => Urn(appId.trim))
      .toSet

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
    val rolloutCheckForSiloingFunc = {
      val siloingEnabledFeature = BasicRolloutFeature("app-siloing-enabled")
      () => rolloutClient.isActive(siloingEnabledFeature)
    }
    val publicApiSiloing = new PublicApiSiloing(rolloutCheckForSiloingFunc, blacklistOfAppIdsForUserSiloing, telemetry)

    new TrackStreamsController(
      userAuthentication,
      trackStreamUrlToJsonResponseMapper,
      trackStreamUrlToRedirectMapper,
      trackStreamSnipHandler,
      publicApiSiloing
    )
  }

  private val tracksController = new TracksController(userAuthentication,
    trackCoordinatorClient,
    okidokiClient,
    mothershipDispatcher,
    gobblyClient)

  lazy val rolloutClient = new RolloutBuilder(config, telemetry).build
  override lazy val rollout = Some(rolloutClient)

  val richOkidokiClient = new RichOkidokiClient(okidokiJsonClient)

  private val singleTrackController = {
    val tracksService = new TrackRepresentationsService(
      trackmetadataClient,
      richOkidokiClient,
      pubmeseClient,
      stitchClient,
      lieblingClient
    )

    new SingleTrackController(
      userAuthentication,
      mothershipDispatcher,
      tracksService,
      new ResponseComparison(telemetry),
      telemetry
    )
  }

  lazy val memcachedClient = MemcachedClient(config)

  private val rateLimitingFacade = new RateLimitingFacade(
    bffApplication,
    curatorFramework,
    userAuthentication,
    config,
    telemetry,
    memcachedClient,
    rolloutClient,
    Some(Seq(RateLimits.playsRateLimiter, RateLimits.searchRateLimiter))
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
    new EntitySummaryMapper(okidokiClient, baseUrl)
  )

  private val searchController = {
    val searchRepository = new SearchRepository(searchService)
    val searchMapper = new SearchMapper(searchRepository, searchEntityMapper, baseUrl)
    val mothershipCounter = telemetry.counter(
      "search_mothership_fallback_total",
      "Number of requests to search endpoints with missing/invalid query parameters that get propagated to Mothership",
      "path"
    )

    new SearchController(
      userAuthentication,
      mothershipDispatcher,
      mothershipCounter,
      followCountsClient,
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

  private val groupUsersController = new GroupsController(
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

  private val userController = new UsersController(
    userAuthentication,
    mothershipDispatcher,
    followCountsClient
  )

  private val playlistsController = new PlaylistsController(
    userAuthentication,
    okidokiClient,
    mothershipDispatcher
  )

  private val resolveController = new ResolveController(
    mothershipDispatcher
  )

  private val announcementsController = new AnnouncementsController(
    mothershipDispatcher
  )

  private val oAuthController = new OAuthController(
    mothershipDispatcher
  )

  private val officialSoundCloudApps = List(
    Urn("soundcloud:applications:46941"), // SoundCloud.com (currently being abused) Internal
    Urn("soundcloud:applications:124"), // SoundCloud iOS Internal
    Urn("soundcloud:applications:3152"), // SoundCloud Android Internal
    Urn("soundcloud:applications:3273"), // Mobile Soundcloud Internal
    Urn("soundcloud:applications:65097"), // Mobi (new mobile soundcloud) Internal
    Urn("soundcloud:applications:-1"), // Classic Internal
    Urn("soundcloud:applications:43164"), // SoundCloud Player Widget Internal
    Urn("soundcloud:applications:90575"), // SoundCloud Visual Embed Player Internal
    Urn("soundcloud:applications:60973"), // SoundCloud Flash Widget Internal
    Urn("soundcloud:applications:66151"), // Old mobi web Internal
    Urn("soundcloud:applications:3537"), // SoundCloud Desktop Internal
    Urn("soundcloud:applications:99561"), // SoundCloud Kik Messenger Card Internal
    Urn("soundcloud:applications:120502"), // Twitter Partner Internal
    Urn("soundcloud:applications:42975"), // SoundCloud Notifications Internal
    Urn("soundcloud:applications:147241"), // SoundCloud Jobs Page Internal
    Urn("soundcloud:applications:140141"), // SoundCloud Chromecast Receiver Internal
    Urn("soundcloud:applications:179522") // Facebook Partner Internal
  )

  private val whatToStrangle = {
    // Endpoints we officially support: https://developers.soundcloud.com/docs/api/reference
    val officiallySupported = List(
      """/connect""",
      """/oauth2/token""",
      """/users/\d+""",
      """/tracks/\d+""",
      """/playlists/\d+""",
      """/groups/\d+""",
      """/comments/\d+""",
      """/me""",
      """/me/groups""",
      """/me/connections""",
      """/me/connections/\d+""",
      """/apps""",
      """/resolve""",
      """/oembed"""
    )

    // Newly discovered endpoints:
    val newlyDiscovered = List(
      """/announcements""",
      """/search/sounds""",
      """/search/sets""",
      """/e1/playlists/\d+/domain-lockings""",
      """/e1/shorten""",
      """/i1/comments/\d+/spam""",
      """/transcodings/.*""",
      """/me/track_likes/ids""",
      """/me/playlist_likes/ids""",
      """/me/shortcuts""",
      """/me/favorites""",
      """/me/tracks""",
      """/me/track_reposts/ids""",
      """/me/playlist_reposts/ids""",
      """/me/stats""",
      """/tracks/\d+/download""",
      """/tracks/\d+/comments""",
      """/tracks/\d+/stream""",
      """/tracks/\d+/streams""",
      """/tracks/\d+/related""",
      """/tracks/\d+/groups""",
      """/i1/tracks/\d+/streams""",
      """/tracks/[a-zA-Z0-9\-\_]+""",
      """/tracks/[a-zA-Z0-9\-\_]+/download""",
      """/tracks/[a-zA-Z0-9\-\_]+/comments""",
      """/tracks/[a-zA-Z0-9\-\_]+/stream""",
      """/tracks/[a-zA-Z0-9\-\_]+/streams""",
      """/tracks/[a-zA-Z0-9\-\_]+/related""",
      """/i1/tracks/[a-zA-Z0-9\-\_]+/streams""",
      """/playlists/\d+""",
      """/playlists/\d+/tracks""",
      """/playlists/[a-zA-Z0-9\-\_]+""",
      """/upload/policy""",
      """/users""",
      """/users/\d+/groups""",
      """/users/\d+/favorites""",
      """/users/\d+/tracks""",
      """/users/\d+/comments""",
      """/users/\d+/playlists""",
      """/users/\d+/web-profiles""",
      """/users/[a-zA-Z0-9\_\-]+""",
      """/users/[a-zA-Z0-9\_\-]+/groups""",
      """/users/[a-zA-Z0-9\-\_]+/favorites""",
      """/users/[a-zA-Z0-9\-\_]+/tracks""",
      """/users/[a-zA-Z0-9\-\_]+/comments""",
      """/users/[a-zA-Z0-9\-\_]+/playlists""",
      """/users/[a-zA-Z0-9\-\_]+/playlists/\d+""",
      """/users/[a-zA-Z0-9\-\_]+/web-profiles""",
      """/users/\d+/followings/not_followed_by/""",
      """/e1/users/\d+/sounds""",
      """/e1/users/\d+/likes""",

      // https://github.com/soundcloud/soundcloud/blob/master/config/routes.rb#L228-L238
      """/e1/me/likes""",
      """/e1/me/sounds""",
      """/e1/me/reposts""",
      """/e1/me/track_likes""",
      """/e1/me/track_reposts""",
      """/e1/me/playlist_likes""",
      """/e1/me/playlist_reposts""",
      """/e1/me/track_likes/ids""",
      """/e1/me/track_reposts/ids""",
      """/e1/me/playlist_likes/ids""",
      """/e1/me/playlist_reposts/ids""",

      // https://github.com/soundcloud/soundcloud/blob/master/config/routes.rb#L240-L246
      """/i1/me/shortcuts"""
    )

    // Everything else, to be compatible with what we have right now
    val unknown = List(".*".r)

    val everything = officiallySupported ++ newlyDiscovered

    everything.flatMap { endpoint =>
      // Wrap regex with start and end anchors. May have .json at the end. May have trailing slash.
      val patternWithOptionalJsonAndSlash = endpoint + "(\\.json)?/?"
      val originalPattern = ("^" + patternWithOptionalJsonAndSlash + "$").r
      val v1Pattern = ("^/v1" + patternWithOptionalJsonAndSlash + "$").r

      // All endpoints may be prefixed with v1 - record these separately
      List(originalPattern, v1Pattern)
    } ++ unknown
  }

  private val fallthroughCounter = telemetry.counter(
    "fallthrough_strangled_by",
    "Fallthrough requests by the path pattern that strangles them",
    "method",
    "path_pattern",
    "agent_urn"
  )

  override val fallbackHandler = Some(
    new SpecificStranglingHandler(mothershipDispatcher,
      whatToStrangle,
      officialSoundCloudApps,
      fallthroughCounter))

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
    new SuccesfulResponseTypeMetricFilter(telemetry),
    new ExceptForTrackUploadsFilter(new ContentAuthorizationFilter(authorizeContent)),
    new ExceptForTrackUploadsFilter(rateLimitingFacade.filter),
    new DefaultResponseHeadersFilter,
    new SessionCache(userAuthentication),
    new CookieHeaderRemovalFilter,
    new OffsetLimitRequestFilter(limitOffsetPaths, limitOffset),
    new AcceptOnlyJsonRequestFilter(() => new StripXmlRollout(rolloutClient).stripXml),
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
    singleTrackController,
    likesController,
    friendsController,
    groupUsersController,
    suggestedUsersController,
    repostersController,
    userController,
    playlistsController,
    resolveController,
    announcementsController,
    oAuthController
  )

  override val customAdminHandlers: Seq[(AdminRoute, Handler)] = Seq(
    new AdminRoute(RequestMethod.GET, "/-/rate-limiting-diagnostics") ->
      rateLimitingFacade.rateLimitingDiagnosticsAdminHandler
  )
}

class StripXmlRollout(rollout: Rollout) {
  def stripXml: Future[Boolean] = rollout.isActive(BasicRolloutFeature("strip_format_xml_param"))
}
