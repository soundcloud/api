package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.client.config.HttpClientConfig
import com.soundcloud.jvmkit.module.http.client.{HttpClient, JsonClient}
import com.soundcloud.jvmkit.module.rollout.{BasicRolloutFeature, Rollout}
import com.soundcloud.jvmkit.module.servicediscovery.ServiceEntryPoint
import com.soundcloud.jvmkit.module.telemetry.{MetricsRegistry, Telemetry}
import com.soundcloud.jvmkit.module.util.config.{AppConfig, ConfigConvention, DataSensitivity}
import com.soundcloud.jvmkit.module.util.{ResourceName, Urn}
import com.soundcloud.publicApiStrangler.authorization._
import com.soundcloud.publicApiStrangler.client._
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.client.follows.FollowsClient
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.mediaservice.MediaServiceUrlGenClient
import com.soundcloud.publicApiStrangler.client.mothership.{OkidokiClient, RichOkidokiClient}
import com.soundcloud.publicApiStrangler.client.playlists.{PlaylistDeletionClient, PlaylistsClient}
import com.soundcloud.publicApiStrangler.client.pubmese.PubmeseClient
import com.soundcloud.publicApiStrangler.client.quota.UserQuotaClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.client.stitch.StitchClient
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorClient
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.mapper.liebling.LikeCountMapper
import com.soundcloud.publicApiStrangler.mapper.search.SearchEntityMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import com.soundcloud.publicApiStrangler.mapper.waveform.WaveformMapper
import com.soundcloud.publicApiStrangler.client.media.WaveformUrlsRepository
import com.soundcloud.publicApiStrangler.service.TrackAccessibilityService
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackRepository, TrackRepresentationsService}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.service.RetryPolicy.RetryableWriteException
import com.twitter.util.{Future, Throw, Try}

trait Clients {

  def moduleConfig: AppConfig

  def config = moduleConfig

  def moduleTelemetry: Telemetry

  def telemetry = moduleTelemetry

  def metricsRegistry: MetricsRegistry

  private def jsonClient(resourceName: String, configConvention: ConfigConvention = ConfigConvention.SRV_RECORD) = JsonClient(
    ServiceEntryPoint(config.get(ResourceName(resourceName), configConvention)),
    HttpClientConfig.from(ResourceName(resourceName), config),
    telemetry
  )

  private val okidokiJsonClient = jsonClient("okidoki")
  val okidokiClient = new OkidokiClient(okidokiJsonClient)

  private val timelineJsonClient =
    JsonClient(
      ServiceEntryPoint(config.get("TIMELINE_SRV_RECORD", DataSensitivity.NON_SENSITIVE)),
      HttpClientConfig.from(ResourceName("timeline"), config),
      telemetry
    )
  val timelineClient = new TimelineJsonClient(timelineJsonClient)

  val lieblingClient = new LieblingClient(jsonClient("liebling"))

  private def createPublicApiClient(resourceName: String): Service[Request, Response] = {
    val writeExceptions: PartialFunction[(Request, Try[Response]), Boolean] = {
      case (_, Throw(RetryableWriteException(_))) => true
    }

    HttpClient(
      ServiceEntryPoint(config.get(ResourceName(resourceName), ConfigConvention.ADDRESS)),
      HttpClientConfig.from(ResourceName(resourceName), config),
      telemetry,
      retryOn = Some(writeExceptions)
    ).httpService
  }

  lazy val publicApiClient = createPublicApiClient("PUBLIC_API")

  lazy val followsClient = new FollowsClient(jsonClient("follows"))

  lazy val repostsClient = new RepostsClient(jsonClient("reposts"))

  lazy val gatekeeperClient = new GatekeeperClient(jsonClient("gatekeeper"))

  lazy val similarSoundsClient = new SimilarSoundsClient(jsonClient("similar_sounds"))

  lazy val trackCoordinatorClient = new TrackCoordinatorClient(jsonClient("track_coordinator"))

  lazy val pubmeseClient = new PubmeseClient(jsonClient("pubmese"))

  lazy val stitchClient = new StitchClient(jsonClient("stitch", ConfigConvention.ADDRESS))

  private val mediaServiceUrlGenJsonClient = JsonClient(
    ServiceEntryPoint(moduleConfig.get(ResourceName("MEDIASERVICE"), ConfigConvention.SRV_RECORD)),
    HttpClientConfig.from(ResourceName("mediaservice_urlgen"), moduleConfig),
    telemetry
  )
  val mediaServiceUrlGenClient = new MediaServiceUrlGenClient(mediaServiceUrlGenJsonClient)

  val mediaService = jsonClient("mediaservice")
  private val authsyService = jsonClient("authsy")

  val searchService = jsonClient("search")

  private val subscriptionsService = jsonClient("user_subscriptions")

  private val stitch4followsService = jsonClient("stitch4follows")

  val playlistsClient = new PlaylistsClient(jsonClient("playlist", ConfigConvention.ADDRESS))

  val followCountsClient = new FollowCountsClient(stitch4followsService, moduleConfig)

  val trackmetadataClient = TrackmetadataClient(config, telemetry)

  val contentAuthorizationRules = new ContentAuthorizationRules(
    new ContentAuthorizationService(authsyService),
    new SubscriptionsService(subscriptionsService))


  private val waveformUrlsRepo = new WaveformUrlsRepository(okidokiJsonClient, mediaService)

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

  val blacklistOfAppIdsForUserSiloing: Set[Urn] =
    config.get("APP_SILOING_BLACKLIST_APPS", DataSensitivity.NON_SENSITIVE)
      .split(",")
      .map(appId => new Urn(appId.trim))
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
      JsonClient(
        ServiceEntryPoint(moduleConfig.get(ResourceName("OKIDOKI"), ConfigConvention.SRV_RECORD)),
        HttpClientConfig.from(ResourceName("okidoki"), moduleConfig),
        moduleTelemetry
      ))

}
