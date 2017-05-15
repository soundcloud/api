package com.soundcloud.publicApiStrangler

import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.media.WaveformUrlsRepository
import com.soundcloud.bff.services.{JsonService, ServiceConfig}
import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.config.ConfigConvention.ADDRESS
import com.soundcloud.jvmkit.config.{AppConfig, ConfigConvention, DataSensitivity}
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.client.config.HttpClientConfig
import com.soundcloud.jvmkit.module.http.client.{JsonClient => ModuleJsonClient}
import com.soundcloud.jvmkit.module.rollout.{BasicRolloutFeature, Rollout}
import com.soundcloud.jvmkit.module.servicediscovery.{ServiceEntryPoint => ModuleServiceEntryPoint}
import com.soundcloud.jvmkit.module.telemetry.{MetricsRegistry, Telemetry => ModuleTelemetry}
import com.soundcloud.jvmkit.module.util.config.{AppConfig => ModuleAppConfig, ConfigConvention => ModuleConfigConvention}
import com.soundcloud.jvmkit.module.util.{ResourceName => ModuleResourceName, Urn => ModuleUrn}
import com.soundcloud.jvmkit.telemetry.Telemetry
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
import com.soundcloud.publicApiStrangler.mapper.liebling.LikeCountMapper
import com.soundcloud.publicApiStrangler.mapper.search.SearchEntityMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import com.soundcloud.publicApiStrangler.mapper.waveform.WaveformMapper
import com.soundcloud.publicApiStrangler.service.{TrackAccessibilityService, TrackRepository}
import com.soundcloud.scalakit.finagle.dns.ServiceEntryPoint
import com.soundcloud.scalakit.finagle.http.HttpClientBuilder
import com.soundcloud.scalakit.finagle.jsonservice.JsonClient
import com.soundcloud.service.client.{GatekeeperClient, OkidokiClient, SimilarSoundsClient}
import com.soundcloud.services.timeline.TimelineJsonClient
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.service.RetryPolicy.RetryableWriteException
import com.twitter.util.{Future, Throw, Try}

trait Clients {

  def config: AppConfig

  def moduleConfig: ModuleAppConfig

  def telemetry: Telemetry

  def moduleTelemetry: ModuleTelemetry

  def metricsRegistry: MetricsRegistry

  private val okidokiJsonClient =
    JsonClient(
      ResourceName("okidoki"),
      ServiceEntryPoint(config.get(ResourceName("OKIDOKI"), ConfigConvention.SRV_RECORD)),
      config,
      telemetry
    )
  val okidokiClient = new OkidokiClient(okidokiJsonClient)

  private val timelineJsonClient =
    JsonClient(
      ResourceName("timeline"),
      ServiceEntryPoint(config.get("TIMELINE_SRV_RECORD", DataSensitivity.NON_SENSITIVE)),
      config,
      telemetry
    )
  val timelineClient = new TimelineJsonClient(timelineJsonClient)

  private val lieblingJsonClient =
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

  private val followsService =
    JsonClient(
      ResourceName("follows"),
      ServiceEntryPoint(config.get(ResourceName("FOLLOWS"), ConfigConvention.SRV_RECORD)),
      config,
      telemetry
    )
  lazy val followsClient = new FollowsClient(followsService)

  private val repostsService =
    JsonClient(
      ResourceName("reposts"),
      ServiceEntryPoint(config.get(ResourceName("REPOSTS"), ConfigConvention.SRV_RECORD)),
      config,
      telemetry
    )
  lazy val repostsClient = new RepostsClient(repostsService)

  private val gatekeeperJsonClient =
    JsonClient(ResourceName("gatekeeper"),
      ServiceEntryPoint(config.get(ResourceName("GATEKEEPER"), ConfigConvention.SRV_RECORD)),
      config,
      telemetry)
  val gatekeeperClient = new GatekeeperClient(gatekeeperJsonClient)

