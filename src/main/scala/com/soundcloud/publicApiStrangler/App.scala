package com.soundcloud.publicApiStrangler

import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.media.{MediaUrlsRepository, WaveformUrlsRepository}
import com.soundcloud.bff.services.{JsonService, ServiceConfig}
import com.soundcloud.jvmkit.config.ConfigConvention.ADDRESS
import com.soundcloud.jvmkit.config.{AppConfig, ConfigConvention, DataSensitivity}
import com.soundcloud.jvmkit.module.admin.AdminServer
import com.soundcloud.jvmkit.module.bff.BffHttpServer
import com.soundcloud.jvmkit.module.bff.filters.SessionCacheFilter
import com.soundcloud.jvmkit.module.bff.ratelimiting.facade._
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.client.config.HttpClientConfig
import com.soundcloud.jvmkit.module.http.client.{JsonClient => ModuleJsonClient}
import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRouter, HandlerRouterBuilder}
import com.soundcloud.jvmkit.module.http.server.config.HttpServerConfig
import com.soundcloud.jvmkit.module.memcached.MemcachedClient
import com.soundcloud.jvmkit.module.memcached.config.MemcachedClientConfig
import com.soundcloud.jvmkit.module.rollout.{BasicRolloutFeature, Rollout}
import com.soundcloud.jvmkit.module.servicediscovery.{ServiceEntryPoint => ModuleServiceEntryPoint}
import com.soundcloud.jvmkit.module.telemetry.{Telemetry => ModuleTelemetry}
import com.soundcloud.jvmkit.module.util.config.{AppConfig => ModuleAppConfig, ConfigConvention => ModuleConfigConvention}
import com.soundcloud.jvmkit.module.util.{ResourceName => ModuleResourceName, Urn => ModuleUrn}
import com.soundcloud.jvmkit.module.zookeeper.CuratorFrameworkFactory
import com.soundcloud.jvmkit.telemetry.{MetricsRegistryImpl, Telemetry}
import com.soundcloud.jvmkit.{ResourceName, Urn}
import com.soundcloud.publicApiStrangler.authorization._
import com.soundcloud.publicApiStrangler.client._
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.client.follows.FollowsClient
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.mediaservice.MediaServiceUrlGenClient
import com.soundcloud.publicApiStrangler.client.playlists.{PlaylistDeletionClient, PlaylistsClient}
import com.soundcloud.publicApiStrangler.client.pubmese.PubmeseClient
import com.soundcloud.publicApiStrangler.client.quota.UserQuotaClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.client.sketchy.SketchyClient
import com.soundcloud.publicApiStrangler.client.stitch.StitchClient
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorClient
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.controller._
import com.soundcloud.publicApiStrangler.headers.DefaultResponseHeadersFilter
import com.soundcloud.publicApiStrangler.mapper.liebling.LikeCountMapper
import com.soundcloud.publicApiStrangler.mapper.search.{SearchEntityMapper, SearchMapper, SearchRepository}
import com.soundcloud.publicApiStrangler.mapper.similarsounds.SimilarSoundsMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.e1.{ActivitiesMapper, StreamMapper}
import com.soundcloud.publicApiStrangler.mapper.timeline.publicApi.ActivitiesWithOriginMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper, FollowingsTracksMapper}
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{TrackStreamJsonResponseMapper, TrackStreamRedirectResponseMapper}
import com.soundcloud.publicApiStrangler.mapper.waveform.WaveformMapper
import RoutingDefinitions._
import com.soundcloud.publicApiStrangler.service.{TrackAccessibilityService, TrackRepository}
import com.soundcloud.publicApiStrangler.support._
import com.soundcloud.scalakit.finagle.dns.ServiceEntryPoint
import com.soundcloud.scalakit.finagle.http.HttpClientBuilder
import com.soundcloud.scalakit.finagle.jsonservice.JsonClient
import com.soundcloud.service.client.{GatekeeperClient, OkidokiClient, SimilarSoundsClient}
import com.soundcloud.services.timeline.TimelineJsonClient
import com.twitter.finagle.{Filter, Service, SimpleFilter}
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.service.RetryPolicy.RetryableWriteException
import com.twitter.util.{Future, Throw, Try}

object App {

