package com.soundcloud.publicApiStrangler

import com.soundcloud.hocuspocus.HocuspocusClientProtobuf
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.client.config.HttpClientConfig
import com.soundcloud.jvmkit.module.http.client.{HttpClient, JsonClient}
import com.soundcloud.jvmkit.module.rollout.{BasicRolloutFeature, Rollout}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.twirp.TwirpClient
import com.soundcloud.jvmkit.module.twirp.filters.ClientTelemetry
import com.soundcloud.jvmkit.module.util.config.{AppConfig, ConfigConvention, DataSensitivity}
import com.soundcloud.jvmkit.module.util.{ResourceName, Urn}
import com.soundcloud.publicApiStrangler.authorization._
import com.soundcloud.publicApiStrangler.client._
import com.soundcloud.publicApiStrangler.client.comments.MoshimoshiCommentsClient
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.client.follows.FollowsClient
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.media.{
  MediaServiceClient,
  TrackAccessRecorderClient,
  WaveformUrlsGenerator
}
import com.soundcloud.publicApiStrangler.client.mothership.{OkidokiClient, RichOkidokiClient}
import com.soundcloud.publicApiStrangler.client.playlists.PlaylistDeletionClient
import com.soundcloud.publicApiStrangler.client.pubmese.PubmeseClient
import com.soundcloud.publicApiStrangler.client.quota.UserQuotaClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.client.search.SearchClient
import com.soundcloud.publicApiStrangler.client.stitch.StitchClient
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorClient
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.client.tracks.TracksClient
import com.soundcloud.publicApiStrangler.service._
import com.soundcloud.publicApiStrangler.service.comments.CommentService
import com.soundcloud.publicApiStrangler.service.media.{StreamService, TrackAccessRecorderService}
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackRepresentationsService, TrackUpdateService}
import com.soundcloud.publicApiStrangler.service.tracks.VisibleTrackMapper
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.service.RetryPolicy.RetryableWriteException
import com.twitter.util.{Future, Throw, Try}
import proto.soundcloud.authenticator.oauth.AuthorizationClientProtobuf
import proto.soundcloud.playlists.api.PlaylistsClientProtobuf
import proto.soundcloud.tracks.api.TracksClientProtobuf

class Clients(
    config: AppConfig,
    telemetry: Telemetry,
    allowlistedCients: Set[Urn],
    exceptionCollector: ExceptionCollector
) {
  private def jsonClient(resourceName: String) = JsonClient(
    HttpClientConfig.from(ResourceName(resourceName), config),
    telemetry
  )

  private val okidokiJsonClient = jsonClient("okidoki")
  val okidokiClient = new OkidokiClient(okidokiJsonClient)

  val timelineClient = new TimelineJsonClient(jsonClient("timeline"))

  val lieblingClient = new LieblingClient(jsonClient("liebling"), exceptionCollector)

  val searchJsonClient = jsonClient("search")
  val searchClient = new SearchClient(searchJsonClient)

  val publicApiClient: Service[Request, Response] = {
    val name = ResourceName("PUBLIC_API")

    val writeExceptions: PartialFunction[(Request, Try[Response]), Boolean] = {
      case (_, Throw(RetryableWriteException(_))) => true
    }

    HttpClient[String](
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

  lazy val moshimoshiCommentsClient = new MoshimoshiCommentsClient(jsonClient("moshimoshi_comments"))

  private val subscriptionsService = jsonClient("user_subscriptions")

  private val stitch4followsService = jsonClient("stitch4follows")

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

  val tracksTwirpClient = TwirpClient(
    ResourceName("tracks"),
    config,
    telemetry,
    new TracksClientProtobuf(_, _)
  )

  val playlistsTwirpClient = TwirpClient(
    ResourceName("playlists"),
    config,
    telemetry,
    new PlaylistsClientProtobuf(_, _)
  )

  val mediaServiceClient = new MediaServiceClient(jsonClient("media_service"))

  val trackAccessRecorderService =
    new TrackAccessRecorderService(new TrackAccessRecorderClient(jsonClient("track_access_recorder")), telemetry)

  val userAuthentication = UserAuthentication(config, telemetry)

  val baseUrl: String = config.get("APP_BASE_URL", DataSensitivity.NON_SENSITIVE)

  val rolloutClient = Rollout(config, telemetry)
  val rollout = Some(rolloutClient)

  val enrichLikesCounts: () => Future[Boolean] =
    () => rolloutClient.isActive(BasicRolloutFeature("load_user_like_counts_from_liebling"))

  val richOkidokiClient = new RichOkidokiClient(okidokiJsonClient)

  val userQuotaClient = new UserQuotaClient(okidokiJsonClient)

  val trackVisibilityService =
    new TrackVisibilityService(
      tracksTwirpClient,
      new VisibleTrackMapper,
      allowlistedCients
    )

  val tracksService = new TrackRepresentationsService(
    trackVisibilityService,
    richOkidokiClient,
    pubmeseClient,
    stitchClient,
    lieblingClient,
    waveformUrlsGenerator,
    userQuotaClient
  )

  private val hocuspocusConfig = HttpClientConfig.from(ResourceName("hocuspocus"), config)
  private val hocuspocusHttpClient = HttpClient[String](hocuspocusConfig, telemetry)
  private val hocuspocusTelemetry = ClientTelemetry.from(hocuspocusConfig, telemetry)
  val hocuspocusClient = new HocuspocusClientProtobuf(hocuspocusHttpClient.httpService, hocuspocusTelemetry)

  val userTracksService = new UserTracksService(tracksService, trackmetadataClient)
  val trackUpdateService =
    new TrackUpdateService(trackCoordinatorClient, okidokiClient, hocuspocusClient, tracksService)
  val similarTracksService = new SimilarTracksService(tracksService, systemPlaylistsClient)
  val likesService = new LikesService(tracksService, lieblingClient)
  val playlistService = new PlaylistsService(playlistsTwirpClient, tracksService, okidokiClient, exceptionCollector)
  val userPlaylistsService = new UserPlaylistsService(playlistService, okidokiClient)
  val searchService =
    new SearchService(searchClient, tracksService, followCountsClient, repostsClient, playlistService, okidokiClient)

  val timelineService = new TimelineService(timelineClient, tracksService, playlistService)

  val streamService = new StreamService(trackVisibilityService, tracksClient)

  val playlistDeletionClient = new PlaylistDeletionClient(okidokiJsonClient)
  val commentsService = new CommentService(richOkidokiClient, moshimoshiCommentsClient)

  private val authorizationConfig = HttpClientConfig.from(ResourceName("oauth_authorization"), config)
  private val authorizationHttpClient = HttpClient[String](authorizationConfig, telemetry)
  private val authorizationTelemetry = ClientTelemetry.from(authorizationConfig, telemetry)
  val authorizationClient = new AuthorizationClientProtobuf(authorizationHttpClient.httpService, authorizationTelemetry)
}