  private val similarSoundsJsonClient =
    JsonClient(
      ResourceName("similar_sounds"),
      ServiceEntryPoint(config.get(ResourceName("SIMILAR_SOUNDS"), ConfigConvention.SRV_RECORD)),
      config,
      telemetry
    )
  val similarSoundsClient = new SimilarSoundsClient(similarSoundsJsonClient)

  private val trackCoordinatorResourceName = ResourceName("track_coordinator")
  private val trackCoordinatorJsonClient =
    JsonClient(
      trackCoordinatorResourceName,
      ServiceEntryPoint(config.get(trackCoordinatorResourceName, ConfigConvention.SRV_RECORD)),
      config,
      telemetry
    )
  val trackCoordinatorClient = new TrackCoordinatorClient(trackCoordinatorJsonClient)

  private val sketchyService = JsonClient(
    ResourceName("sketchy"),
    ServiceEntryPoint(config.get(ResourceName("SKETCHY"), ConfigConvention.SRV_RECORD)),
    config,
    telemetry
  )
  lazy val sketchyClient = new SketchyClient(sketchyService)

  private val pubmeseJsonClient =
    JsonClient(
      ResourceName("pubmese"),
      ServiceEntryPoint(config.get(ResourceName("PUBMESE"), ConfigConvention.ADDRESS)),
      config,
      telemetry
    )
  val pubmeseClient = new PubmeseClient(pubmeseJsonClient)

  private val stitchJsonClient =
    JsonClient(
      ResourceName("stitch"),
      ServiceEntryPoint(config.get(ResourceName("STITCH"), ConfigConvention.ADDRESS)),
      config,
      telemetry
    )
  val stitchClient = new StitchClient(stitchJsonClient)


  private val mediaServiceUrlGenJsonClient =
    ModuleJsonClient(
      ModuleServiceEntryPoint(moduleConfig.get(ModuleResourceName("MEDIASERVICE"), ModuleConfigConvention.SRV_RECORD)),
      HttpClientConfig.from(ModuleResourceName("mediaservice_urlgen"), moduleConfig),
      moduleTelemetry
    )
  val mediaServiceUrlGenClient = new MediaServiceUrlGenClient(mediaServiceUrlGenJsonClient)

  private val okidokiService = JsonService(
    ServiceConfig("okidoki", config.get(ResourceName("OKIDOKI"), ConfigConvention.SRV_RECORD), config)
  )
  val mediaService = JsonService(
    ServiceConfig("mediaservice", config.get(ResourceName("MEDIASERVICE"), ConfigConvention.SRV_RECORD), config)
  )
  private val authsyService = JsonService(
    ServiceConfig("authsy", config.get(ResourceName("AUTHSY"), ConfigConvention.SRV_RECORD), config)
  )

  val searchService = JsonService(
    ServiceConfig("search", config.get(ResourceName("SEARCH"), ConfigConvention.SRV_RECORD), config)
  )

  private val subscriptionsService = JsonService(
    ServiceConfig("user_subscriptions", config.get(ResourceName("USER_SUBSCRIPTIONS"), ConfigConvention.SRV_RECORD), config)
  )

  private val stitch4followsService = JsonService(
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


  private val waveformUrlsRepo = new WaveformUrlsRepository(okidokiService, mediaService)

  // Whitelist source: http://redash.int.s-cloud.net/queries/632/source
  private val whitelistedClients: Set[ModuleUrn] = Set(
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

  val baseUrl = config.get("APP_BASE_URL", DataSensitivity.NON_SENSITIVE)

  val rolloutClient = Rollout(moduleConfig, moduleTelemetry)
  val rollout = Some(rolloutClient)

  val enrichLikesCounts: () => Future[Boolean] =
    () => rolloutClient.isActive(BasicRolloutFeature("load_user_like_counts_from_liebling"))


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

  val playlistDeletionClient =
    new PlaylistDeletionClient(
      ModuleJsonClient(
        ModuleServiceEntryPoint(moduleConfig.get(ModuleResourceName("OKIDOKI"), ModuleConfigConvention.SRV_RECORD)),
        HttpClientConfig.from(ModuleResourceName("okidoki"), moduleConfig),
        moduleTelemetry
      ))

}