  def main(args: Array[String]): Unit = {

    val config = new AppConfig()
    val moduleConfig = new ModuleAppConfig()

    val metricsRegistry = MetricsRegistryImpl.defaultRegistry
    val telemetry = new Telemetry(config, metricsRegistry)
    val moduleTelemetry = new ModuleTelemetry(config.getApplicationName, metricsRegistry)

    val bffApplication = BffApplication(new ModuleUrn("soundcloud", "systems", "public-api-strangler"), moduleConfig.getApplicationResourceName)

    val okidokiJsonClient =
      JsonClient(
        ResourceName("okidoki"),
        ServiceEntryPoint(config.get(ResourceName("OKIDOKI"), ConfigConvention.SRV_RECORD)),
        config,
        telemetry
      )
    val okidokiClient = new OkidokiClient(okidokiJsonClient)

    val timelineJsonClient =
      JsonClient(
        ResourceName("timeline"),
        ServiceEntryPoint(config.get("TIMELINE_SRV_RECORD", DataSensitivity.NON_SENSITIVE)),
        config,
        telemetry
      )

    val timelineClient = new TimelineJsonClient(timelineJsonClient)


    val lieblingJsonClient =
      JsonClient(
        ResourceName("liebling"),
        ServiceEntryPoint(config.get(ResourceName("LIEBLING"), ConfigConvention.SRV_RECORD)),
        config,
        telemetry
      )

    val lieblingClient = new LieblingClient(lieblingJsonClient)

    lazy val publicApiClient: Service[Request, Response] = {
      val telemetry = new Telemetry(config)

      val writeExceptions: PartialFunction[(Request, Try[Response]), Boolean] = {
        case (_, Throw(RetryableWriteException(_))) => true
      }

      new HttpClientBuilder(ResourceName("PUBLIC_API"),
        ServiceEntryPoint(config.get(ResourceName("PUBLIC_API"), ADDRESS)),
        config,
        telemetry,
        retry = Some(writeExceptions)).client

    }
    val followsService =
      JsonClient(
        ResourceName("follows"),
        ServiceEntryPoint(config.get(ResourceName("FOLLOWS"), ConfigConvention.SRV_RECORD)),
        config,
        telemetry
      )

    lazy val followsClient = new FollowsClient(followsService)

    val repostsService =
      JsonClient(
        ResourceName("reposts"),
        ServiceEntryPoint(config.get(ResourceName("REPOSTS"), ConfigConvention.SRV_RECORD)),
        config,
        telemetry
      )

    lazy val repostsClient = new RepostsClient(repostsService)

    val gatekeeperJsonClient =
      JsonClient(ResourceName("gatekeeper"),
        ServiceEntryPoint(config.get(ResourceName("GATEKEEPER"), ConfigConvention.SRV_RECORD)),
        config,
        telemetry)

    val gatekeeperClient = new GatekeeperClient(gatekeeperJsonClient)

    val similarSoundsJsonClient =
      JsonClient(
        ResourceName("similar_sounds"),
        ServiceEntryPoint(config.get(ResourceName("SIMILAR_SOUNDS"), ConfigConvention.SRV_RECORD)),
        config,
        telemetry
      )

    val similarSoundsClient = new SimilarSoundsClient(similarSoundsJsonClient)

    val trackCoordinatorResourceName = ResourceName("track_coordinator")
    val trackCoordinatorJsonClient =
      JsonClient(
        trackCoordinatorResourceName,
        ServiceEntryPoint(config.get(trackCoordinatorResourceName, ConfigConvention.SRV_RECORD)),
        config,
        telemetry
      )

    val trackCoordinatorClient = new TrackCoordinatorClient(trackCoordinatorJsonClient)

    val sketchyService = JsonClient(
      ResourceName("sketchy"),
      ServiceEntryPoint(config.get(ResourceName("SKETCHY"), ConfigConvention.SRV_RECORD)),
      config,
      telemetry
    )

    lazy val sketchyClient = new SketchyClient(sketchyService)

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


    val mediaServiceUrlGenJsonClient =
      ModuleJsonClient(
        ModuleServiceEntryPoint(moduleConfig.get(ModuleResourceName("MEDIASERVICE"), ModuleConfigConvention.SRV_RECORD)),
        HttpClientConfig.from(ModuleResourceName("mediaservice_urlgen"), moduleConfig),
        moduleTelemetry
      )
    val mediaServiceUrlGenClient = new MediaServiceUrlGenClient(mediaServiceUrlGenJsonClient)

    val okidokiService = JsonService(
      ServiceConfig("okidoki", config.get(ResourceName("OKIDOKI"), ConfigConvention.SRV_RECORD), config)
    )
    val mediaService = JsonService(
      ServiceConfig("mediaservice", config.get(ResourceName("MEDIASERVICE"), ConfigConvention.SRV_RECORD), config)
    )
    val authsyService = JsonService(
      ServiceConfig("authsy", config.get(ResourceName("AUTHSY"), ConfigConvention.SRV_RECORD), config)
    )

    val searchService = JsonService(
      ServiceConfig("search", config.get(ResourceName("SEARCH"), ConfigConvention.SRV_RECORD), config)
    )

    val subscriptionsService = JsonService(
      ServiceConfig("user_subscriptions", config.get(ResourceName("USER_SUBSCRIPTIONS"), ConfigConvention.SRV_RECORD), config)
    )

    val stitch4followsService = JsonService(
      ServiceConfig("stitch4follows", config.get(ResourceName("STITCH4FOLLOWS"), ConfigConvention.SRV_RECORD), config)
    )

    val playlistsClient = new PlaylistsClient(
      JsonClient(
        ResourceName("playlist"),
        ServiceEntryPoint(config.get(ResourceName("PLAYLIST"), ConfigConvention.BASE_URL)),
        config,
        telemetry)
    )

    val followCountsClient = new FollowCountsClient(stitch4followsService, moduleConfig)

    val trackmetadataClient = TrackmetadataClient(config, telemetry)

    val contentAuthorizationRules = new ContentAuthorizationRules(
      new ContentAuthorizationService(authsyService),
      new SubscriptionsService(subscriptionsService))


    val waveformUrlsRepo = new WaveformUrlsRepository(okidokiService, mediaService)

    // Whitelist source: http://redash.int.s-cloud.net/queries/632/source
    val whitelistedClients: Set[ModuleUrn] = Set(
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
    ).map(new ModuleUrn(_))

    val blacklistOfAppIdsForUserSiloing: Set[ModuleUrn] =
      config.get("APP_SILOING_BLACKLIST_APPS", DataSensitivity.NON_SENSITIVE)
        .split(",")
        .map(appId => new ModuleUrn(appId.trim))
        .toSet

    val userAuthentication = UserAuthentication(moduleConfig, moduleTelemetry)
    val authorizeContent = new AuthorizeHttpResponse(contentAuthorizationRules, userAuthentication, waveformUrlsRepo, TrackPolicyApplicator(whitelistedClients))

    val mothershipDispatcher = new DispatchToMothershipHandler(publicApiClient)

    val baseUrl = config.get("APP_BASE_URL", DataSensitivity.NON_SENSITIVE)

    val rolloutClient = Rollout(moduleConfig, moduleTelemetry)
    val rollout = Some(rolloutClient)

    val enrichLikesCounts: () => Future[Boolean] =
      () => rolloutClient.isActive(BasicRolloutFeature("load_user_like_counts_from_liebling"))

    val userRelatedMothershipDispatcher = new UserRelatedMothershipDispatcher(
      userAuthentication,
      mothershipDispatcher,
      followCountsClient,
      lieblingClient,
      enrichLikesCounts,
      repostsClient
    )

    val timelineController = {
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
      new TimelineController(userAuthentication, streamMapper, activitiesMapper, publicActivitiesMapper, followingsTracksMapper, pagination)
    }


    val trackStreamsController = {
      val trackStreamUrlToJsonResponseMapper = new TrackStreamJsonResponseMapper
      val trackStreamUrlToRedirectMapper = new TrackStreamRedirectResponseMapper

      val mediaUrlsRepository = new MediaUrlsRepository(mediaService)
      val trackStreamSnipHandler = new TrackStreamHandler(mothershipDispatcher, contentAuthorizationRules, mediaUrlsRepository)
      val rolloutCheckForSiloingFunc = {
        val siloingEnabledFeature = BasicRolloutFeature("app-siloing-enabled")
        () => rolloutClient.isActive(siloingEnabledFeature)
      }
      val publicApiSiloing = new PublicApiSiloing(rolloutCheckForSiloingFunc, blacklistOfAppIdsForUserSiloing, moduleTelemetry)

      new TrackStreamsController(
        userAuthentication,
        trackStreamUrlToJsonResponseMapper,
        trackStreamUrlToRedirectMapper,
        trackStreamSnipHandler,
        publicApiSiloing
      )
    }

    val tracksController = new TracksController(userAuthentication,
      trackCoordinatorClient,
      okidokiClient,
      mothershipDispatcher,
      trackmetadataClient)


    val richOkidokiClient = new RichOkidokiClient(okidokiJsonClient)

    val userQuotaClient = new UserQuotaClient(okidokiJsonClient)

    val trackAccessibilityService = new TrackAccessibilityService(playlistsClient)

    val trackRepository = new TrackRepository(
      trackmetadataClient,
      richOkidokiClient,
      pubmeseClient,
      stitchClient,
      lieblingClient,
      mediaServiceUrlGenClient,
      userQuotaClient,
      trackAccessibilityService
    )

    val tracksService = new TrackRepresentationsService(
      trackRepository,
      trackmetadataClient,
      richOkidokiClient,
      pubmeseClient,
      stitchClient,
      lieblingClient,
      mediaServiceUrlGenClient,
      userQuotaClient,
      trackAccessibilityService
    )

    val singleTrackController = new SingleTrackController(userAuthentication, tracksService, moduleTelemetry)

    val userTracksController = {
      val trackMothershipDispatcherWithCounts = new TrackMothershipDispatcherWithCounts(
        userAuthentication,
        mothershipDispatcher,
        stitchClient)

      val shouldUseTrackMetadata = BasicRolloutFeature("track_metadata_for_user_tracks")

      new UserTracksController(
        userAuthentication,
        trackMothershipDispatcherWithCounts,
        tracksService,
        moduleTelemetry,
        () => rolloutClient.isActive(shouldUseTrackMetadata),
        baseUrl
      )
    }

    val memcachedResourceName = ModuleResourceName("MEMCACHED")
    lazy val memcachedClient = MemcachedClient(
      MemcachedClientConfig.from(memcachedResourceName, moduleConfig),
      moduleTelemetry)

    val curatorFramework = CuratorFrameworkFactory.create(moduleConfig, moduleTelemetry)

    val rateLimitingFacade = new RateLimitingFacade(
      bffApplication,
      curatorFramework,
      userAuthentication,
      moduleConfig,
      moduleTelemetry,
      memcachedClient,
      rolloutClient,
      Some(Seq(RateLimits.playsRateLimiter, RateLimits.searchRateLimiter))
    )

    val userFollowController = new UserFollowController(userAuthentication, okidokiClient, followsClient, followCountsClient, repostsClient, baseUrl)

    val searchEntityMapper = new SearchEntityMapper(
      okidokiClient,
      followCountsClient,
      repostsClient,
      baseUrl,
      contentAuthorizationRules,
      new WaveformMapper(waveformUrlsRepo),
      new LikeCountMapper(lieblingClient),
      new EntitySummaryMapper(okidokiClient, repostsClient, baseUrl)
    )

    val trackMothershipDispatcherWithCounts = new TrackMothershipDispatcherWithCounts(userAuthentication, mothershipDispatcher, stitchClient)

    val searchController = {
      val searchRepository = new SearchRepository(searchService)
      val searchMapper = new SearchMapper(searchRepository, searchEntityMapper, baseUrl)
      val mothershipCounter = moduleTelemetry.counter(
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
        baseUrl,
        lieblingClient,
        userRelatedMothershipDispatcher,
        trackMothershipDispatcherWithCounts
      )
    }

    val similarSoundsController = {
      val similarSoundsMapper = new SimilarSoundsMapper(similarSoundsClient, searchEntityMapper)
      new SimilarSoundsController(
        userAuthentication,
        similarSoundsMapper,
        baseUrl
      )
    }

    val repostersController = new RepostersController(userAuthentication,
      repostsClient,
      richOkidokiClient,
      followCountsClient,
      lieblingClient,
      enrichLikesCounts)


    val playlistDeletionClient =
      new PlaylistDeletionClient(
        ModuleJsonClient(
          ModuleServiceEntryPoint(moduleConfig.get(ModuleResourceName("OKIDOKI"), ModuleConfigConvention.SRV_RECORD)),
          HttpClientConfig.from(ModuleResourceName("okidoki"), moduleConfig),
          moduleTelemetry
        ))

    val playlistsController = new PlaylistsController(userAuthentication, playlistDeletionClient)

    val repostsController = new RepostsController(userAuthentication, repostsClient)

    val spamWarningsController = new SpamWarningsController(userAuthentication, sketchyClient)

    val officialSoundCloudApps = List(
      new ModuleUrn("soundcloud:applications:46941"), // SoundCloud.com (currently being abused) Internal
      new ModuleUrn("soundcloud:applications:124"), // SoundCloud iOS Internal
      new ModuleUrn("soundcloud:applications:3152"), // SoundCloud Android Internal
      new ModuleUrn("soundcloud:applications:3273"), // Mobile Soundcloud Internal
      new ModuleUrn("soundcloud:applications:65097"), // Mobi (new mobile soundcloud) Internal
      new ModuleUrn("soundcloud:applications:-1"), // Classic Internal
      new ModuleUrn("soundcloud:applications:43164"), // SoundCloud Player Widget Internal
      new ModuleUrn("soundcloud:applications:90575"), // SoundCloud Visual Embed Player Internal
      new ModuleUrn("soundcloud:applications:60973"), // SoundCloud Flash Widget Internal
      new ModuleUrn("soundcloud:applications:66151"), // Old mobi web Internal
      new ModuleUrn("soundcloud:applications:3537"), // SoundCloud Desktop Internal
      new ModuleUrn("soundcloud:applications:99561"), // SoundCloud Kik Messenger Card Internal
      new ModuleUrn("soundcloud:applications:120502"), // Twitter Partner Internal
      new ModuleUrn("soundcloud:applications:42975"), // SoundCloud Notifications Internal
      new ModuleUrn("soundcloud:applications:147241"), // SoundCloud Jobs Page Internal
      new ModuleUrn("soundcloud:applications:140141"), // SoundCloud Chromecast Receiver Internal
      new ModuleUrn("soundcloud:applications:179522") // Facebook Partner Internal
    )

    val whatToStrangle = {
      // Endpoints we officially support: https://developers.soundcloud.com/docs/api/reference
      val officiallySupported = List(
        """/connect""",
        """/oauth2/token""",
        """/users/\d+""",
        """/tracks/\d+""",
        """/playlists/\d+""",
        """/comments/\d+""",
        """/me""",
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

    val fallthroughCounter = moduleTelemetry.counter(
      "fallthrough_strangled_by",
      "Fallthrough requests by the path pattern that strangles them",
      "method",
      "path_pattern",
      "agent_urn"
    )

    val fallbackHandler =
      new SpecificStranglingHandler(mothershipDispatcher,
        whatToStrangle,
        officialSoundCloudApps,
        fallthroughCounter)

    val limitOffsetPaths = Seq(
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
    val limitOffset = 200

    lazy val additionalFilters: List[SimpleFilter[Request, Response]] =
      List(
        new SuccesfulResponseTypeMetricFilter(moduleTelemetry),
        new ExceptForTrackUploadsFilter(new ContentAuthorizationFilter(authorizeContent)),
        new ExceptForTrackUploadsFilter(rateLimitingFacade.filter),
        new DefaultResponseHeadersFilter,
        new SessionCacheFilter(userAuthentication),
        new CookieHeaderRemovalFilter,
        new OffsetLimitRequestFilter(limitOffsetPaths, limitOffset),
        new AcceptOnlyJsonRequestFilter(() => new StripXmlRollout(rolloutClient).stripXml),
        new StaticFilesFilter
      )


    //    val customAdminHandlers: Seq[(AdminRoute, Handler)] =
    //      Seq(
    //        new AdminRoute(RequestMethod.GET, "/-/rate-limiting-diagnostics") ->
    //          rateLimitingFacade.rateLimitingDiagnosticsAdminHandler
    //      )


    val router = HandlerRouterBuilder()
      .registerFallback(fallbackHandler)
      .register(Method.Get, rateLimitingFacade.statusEndpoint, rateLimitingFacade.rateLimitStatusHandler.handle)
      .register(List.concat(
        forUserFollowController(userFollowController),
        forMothershipDispatcher(mothershipDispatcher),
        forSingleTrackController(singleTrackController),
        forPlaylistontroller(playlistsController),
        forSimilarSoundsController(similarSoundsController),
        forTracksController(tracksController),
        forUserRelatedMothershipDispatcher(userRelatedMothershipDispatcher),
        forSearchController(searchController),
        forUserTracksController(userTracksController),
        forRepostsController(repostsController),
        forRepostersController(repostersController),
        forSpamWarningsController(spamWarningsController),
        forTimelineController(timelineController),
        forTrackStreamsController(trackStreamsController)))
      .build


    new AdminServer(
      config = moduleConfig,
      telemetry = moduleTelemetry,
      customHandlers = List(
        (Method.Get, "/rate-limiting", rateLimitingFacade.rateLimitingDiagnosticsAdminHandler.handle _)
      ),
      rollout = rollout
    ).start()

    BffHttpServer(resourceName = moduleConfig.getApplicationResourceName,
      config = HttpServerConfig.from(moduleConfig),
      telemetry = moduleTelemetry,
      router = router,
      customFilters = additionalFilters
    ).start().join()
  }
}

class StripXmlRollout(rollout: Rollout) {
  def stripXml: Future[Boolean] = rollout.isActive(BasicRolloutFeature("strip_format_xml_param"))
}
