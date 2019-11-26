package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.client.config.HttpClientConfig
import com.soundcloud.jvmkit.module.http.client.{HttpClient, JsonClient}
import com.soundcloud.jvmkit.module.rollout.{BasicRolloutFeature, Rollout}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.config.{AppConfig, ConfigConvention, DataSensitivity}
import com.soundcloud.jvmkit.module.util.{ResourceName, Urn}
import com.soundcloud.publicApiStrangler.authorization._
import com.soundcloud.publicApiStrangler.client._
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.client.follows.FollowsClient
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.media.{
  MediaServiceClient,
  TrackAccessRecorderClient,
  WaveformUrlsGenerator
}
import com.soundcloud.publicApiStrangler.client.mothership.{OkidokiClient, RichOkidokiClient}
import com.soundcloud.publicApiStrangler.client.playlists.{PlaylistDeletionClient, PlaylistsClient}
import com.soundcloud.publicApiStrangler.client.pubmese.PubmeseClient
import com.soundcloud.publicApiStrangler.client.quota.UserQuotaClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.client.stitch.StitchClient
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorClient
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.client.tracks.TracksClient
import com.soundcloud.publicApiStrangler.mapper.liebling.LikeCountMapper
import com.soundcloud.publicApiStrangler.mapper.search.SearchEntityMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import com.soundcloud.publicApiStrangler.mapper.waveform.WaveformMapper
import com.soundcloud.publicApiStrangler.service.TrackAccessibilityService
import com.soundcloud.publicApiStrangler.service.media.TrackAccessRecorderService
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackRepository, TrackRepresentationsService}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.service.RetryPolicy.RetryableWriteException
import com.twitter.util.{Future, Throw, Try}

class Clients(config: AppConfig, telemetry: Telemetry) {
  private def jsonClient(resourceName: String) = JsonClient(
    HttpClientConfig.from(ResourceName(resourceName), config),
    telemetry
  )

  private val okidokiJsonClient = jsonClient("okidoki")
  val okidokiClient = new OkidokiClient(okidokiJsonClient)

  val timelineClient = new TimelineJsonClient(jsonClient("timeline"))

  val lieblingClient = new LieblingClient(jsonClient("liebling"))

  val publicApiClient: Service[Request, Response] = {
    val name = ResourceName("PUBLIC_API")

    val writeExceptions: PartialFunction[(Request, Try[Response]), Boolean] = {
      case (_, Throw(RetryableWriteException(_))) => true
    }

    HttpClient(
      HttpClientConfig.from(name, config),
      telemetry,
      retryOn = Some(writeExceptions)
    ).httpService
  }

  val followsClient = new FollowsClient(jsonClient("follows"))

  val repostsClient = new RepostsClient(jsonClient("reposts"))

  val gatekeeperClient = new GatekeeperClient(jsonClient("gatekeeper"))

  val systemPlaylistsClient = new SystemPlaylistsClient(jsonClient("system_playlists"))

  val trackCoordinatorClient = new TrackCoordinatorClient(jsonClient("track_coordinator"))

  val pubmeseClient = new PubmeseClient(jsonClient("pubmese"))

  val stitchClient = new StitchClient(jsonClient("stitch"))

  val searchService: JsonClient = jsonClient("search")

  private val subscriptionsService = jsonClient("user_subscriptions")

  private val stitch4followsService = jsonClient("stitch4follows")

  val playlistsClient = new PlaylistsClient(jsonClient("playlist"))

  val followCountsClient = new FollowCountsClient(stitch4followsService, config)

  val trackmetadataClient = new TrackmetadataClient(jsonClient("trackmetadata"))

  val contentAuthorizationRules = new ContentAuthorizationRules(
    new ContentAuthorizationService(jsonClient("authsy")),
    new SubscriptionsService(subscriptionsService)
  )

  private val waveformUrlsGenerator = new WaveformUrlsGenerator(
    config.get(ResourceName("CDN_WAVE"), ConfigConvention.HTTPS_ENDPOINT)
  )

  val tracksClient = new TracksClient(jsonClient("tracks"))

  val mediaServiceClient = new MediaServiceClient(jsonClient("media_service"))

  val trackAccessRecorderService =
    new TrackAccessRecorderService(new TrackAccessRecorderClient(jsonClient("track_access_recorder")), telemetry)

  // Whitelist source: http://redash.int.s-cloud.net/queries/632/source
  private val whitelistedClients: Set[Urn] = Set(
    Urn("soundcloud", "systems", "soundcloud"), // Agent returned by Authenticator for those with _soundcloud_session cookie
    Urn("soundcloud", "applications", "124"), // SoundCloud iOS
    Urn("soundcloud", "applications", "3152"), // SoundCloud Android
    Urn("soundcloud", "applications", "3273"), // Mobile Soundcloud
    Urn("soundcloud", "applications", "3537"), // SoundCloud Desktop
    Urn("soundcloud", "applications", "43164"), // SoundCloud Player Widget
    Urn("soundcloud", "applications", "46941"), // SoundCloud.com
    Urn("soundcloud", "applications", "60973"), // SoundCloud Flash Widget
    Urn("soundcloud", "applications", "65097"), // MobileWeb3
    Urn("soundcloud", "applications", "66151"), // MobileWeb production
    Urn("soundcloud", "applications", "90575"), // SoundCloud Visual Embed Player
    Urn("soundcloud", "applications", "99561"), // SoundCloud Kik Messenger Card
    Urn("soundcloud", "applications", "120502"), // Twitter Partner
    Urn("soundcloud", "applications", "135495"), // Mobile Web App
    Urn("soundcloud", "applications", "167582"), // HEOS by Denon (Production)

    // other whitelisted apps
    Urn("soundcloud", "applications", "288860"),
    Urn("soundcloud", "applications", "271862"),
    Urn("soundcloud", "applications", "59007"),
    Urn("soundcloud", "applications", "62023"),
    Urn("soundcloud", "applications", "265616"),
    Urn("soundcloud", "applications", "265183")
  )

  val blacklistOfAppIdsForUserSiloing: Set[Urn] =
    config
      .get("APP_SILOING_BLACKLIST_APPS", DataSensitivity.NON_SENSITIVE)
      .split(",")
      .map(appId => Urn.parse(appId.trim).get)
      .toSet

  val userAuthentication = UserAuthentication(config, telemetry)
  val authorizeContent =
    new AuthorizeHttpResponse(contentAuthorizationRules, userAuthentication, TrackPolicyApplicator(whitelistedClients))

  val baseUrl: String = config.get("APP_BASE_URL", DataSensitivity.NON_SENSITIVE)

  val rolloutClient = Rollout(config, telemetry)
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
    waveformUrlsGenerator,
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
    waveformUrlsGenerator,
    userQuotaClient,
    trackAccessibilityService
  )

  val searchEntityMapper = new SearchEntityMapper(
    okidokiClient,
    followCountsClient,
    repostsClient,
    baseUrl,
    contentAuthorizationRules,
    new WaveformMapper(waveformUrlsGenerator),
    new LikeCountMapper(lieblingClient),
    new EntitySummaryMapper(okidokiClient, repostsClient, baseUrl)
  )

  val playlistDeletionClient = new PlaylistDeletionClient(okidokiJsonClient)
}
